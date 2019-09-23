package kspt.pizzaservicespring.web

import kspt.pizzaservicespring.logic.UserLogic
import kspt.pizzaservicespring.security.SecurityConstants
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Controller
class ClientRegistrationController {
    @PostMapping(SecurityConstants.CLIENT_REGISTRATION_URL)
    fun register(@RequestBody form: ClientRegistrationForm): ResponseEntity<RegistrationErrorResponse> {
        return form.createGenericUser { login, password ->
            UserLogic.createClient(login, password)
        }
    }
}
