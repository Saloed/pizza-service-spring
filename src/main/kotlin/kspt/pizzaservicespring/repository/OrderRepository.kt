package kspt.pizzaservicespring.repository

import kspt.pizzaservicespring.models.*
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Int>{
    fun findByStatusInAndClient(status: List<OrderStatus>, client: Client): List<Order>
    fun findByStatusInAndManager(status: List<OrderStatus>, manager: Manager): List<Order>
    fun findByStatusInAndOperator(status: List<OrderStatus>, operator: Operator): List<Order>
    fun findByStatusInAndCourier(status: List<OrderStatus>, courier: Courier): List<Order>

    fun findByClient(client: Client): List<Order>
    fun findByManager(manager: Manager): List<Order>
    fun findByOperator(operator: Operator): List<Order>
    fun findByCourier(courier: Courier): List<Order>
}
