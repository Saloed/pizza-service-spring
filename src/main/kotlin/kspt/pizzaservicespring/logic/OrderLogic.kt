package kspt.pizzaservicespring.logic

import kspt.pizzaservicespring.models.*
import kspt.pizzaservicespring.utils.MyResult
import org.springframework.data.repository.findByIdOrNull
import kspt.pizzaservicespring.models.OrderWithPermission
import kspt.pizzaservicespring.models.fullPermission
import kspt.pizzaservicespring.models.infoOnlyPermission

data class OrderModification(val status: OrderStatus, val promoId: Int?)

private abstract class OrderTransition<T : User>(val from: List<OrderStatus>, val to: OrderStatus) {

    fun match(user: User, order: Order, modification: OrderModification) =
            checkUserAccess(user, order)
                    && order.status in from
                    && modification.status == to
                    && additionalChecks(user, order, modification)

    abstract fun checkUserAccess(user: User, order: Order): Boolean
    open fun additionalChecks(user: User, order: Order, modification: OrderModification) = true
    abstract fun apply(
            user: User,
            order: Order,
            modification: OrderModification
    ): MyResult<Order>

    companion object {
        fun success(order: Order) = MyResult.Success(order)
        fun fail(message: String) = MyResult.Error<Order>(message)
    }
}

private abstract class ClientOrderTransition(vararg from: OrderStatus, to: OrderStatus) :
        OrderTransition<Client>(from.asList(), to) {
    override fun checkUserAccess(user: User, order: Order) = user is Client && order.client.id == user.id
}

private abstract class ManagerOrderTransition(vararg from: OrderStatus, to: OrderStatus) :
        OrderTransition<Manager>(from.asList(), to) {
    override fun checkUserAccess(user: User, order: Order) = user is Manager && order.manager?.id == user.id
}

private abstract class OperatorOrderTransition(vararg from: OrderStatus, to: OrderStatus) :
        OrderTransition<Operator>(from.asList(), to) {
    override fun checkUserAccess(user: User, order: Order) = user is Operator && order.operator?.id == user.id
}

private abstract class CourierOrderTransition(vararg from: OrderStatus, to: OrderStatus) :
        OrderTransition<Courier>(from.asList(), to) {
    override fun checkUserAccess(user: User, order: Order) = user is Courier && order.courier?.id == user.id
}


private val transitions = listOf(
        object : ClientOrderTransition(OrderStatus.DRAFT, to = OrderStatus.DRAFT) {

            override fun apply(user: User, order: Order, modification: OrderModification): MyResult<Order> {
                if (modification.promoId == null && order.promo == null) return success(order)
                val fullCost = order.pizza.map { it.price }.sum()
                if (modification.promoId == null) {
                    val newOrder = order.copy(promo = null, cost = fullCost)
                    return success(newOrder)
                }

                val promo = PromoClient.repository
                        .findAllByClientAndPromo_StatusIn(user as Client, listOf(PromoStatus.ACTIVE))
                        .map { it.promo }
                        .find { it.id == modification.promoId }
                        ?: return fail("No promo available")

                val newCost = when (promo.effect) {
                    PromoEffect.DISCOUNT_5 -> (fullCost * 0.95).toInt()
                    PromoEffect.DISCOUNT_10 -> (fullCost * 0.9).toInt()
                    PromoEffect.DISCOUNT_15 -> (fullCost * 0.85).toInt()
                }
                val orderWithPromo = order.copy(promo = promo, cost = newCost)
                return success(orderWithPromo)
            }

        },
        object : ClientOrderTransition(OrderStatus.DRAFT, to = OrderStatus.NEW) {
            fun findOperator(): Operator {
                val operators = Operator.repository.findAll()
                return operators.random()
            }

            fun findManager(): Manager {
                val managers = Manager.repository.findAll()
                return managers.random()
            }

            override fun apply(
                    user: User,
                    order: Order,
                    modification: OrderModification
            ): MyResult<Order> {
                val operator = findOperator()
                val manager = findManager()
                return success(order.copy(status = to, operator = operator, manager = manager, isActive = true))
            }
        },
        object : ClientOrderTransition(
                OrderStatus.DRAFT,
                OrderStatus.NEW,
                OrderStatus.APPROVED,
                OrderStatus.PROCESSING,
                OrderStatus.READY,
                OrderStatus.SHIPPING,
                to = OrderStatus.CANCELED
        ) {
            override fun apply(
                    user: User,
                    order: Order,
                    modification: OrderModification
            ): MyResult<Order> {
                return success(order.copy(isActive = false, status = to))
            }
        },
        object : OperatorOrderTransition(OrderStatus.NEW, to = OrderStatus.APPROVED) {
            override fun apply(
                    user: User,
                    order: Order,
                    modification: OrderModification
            ): MyResult<Order> {
                return success(order.copy(status = to))
            }
        },
        object : OperatorOrderTransition(OrderStatus.NEW, to = OrderStatus.CANCELED) {
            override fun apply(
                    user: User,
                    order: Order,
                    modification: OrderModification
            ): MyResult<Order> {
                return success(order.copy(status = to, isActive = false))
            }
        },
        object : ManagerOrderTransition(OrderStatus.APPROVED, to = OrderStatus.PROCESSING) {
            override fun apply(
                    user: User,
                    order: Order,
                    modification: OrderModification
            ): MyResult<Order> {
                return success(order.copy(status = to))
            }
        },
        object : ManagerOrderTransition(OrderStatus.PROCESSING, to = OrderStatus.READY) {
            fun findCourier(): Courier {
                val couriers = Courier.repository.findAll()
                return couriers.random()
            }

            override fun apply(
                    user: User,
                    order: Order,
                    modification: OrderModification
            ): MyResult<Order> {
                val courier = findCourier()
                return success(order.copy(status = to, courier = courier))
            }

        },
        object : ManagerOrderTransition(OrderStatus.APPROVED, OrderStatus.PROCESSING, to = OrderStatus.CANCELED) {
            override fun apply(
                    user: User,
                    order: Order,
                    modification: OrderModification
            ): MyResult<Order> {
                return success(order.copy(status = to, isActive = false))
            }
        },
        object : CourierOrderTransition(OrderStatus.READY, to = OrderStatus.SHIPPING) {
            override fun apply(
                    user: User,
                    order: Order,
                    modification: OrderModification
            ): MyResult<Order> {
                return success(order.copy(status = to))
            }
        },
        object : CourierOrderTransition(OrderStatus.SHIPPING, to = OrderStatus.CLOSED) {
            override fun apply(
                    user: User,
                    order: Order,
                    modification: OrderModification
            ): MyResult<Order> {
                if (order.payment == null) return fail("No payment for order")
                return success(order.copy(status = to, isActive = false))
            }
        }
)


object OrderLogic {

    private fun sanitizeOrder(user: User, order: Order): OrderWithPermission {
        val promo = order.promo?.infoOnlyPermission()
        val payment = order.payment?.fullPermission()

        return when (user) {
            is Client -> {
                val client = order.client.infoOnlyPermission()
                val operator = order.operator?.infoOnlyPermission()
                val manager = order.manager?.infoOnlyPermission()
                OrderWithPermission(
                        order.id, order.status.name, order.cost,
                        payment, promo, client, operator, manager,
                        null
                )
            }
            is Manager -> {
                val client = order.client.fullPermission()
                val operator = order.operator?.fullPermission()
                val manager = order.manager?.fullPermission()
                val courier = order.courier?.fullPermission()
                OrderWithPermission(
                        order.id, order.status.name, order.cost,
                        payment, promo, client, operator,
                        manager, courier
                )
            }
            is Operator -> {
                val client = order.client.infoOnlyPermission()
                val operator = order.operator?.infoOnlyPermission()
                val manager = order.manager?.fullPermission()
                OrderWithPermission(
                        order.id, order.status.name, order.cost,
                        payment, promo, client, operator, manager,
                        null
                )
            }
            is Courier -> {
                val client = order.client.infoOnlyPermission()
                val manager = order.manager?.fullPermission()
                val courier = order.courier?.fullPermission()
                OrderWithPermission(
                        order.id, order.status.name, order.cost,
                        payment, promo, client, null, manager, courier
                )
            }
        }
    }

    fun list(user: User) = when (user) {
        is Client -> Order.repository.findAllByClient(user)
        is Manager -> Order.repository.findAllByStatusInAndManager(OrderStatus.forManager, user)
        is Operator -> Order.repository.findAllByStatusInAndOperator(OrderStatus.forOperator, user)
        is Courier -> Order.repository.findAllByStatusInAndCourier(OrderStatus.forCourier, user)
    }.map { sanitizeOrder(user, it) }

    fun get(user: User, id: Int): OrderWithPermission? {
        val order = Order.repository.findByIdOrNull(id) ?: return null
        return when (user) {
            is Client -> if (order.client.id == user.id) order else null
            is Manager -> if (order.manager?.id == user.id) order else null
            is Operator -> if (order.operator?.id == user.id && order.isActive && order.status in OrderStatus.forOperator) order else null
            is Courier -> if (order.courier?.id == user.id && order.isActive && order.status in OrderStatus.forCourier) order else null
        }?.let { sanitizeOrder(user, it) }
    }

    fun create(user: User, pizza: List<Int>): MyResult<OrderWithPermission> {
        if (user !is Client) return MyResult.Error("Only client can create orders")
        if (pizza.isEmpty()) return MyResult.Error("Order pizza is empty")
        val dbPizza = Pizza.repository.findAllById(pizza)
        if (!dbPizza.map { it.id }.containsAll(pizza)) return MyResult.Error("Pizza list contains unknown items")
        val pizzaCost = dbPizza.map { it.price }.sum()
        val order = Order(
                status = OrderStatus.DRAFT,
                cost = pizzaCost,
                isActive = false,
                client = user
        )
        order.pizza.addAll(dbPizza)
        val createdOrder = Order.repository.save(order)
        val result = get(user, createdOrder.id) ?: return MyResult.Error("Not found")
        return MyResult.Success(result)
    }


    fun change(
            user: User,
            orderId: Int,
            modification: OrderModification
    ): MyResult<OrderWithPermission> {
        val order = Order.repository.findByIdOrNull(orderId) ?: return MyResult.Error("No such order")
        val transition =
                transitions.find { it.match(user, order, modification) }
                        ?: return MyResult.Error("Transition is not possible")
        val changed = transition.apply(user, order, modification)
        val result = when (changed) {
            is MyResult.Error -> return MyResult.Error(changed.message)
            is MyResult.Success -> changed.data
        }
        val resultWithDate = Order.repository.save(result)
        val resultWithPermission = sanitizeOrder(user, resultWithDate)
//        NotificationService.notifyUpdateOrder(order.client.id, order.id)
//        order.manager?.id?.let { NotificationService.notifyUpdateOrder(it, order.id) }
//        order.operator?.id?.let { NotificationService.notifyUpdateOrder(it, order.id) }
//        order.courier?.id?.let { NotificationService.notifyUpdateOrder(it, order.id) }
        return MyResult.Success(resultWithPermission)
    }

}

