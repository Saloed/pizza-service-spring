package kspt.pizzaservicespring.external

import kspt.pizzaservicespring.models.Pizza
import kspt.pizzaservicespring.models.PizzaTopping
import org.springframework.web.client.RestTemplate


object PizzaApi {

    private data class ApiPrice(val RefID: String, val Size: String, val PriceDelivery: Int)
    private data class ApiTopping(val ID: Int, val Name: String)
    private data class ApiPizza(
            val ID: Int,
            val Name: String,
            val MediaDetailed: String,
            val Toppings: List<ApiTopping>,
            val Prices: List<ApiPrice>
    )

    private const val url = "https://api.dominos.is/api/pizza?lang=en"
    private const val mediaPrefixUrl = "https://www.dominos.is"


    fun query(): List<Pizza> {
        return ExternalApiDbCache.get("pizza") {
            val data = RestTemplate().getForObject(url, Array<ApiPizza>::class.java)
                    ?: throw IllegalStateException("Error when requesting pizza api")
            val dataWithMediaUrl = data.map { it.copy(MediaDetailed = "$mediaPrefixUrl${it.MediaDetailed}") }
            dataWithMediaUrl.map {
                val toppings = it.Toppings.map { PizzaTopping(it.ID, it.Name) }
                val price = it.Prices.last()
                Pizza(it.ID, it.Name, price.PriceDelivery, it.MediaDetailed, toppings)
            }.toTypedArray()
        }.toList()
    }

}
