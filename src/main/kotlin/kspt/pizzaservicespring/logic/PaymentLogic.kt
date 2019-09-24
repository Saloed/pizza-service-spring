package kspt.pizzaservicespring.logic

import kspt.pizzaservicespring.models.*
import kspt.pizzaservicespring.utils.MyResult
import org.springframework.data.repository.findByIdOrNull

data class PaymentCreateForm(val orderId: Int, val type: String, val amount: Int, val transaction: String?)

object PaymentLogic {
    fun create(user: User, payment: PaymentCreateForm): MyResult<PaymentWithPermission> {
        if (user !is Courier) return MyResult.Error("Only courier can create payments")
        val order = Order.repository.findByIdOrNull(payment.orderId)
        if (order == null || order.courier?.id != user.id) return MyResult.Error("Order not found")
        if (order.payment != null) return MyResult.Error("Order already payed")
        if (order.cost != payment.amount) return MyResult.Error("Incorrect payment amount")
        val paymentType = PaymentType.valueOf(payment.type.toUpperCase())
        if (paymentType == PaymentType.CARD && payment.transaction == null) return MyResult.Error("Card payments must have a transaction")
        val paymentRecord = Payment(
                order = order,
                type = paymentType,
                amount = payment.amount,
                cardTransaction = payment.transaction
        )
        val result = Payment.repository.save(paymentRecord)
        return MyResult.Success(result.fullPermission())
    }
}
