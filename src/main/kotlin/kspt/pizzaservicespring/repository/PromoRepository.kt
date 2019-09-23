package kspt.pizzaservicespring.repository

import kspt.pizzaservicespring.models.*
import org.springframework.data.jpa.repository.JpaRepository

interface PromoRepository : JpaRepository<Promo, Int>{
    fun findByStatusInAndManager(status: List<PromoStatus>, manager: Manager): List<Promo>
    fun findByManager(manager: Manager): List<Promo>
    fun findByStatusIn(status: List<PromoStatus>): List<Promo>
    fun findByStatusInAndClients_Client(status: List<PromoStatus>, client: Client): List<Promo>
}

interface PromoClientRepository : JpaRepository<PromoClient, Int> {
    fun findByClientAndPromo_StatusIn(client: Client, promo_status: List<PromoStatus>): List<PromoClient>
    fun findByOperatorAndPromo_StatusIn(client: Operator, promo_status: List<PromoStatus>): List<PromoClient>
}
