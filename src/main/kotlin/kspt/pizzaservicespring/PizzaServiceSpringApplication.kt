package kspt.pizzaservicespring

import kspt.pizzaservicespring.models.Client
import kspt.pizzaservicespring.models.User
import kspt.pizzaservicespring.repository.*
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PizzaServiceSpringApplication(
        val userRepository: UserRepository,
        val clientRepository: ClientRepository,
        val orderRepository: OrderRepository,
        val paymentRepository: PaymentRepository,
        val pizzaRepository: PizzaRepository,
        val promoRepository: PromoRepository,
        val promoClientRepository: PromoClientRepository
) : CommandLineRunner {


    override fun run(vararg args: String?) {
        RepositoryRegister.init(
                UserRepository::class to userRepository,
                ClientRepository::class to clientRepository,
                OrderRepository::class to orderRepository,
                PaymentRepository::class to paymentRepository,
                PizzaRepository::class to pizzaRepository,
                PromoRepository::class to promoRepository,
                PromoClientRepository::class to promoClientRepository
        )
        println("users")
        Client.repository.save(Client("name", "[password]", "address", "phone"))
        User.repository.findAll().map { println("$it") }
    }

}

fun main(args: Array<String>) {
    runApplication<PizzaServiceSpringApplication>(*args)
}
