package kspt.pizzaservicespring.web

import kspt.pizzaservicespring.models.User
import kspt.pizzaservicespring.utils.UserValidator
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

data class RegistrationErrorResponse(val message: String)

abstract class RegistrationForm {
    abstract val username: String
    abstract val password: String

    fun createGenericUser(
            userCreator:  (login: String, password: String) -> User?
    ): ResponseEntity<RegistrationErrorResponse> {
        val error = when {
            !UserValidator.passwordValid(password) -> "Password should be at least 6 characters long"
            !UserValidator.loginValid(username) -> "Login should be at least 4 characters long and consists of digits, letters, dots or underscores"
            User.repository.findByLogin(username) != null -> "User with the following login is already registered"
            else -> null
        }
        if (error != null) {
            val message = RegistrationErrorResponse(error)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message)
        }

        val passwordHash = BCryptPasswordEncoder().encode(password)
        val newUser = try {
            userCreator(username, passwordHash)
        } catch (e: Throwable) {
            LoggerFactory.getLogger(this::class.java).error("Failed to register user", e)
            val error = "Failed to register"
            val message = RegistrationErrorResponse(error)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message)
        }

        if (newUser == null) {
            val error = "Failed to register"
            val message = RegistrationErrorResponse(error)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message)
        }
        return ResponseEntity.status(HttpStatus.OK).body(RegistrationErrorResponse(""))
    }
}

data class ClientRegistrationForm(
        override val username: String,
        override val password: String,
        val address: String,
        val phone: String
) : RegistrationForm()

data class ManagerRegistrationForm(
        override val username: String,
        override val password: String,
        val restaurant: String
) : RegistrationForm()

data class OperatorRegistrationForm(
        override val username: String,
        override val password: String,
        val number: Int
) : RegistrationForm()

data class CourierRegistrationForm(
        override val username: String,
        override val password: String
) : RegistrationForm()
