package kspt.pizzaservicespring.logic

import kspt.pizzaservicespring.models.*
import kspt.pizzaservicespring.utils.MyResult
import org.springframework.data.repository.findByIdOrNull


data class PromoCreationParameters(val clientIds: List<Int>, val effect: PromoEffect, val description: String)

data class PromoModificationParams(val status: PromoStatus, val result: String?)

object PromoLogic {
    suspend fun create(user: User, parameters: PromoCreationParameters): MyResult<PromoWithPermission> {
        if (user !is Manager) return MyResult.Error("Only manager can create promos")
        if (parameters.clientIds.isEmpty()) return MyResult.Error("Promo without users is not permitted")
        val clients = Client.repository.findAllById(parameters.clientIds)
        if (!clients.map { it.id }.containsAll(parameters.clientIds)) return MyResult.Error("All users must be a clients")
        val promoRecord = Promo(
                status = PromoStatus.NEW,
                result = null,
                manager = user,
                description = parameters.description,
                effect = parameters.effect
        )
        val promo = Promo.repository.save(promoRecord)
        val promoClients = clients.map {
            PromoClient(
                    operator = null,
                    promo = promo,
                    client = it,
                    status = PromoClientStatus.NOTINFORMED
            )
        }
        val createdPromoClients = PromoClient.repository.saveAll(promoClients)
        return MyResult.Success(promo.fullPermission())
    }


    suspend fun list(user: User) = when (user) {
        is Manager -> {
            val userPromos = Promo.repository.findByManager(user)
            val activePromos = Promo.repository.findByStatusIn(listOf(PromoStatus.ACTIVE))
            val managerPromoIds = userPromos.map { it.id }.toSet()
            val full = userPromos.map { it.fullPermission() }
            val infoOnly = activePromos.filterNot { it.id in managerPromoIds }.map { it.infoOnlyPermission() }
            MyResult.Success(full + infoOnly)
        }
        is Operator, is Courier -> {
            val result = Promo.repository.findByStatusIn(listOf(PromoStatus.ACTIVE)).map { it.infoOnlyPermission() }
            MyResult.Success(result)
        }
        is Client -> {
            val result = Promo.repository
                    .findByStatusInAndClients_Client(listOf(PromoStatus.ACTIVE), user)
                    .map { it.infoOnlyPermission() }
            MyResult.Success(result)
        }
    }

    suspend fun get(user: User, promoId: Int): MyResult<PromoWithPermission> {
        val promo = Promo.repository.findByIdOrNull(promoId) ?: return MyResult.Error("Not found")
        if (user is Manager && user.id == promo.manager.id) return MyResult.Success(promo.fullPermission())
        if (user is Client) {
            val clientPromoIds = Promo.repository
                    .findByStatusInAndClients_Client(listOf(PromoStatus.ACTIVE), user)
                    .map { it.id }
            if (promo.id !in clientPromoIds) return MyResult.Error("No access")
        }
        return MyResult.Success(promo.infoOnlyPermission())
    }

    private suspend fun startPromo(user: User, promo: Promo): Promo {
        val operators = Operator.repository.findAll()
        val clientsWithOperator = promo.clients.map {
            it.copy(
                    status = PromoClientStatus.NOTINFORMED,
                    operator = operators.random()
            )
        }
        PromoClient.repository.saveAll(clientsWithOperator)
        return promo.copy(status = PromoStatus.ACTIVE)
    }

    private suspend fun closePromo(user: User, promo: Promo): Promo {
        return promo.copy(status = PromoStatus.FINISHED)
    }

    private suspend fun analyzePromo(user: User, promo: Promo, result: String): Promo {
        return promo.copy(status = PromoStatus.CLOSED, result = result)
    }


    suspend fun update(user: User, promoId: Int, params: PromoModificationParams): MyResult<PromoWithPermission> {
        if (user !is Manager) return MyResult.Error("Only manager can modify promos")
        val promo = Promo.repository.findByIdOrNull(promoId) ?: return MyResult.Error("Not found")
        if (promo.manager.id != user.id) return MyResult.Error("No access")

        val updatedPromo = when (promo.status to params.status) {
            (PromoStatus.NEW to PromoStatus.ACTIVE) -> startPromo(user, promo)
            (PromoStatus.ACTIVE to PromoStatus.FINISHED) -> closePromo(user, promo)
            (PromoStatus.FINISHED to PromoStatus.CLOSED) -> {
                val promoResult = params.result ?: return MyResult.Error("No result supplied")
                analyzePromo(user, promo, promoResult)
            }
            else -> return MyResult.Error("No possible transition")
        }
        val savedUpdatedPromo = Promo.repository.save(updatedPromo)
        return MyResult.Success(savedUpdatedPromo.fullPermission())
    }

}


