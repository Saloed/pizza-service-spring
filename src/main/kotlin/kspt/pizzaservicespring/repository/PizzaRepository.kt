package kspt.pizzaservicespring.repository

import kspt.pizzaservicespring.models.Pizza
import kspt.pizzaservicespring.models.PizzaTopping
import kspt.pizzaservicespring.external.PizzaApi

object PizzaRepository {
    fun qetPizzaFromApi(ids: List<Int> = emptyList()): List<Pizza> {
        val data = PizzaApi.query()
        val pizza = data.map {
            val toppings = it.toppings.map { PizzaTopping(it.id, it.name) }
            Pizza(it.id, it.name, it.price, it.imageUrl, toppings)
        }
        if (ids.isEmpty()) return pizza
        val idSet = ids.toSet()
        return pizza.filter { it.id in idSet }
    }

     fun get(id: Int): Pizza? = this.qetPizzaFromApi().find { it.id == id }

}