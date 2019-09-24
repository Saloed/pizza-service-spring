package kspt.pizzaservicespring

import kspt.pizzaservicespring.external.ApiCacheRepository
import kspt.pizzaservicespring.repository.*
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.filter.CommonsRequestLoggingFilter


@SpringBootApplication
class PizzaServiceSpringApplication(
        val userRepository: UserRepository,
        val clientRepository: ClientRepository,
        val operatorRepository: OperatorRepository,
        val managerRepository: ManagerRepository,
        val courierRepository: CourierRepository,
        val orderRepository: OrderRepository,
        val paymentRepository: PaymentRepository,
        val pizzaRepository: OrderPizzaRepository,
        val promoRepository: PromoRepository,
        val promoClientRepository: PromoClientRepository,
        val apiCacheRepository: ApiCacheRepository
) : CommandLineRunner {

    @Bean
    fun requestLoggingFilter(): CommonsRequestLoggingFilter {
        val loggingFilter = CommonsRequestLoggingFilter()
        loggingFilter.setIncludeClientInfo(true)
        loggingFilter.setIncludeQueryString(true)
        loggingFilter.setIncludePayload(true)
        loggingFilter.setIncludeHeaders(false)
        return loggingFilter
    }

    override fun run(vararg args: String?) {
        RepositoryRegister.init(
                UserRepository::class to userRepository,
                ClientRepository::class to clientRepository,
                ManagerRepository::class to managerRepository,
                OperatorRepository::class to operatorRepository,
                CourierRepository::class to courierRepository,
                OrderRepository::class to orderRepository,
                PaymentRepository::class to paymentRepository,
                OrderPizzaRepository::class to pizzaRepository,
                PromoRepository::class to promoRepository,
                PromoClientRepository::class to promoClientRepository,
                ApiCacheRepository::class to apiCacheRepository,
                PizzaRepository::class to PizzaRepository
        )
        DummyInitializer.init()

    }

}

fun main(args: Array<String>) {
    runApplication<PizzaServiceSpringApplication>(*args)
}
