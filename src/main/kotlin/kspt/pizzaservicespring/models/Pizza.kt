package kspt.pizzaservicespring.models

import kspt.pizzaservicespring.repository.PizzaRepository
import kspt.pizzaservicespring.repository.RepositoryRegister
import kspt.pizzaservicespring.repository.UserRepository
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToMany

// todo: rewrite on external api

@Entity
data class PizzaTopping(
        @Id
        val id: Int = -1,
        val name: String
)

@Entity
data class Pizza(
        @Id
        val id: Int = -1,
        val name: String,
        val price: Int,
        val imageUrl: String,

        @ManyToMany
        val toppings: List<PizzaTopping>
){
    companion object {
        val repository: PizzaRepository by RepositoryRegister
    }
}
