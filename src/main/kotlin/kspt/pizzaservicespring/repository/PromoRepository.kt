package kspt.pizzaservicespring.repository

import kspt.pizzaservicespring.models.*
import org.springframework.data.jpa.repository.JpaRepository

interface PromoRepository : JpaRepository<Promo, Int>{
    fun findAllByManager(manager: Manager): List<Promo>
    fun findAllByStatusIn(status: List<PromoStatus>): List<Promo>
    fun findAllByStatusInAndClients_Client(status: List<PromoStatus>, client: Client): List<Promo>
}

interface PromoClientRepository : JpaRepository<PromoClient, Int> {
    fun findAllByClientAndPromo_StatusIn(client: Client, promo_status: List<PromoStatus>): List<PromoClient>
    fun findAllByOperatorAndPromo_StatusIn(client: Operator, promo_status: List<PromoStatus>): List<PromoClient>
}
