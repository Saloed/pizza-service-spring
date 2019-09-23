package kspt.pizzaservicespring.repository

import kspt.pizzaservicespring.models.Payment
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository :JpaRepository<Payment, Int>
