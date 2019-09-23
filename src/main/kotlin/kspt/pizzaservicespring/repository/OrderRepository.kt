package kspt.pizzaservicespring.repository

import kspt.pizzaservicespring.models.*
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Int>{
    fun findAllByStatusInAndManager(status: List<OrderStatus>, manager: Manager): List<Order>
    fun findAllByStatusInAndOperator(status: List<OrderStatus>, operator: Operator): List<Order>
    fun findAllByStatusInAndCourier(status: List<OrderStatus>, courier: Courier): List<Order>

    fun findAllByClient(client: Client): List<Order>
}
