package kspt.pizzaservicespring.web

import kspt.pizzaservicespring.logic.OrderLogic
import kspt.pizzaservicespring.logic.OrderModification
import kspt.pizzaservicespring.models.OrderStatus
import kspt.pizzaservicespring.models.OrderWithPermission
import kspt.pizzaservicespring.models.User
import kspt.pizzaservicespring.utils.getListQueryParams
import kspt.pizzaservicespring.utils.responseListRange
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
class OrderController {
    data class OrderListFilter(val id: Int?)
    data class OrderCreateForm(val pizza: List<Int>)
    data class OrderModificationForm(val status: String, val promoId: Int?)

    @GetMapping("/order/{id}")
    fun getOrder(@AuthenticationPrincipal user: User, @PathVariable id: Int): ResponseEntity<OrderWithPermission> {
        val data = OrderLogic.get(user, id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(data)
    }

    @GetMapping("/order")
    fun getOrders(@AuthenticationPrincipal user: User, @RequestParam params: Map<String, String>): ResponseEntity<List<OrderWithPermission>> {
        val listParams = getListQueryParams<OrderListFilter>(params)
        val data = OrderLogic.list(user)
        return ResponseEntity.status(HttpStatus.OK).responseListRange(data, listParams.range)
    }

    @PostMapping("/order")
    fun createOrder(@AuthenticationPrincipal user: User, @RequestBody form: OrderCreateForm): ResponseEntity<*> {
        val result = OrderLogic.create(user, form.pizza)
        return result.response()
    }

    @PutMapping("/order/{id}")
    fun updateOrder(@AuthenticationPrincipal user: User, @PathVariable id: Int, @RequestBody form: OrderModificationForm): ResponseEntity<*> {
        val status = OrderStatus.valueOf(form.status.toUpperCase())
        val orderModification = OrderModification(status, form.promoId)
        val result = OrderLogic.change(user, id, orderModification)
        return result.response()
    }


}
