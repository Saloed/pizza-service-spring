import kspt.pizzaservicespring.models.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

object DummyInitializer {

    private val dummyUsers = listOf(
            Client("client", "password".encode(), "Address", "phone"),
            Manager("manager", "password".encode(), "Restaurant №1"),
            Operator("operator", "password".encode(), 17),
            Courier("courier", "password".encode())
    )

    private fun String.encode() = BCryptPasswordEncoder().encode(this)

    fun init() = dummyUsers.map { createUserIfNotFound(it) }

    private fun createUserIfNotFound(user: User) = User.repository.findByLogin(user.login) ?: User.repository.save(user)
}
