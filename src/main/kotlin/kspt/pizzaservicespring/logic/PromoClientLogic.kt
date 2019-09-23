package kspt.pizzaservicespring.logic

import kspt.pizzaservicespring.models.*
import kspt.pizzaservicespring.utils.MyResult
import org.springframework.data.repository.findByIdOrNull


object PromoClientLogic {

    suspend fun list(user: User, promoId: Int?): MyResult<List<PromoClientWithPermission>> {
        return when {
            user is Manager && promoId != null -> {
                val promo = Promo.repository.findByIdOrNull(promoId) ?: return MyResult.Error("No such promo")
                if (promo.manager.id != user.id) return MyResult.Error("No access")
                promo.clients.map { it.fullPermission() }
            }
            user is Manager && promoId == null -> {
                val promos = Promo.repository.findAllByManager(user)
                promos.flatMap { promo ->
                    promo.clients.map { it.fullPermission() }
                }
            }
            user is Operator && promoId != null -> {
                val promo = Promo.repository.findByIdOrNull(promoId) ?: return MyResult.Error("No such promo")
                val clients = promo.clients.map { it.fullPermission() }
                clients.filter { it.operator?.id == user.id }
            }
            user is Operator && promoId == null -> {
                PromoClient.repository
                        .findAllByOperatorAndPromo_StatusIn(user, listOf(PromoStatus.ACTIVE))
                        .map { it.fullPermission() }
            }

            else -> return MyResult.Error("No access")
        }.let { MyResult.Success(it) }
    }

    suspend fun get(user: User, id: Int): MyResult<PromoClientWithPermission> {
        if (user !is Manager && user !is Operator) return MyResult.Error("No access")
        val promoClient = PromoClient.repository.findByIdOrNull(id) ?: return MyResult.Error("Not found")
        if (user is Manager && promoClient.promo.manager.id != user.id) return MyResult.Error("No access")
        if (user is Operator && promoClient.operator?.id != user.id) return MyResult.Error("No access")
        return MyResult.Success(promoClient.fullPermission())
    }

    suspend fun update(user: User, id: Int, status: PromoClientStatus): MyResult<PromoClientWithPermission> {
        if (user !is Operator) return MyResult.Error("Only operator can change promo client status")
        val promoClient = PromoClient.repository.findByIdOrNull(id) ?: return MyResult.Error("Not found")
        if (promoClient.operator?.id != user.id) return MyResult.Error("No access")
        if (promoClient.promo.status != PromoStatus.ACTIVE) return MyResult.Error("No access")
        val nextStatus = when (promoClient.status to status) {
            (PromoClientStatus.NOTINFORMED to PromoClientStatus.PROCESSING) -> PromoClientStatus.PROCESSING
            (PromoClientStatus.PROCESSING to PromoClientStatus.NOTINFORMED) -> PromoClientStatus.NOTINFORMED
            (PromoClientStatus.PROCESSING to PromoClientStatus.INFORMED) -> PromoClientStatus.INFORMED
            else -> return MyResult.Error("Transition not possible")
        }
        val updatedClient = PromoClient.repository.save(promoClient.copy(status = nextStatus))
        return MyResult.Success(updatedClient.fullPermission())
    }

}
