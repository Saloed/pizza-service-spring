package kspt.pizzaservicespring.logic

import kspt.pizzaservicespring.models.*
import org.springframework.data.repository.findByIdOrNull


object PizzaLogic {
    fun list(user: User, orderId: Int): List<Pizza>? {
        val order = OrderLogic.get(user, orderId) ?: return null
        return Order.repository.findByIdOrNull(order.id)?.fetchPizza()
    }

    fun list(): List<Pizza> = Pizza.repository.qetPizzaFromApi()
}
