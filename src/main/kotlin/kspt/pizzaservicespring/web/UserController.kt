package kspt.pizzaservicespring.web

import kspt.pizzaservicespring.logic.UserLogic
import kspt.pizzaservicespring.models.ClientWithPermission
import kspt.pizzaservicespring.models.User
import kspt.pizzaservicespring.models.UserRoleType
import kspt.pizzaservicespring.utils.getListQueryParams
import kspt.pizzaservicespring.utils.responseListRange
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
class UserController {

    @PostMapping("/manager")
    fun createManager(@AuthenticationPrincipal user: User, @RequestBody form: ManagerRegistrationForm) =
            form.createGenericUser { login, password ->
                UserLogic.create(user, login, password, UserRoleType.Manager)
            }

    @PostMapping("/operator")
    fun createOperator(@AuthenticationPrincipal user: User, @RequestBody form: OperatorRegistrationForm) =
            form.createGenericUser { login, password ->
                UserLogic.create(user, login, password, UserRoleType.Operator)
            }

    @PostMapping("/courier")
    fun createCourier(@AuthenticationPrincipal user: User, @RequestBody form: CourierRegistrationForm) =
            form.createGenericUser { login, password ->
                UserLogic.create(user, login, password, UserRoleType.Courier)
            }


    data class ClientListFilter(val id: Int?)

    @GetMapping("/client")
    fun getClients(@AuthenticationPrincipal user: User, @RequestParam params: Map<String, String>): ResponseEntity<*> {
        val listParams = getListQueryParams<ClientListFilter>(params)
        val result = UserLogic.listClients(user)
        return result.response {
            ResponseEntity.ok().responseListRange(it, listParams.range)
        }

    }


}
