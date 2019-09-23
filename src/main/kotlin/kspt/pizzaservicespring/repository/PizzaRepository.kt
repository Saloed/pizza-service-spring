package kspt.pizzaservicespring.repository

import kspt.pizzaservicespring.models.Pizza
import org.springframework.data.jpa.repository.JpaRepository

interface PizzaRepository : JpaRepository<Pizza, Int>
