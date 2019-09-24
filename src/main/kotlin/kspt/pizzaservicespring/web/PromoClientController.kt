package kspt.pizzaservicespring.web

import kspt.pizzaservicespring.logic.PromoClientLogic
import kspt.pizzaservicespring.models.PromoClientStatus
import kspt.pizzaservicespring.models.User
import kspt.pizzaservicespring.utils.getListQueryParams
import kspt.pizzaservicespring.utils.responseListRange
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
class PromoClientController {

    data class PromoClientFilter(val promoId: Int?)

    data class PromoClientModificationForm(val status: String)

    @GetMapping("/promoClient/{id}")
    fun getPromoClient(@AuthenticationPrincipal user: User, @PathVariable id: Int): ResponseEntity<*> {
        val data = PromoClientLogic.get(user, id)
        return data.response()
    }

    @GetMapping("/promoClient")
    fun getPromoClients(@AuthenticationPrincipal user: User, @RequestParam requestParam: Map<String, String>): ResponseEntity<*> {
        val params = getListQueryParams<PromoClientFilter>(requestParam)
        val data = PromoClientLogic.list(user, params.filter?.promoId)
        return data.response {
            ResponseEntity.ok().responseListRange(it, params.range)
        }
    }

    @PutMapping("/promoClient/{id}")
    fun updatePromoClient(@AuthenticationPrincipal user: User, @PathVariable id: Int, @RequestBody form: PromoClientModificationForm): ResponseEntity<*> {
        val status = PromoClientStatus.valueOf(form.status.toUpperCase())
        val data = PromoClientLogic.update(user, id, status)
        return data.response()
    }
}


