package kspt.pizzaservicespring.web

import kspt.pizzaservicespring.logic.PromoCreationParameters
import kspt.pizzaservicespring.logic.PromoLogic
import kspt.pizzaservicespring.logic.PromoModificationParams
import kspt.pizzaservicespring.models.PromoEffect
import kspt.pizzaservicespring.models.PromoStatus
import kspt.pizzaservicespring.models.User
import kspt.pizzaservicespring.utils.getListQueryParams
import kspt.pizzaservicespring.utils.responseListRange
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
class PromoController {

    data class PromoListFilter(val id: List<Int>?)

    data class PromoCreationForm(val clientIds: List<Int>, val effect: String, val description: String)
    data class PromoModificationForm(val status: String, val result: String?)

    @GetMapping("/promo")
    fun getPromos(@AuthenticationPrincipal user: User, @RequestParam requestParams: Map<String, String>): ResponseEntity<*> {
        val params = getListQueryParams<PromoListFilter>(requestParams)
        val data = PromoLogic.list(user)
        return data.response {
            val result = if (params.filter?.id != null) it.filter { it.id in params.filter.id } else it
            ResponseEntity.ok().responseListRange(result, params.range)
        }
    }

    @GetMapping("/promo/{id}")
    fun getPromo(@AuthenticationPrincipal user: User, @PathVariable id: Int): ResponseEntity<*> {
        val data = PromoLogic.get(user, id)
        return data.response()
    }

    @PostMapping("/promo")
    fun createPromo(@AuthenticationPrincipal user: User, @RequestBody form: PromoCreationForm): ResponseEntity<*> {
        val effect = PromoEffect.valueOf(form.effect.toUpperCase())
        val parameters = PromoCreationParameters(form.clientIds, effect, form.description)
        val data = PromoLogic.create(user, parameters)
        return data.response()
    }

    @PutMapping("/promo/{id}")
    fun updatePromo(@AuthenticationPrincipal user: User, @PathVariable id: Int, @RequestBody form: PromoModificationForm): ResponseEntity<*> {
        val status = PromoStatus.valueOf(form.status.toUpperCase())
        val parameters = PromoModificationParams(status, form.result)
        val data = PromoLogic.update(user, id, parameters)
        return data.response()
    }

}