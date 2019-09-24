package kspt.pizzaservicespring.web

import kspt.pizzaservicespring.logic.PaymentCreateForm
import kspt.pizzaservicespring.logic.PaymentLogic
import kspt.pizzaservicespring.models.User
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PaymentController {

    @PostMapping("/payment")
    fun create(@AuthenticationPrincipal user: User, @RequestBody form: PaymentCreateForm): ResponseEntity<*> {
        val result = PaymentLogic.create(user, form)
        return result.response()
    }
}
