package kspt.pizzaservicespring.web


abstract class RegistrationForm {
    abstract val username: String
    abstract val password: String
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
