package kspt.pizzaservicespring.models

import kspt.pizzaservicespring.repository.PaymentRepository
import kspt.pizzaservicespring.repository.RepositoryRegister
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.util.*
import javax.persistence.*


enum class PaymentType {
    CASH, CARD
}

@Entity
data class Payment(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int = -1,

        @OneToOne(cascade = [CascadeType.ALL])
        @JoinColumn(name = "order_id", referencedColumnName = "id")
        val order: Order,

        @Enumerated(EnumType.STRING)
        val type: PaymentType,

        val amount: Int,

        val cardTransaction: String?,

        @CreatedDate
        @Temporal(TemporalType.TIMESTAMP)
        val createdAt: Date = Date(),

        @LastModifiedDate
        @Temporal(TemporalType.TIMESTAMP)
        val updatedAt: Date = Date()
) {
    companion object {
        val repository: PaymentRepository by RepositoryRegister
    }
}
