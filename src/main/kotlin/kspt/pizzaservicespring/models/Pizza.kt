package kspt.pizzaservicespring.models

import kspt.pizzaservicespring.repository.OrderPizzaRepository
import kspt.pizzaservicespring.repository.PizzaRepository
import kspt.pizzaservicespring.repository.RepositoryRegister
import javax.persistence.*


data class PizzaTopping(
        val id: Int,
        val name: String
)


data class Pizza(
        val id: Int,
        val name: String,
        val price: Int,
        val imageUrl: String,
        val toppings: List<PizzaTopping>
){
    companion object {
        val repository: PizzaRepository by RepositoryRegister
    }
}

@Entity
data class OrderPizza(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int = -1,

        @ManyToOne(cascade = [CascadeType.ALL])
        @JoinColumn(name = "order_id", referencedColumnName = "id")
        val order: Order,

        val pizzaId: Int
) {
    companion object {
        val repository: OrderPizzaRepository by RepositoryRegister
    }
}
