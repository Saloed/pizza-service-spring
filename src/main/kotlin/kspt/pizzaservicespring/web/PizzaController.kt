package kspt.pizzaservicespring.web

import kspt.pizzaservicespring.logic.PizzaLogic
import kspt.pizzaservicespring.models.Pizza
import kspt.pizzaservicespring.models.User
import kspt.pizzaservicespring.utils.getListQueryParams
import kspt.pizzaservicespring.utils.responseListRange
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class PizzaController {

    data class PizzaListFilter(val orderId: Int?, val id: Int?)

    @GetMapping("/pizza")
    fun getPizza(@AuthenticationPrincipal user: User, @RequestParam params: Map<String, String>): ResponseEntity<List<Pizza>> {
        val listParams = getListQueryParams<PizzaListFilter>(params)
        val pizza = when {
            listParams.filter?.orderId != null -> PizzaLogic.list(user, listParams.filter.orderId)
            else -> PizzaLogic.list()
        } ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok().responseListRange(pizza, listParams.range)
    }
}