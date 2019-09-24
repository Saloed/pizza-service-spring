package kspt.pizzaservicespring

import kspt.pizzaservicespring.external.ApiCacheRepository
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
        val pizzaRepository: OrderPizzaRepository,
        val promoRepository: PromoRepository,
        val promoClientRepository: PromoClientRepository,
        val apiCacheRepository: ApiCacheRepository
) : CommandLineRunner {


    override fun run(vararg args: String?) {
        RepositoryRegister.init(
                UserRepository::class to userRepository,
                ClientRepository::class to clientRepository,
                OrderRepository::class to orderRepository,
                PaymentRepository::class to paymentRepository,
                OrderPizzaRepository::class to pizzaRepository,
                PromoRepository::class to promoRepository,
                PromoClientRepository::class to promoClientRepository,
                ApiCacheRepository::class to apiCacheRepository,
                PizzaRepository::class to PizzaRepository
        )
    }

}

fun main(args: Array<String>) {
    runApplication<PizzaServiceSpringApplication>(*args)
}
