package kspt.pizzaservicespring.repository

import kspt.pizzaservicespring.models.OrderPizza
import kspt.pizzaservicespring.models.Pizza
import org.springframework.data.jpa.repository.JpaRepository

interface OrderPizzaRepository : JpaRepository<OrderPizza, Int>
