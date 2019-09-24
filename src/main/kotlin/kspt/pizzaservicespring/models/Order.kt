package kspt.pizzaservicespring.models


import kspt.pizzaservicespring.repository.OrderRepository
import kspt.pizzaservicespring.repository.RepositoryRegister
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.util.*
import javax.persistence.*

enum class OrderStatus {
    NEW,
    APPROVED,
    PROCESSING,
    READY,
    SHIPPING,
    CLOSED,
    CANCELED,
    DRAFT;

    companion object {
        val forManager = listOf(APPROVED, PROCESSING, READY)
        val forOperator = listOf(NEW)
        val forCourier = listOf(READY, SHIPPING)
    }
}

@Entity
@Table(name = "_order")
data class Order(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int = -1,

        @Enumerated(EnumType.STRING)
        val status: OrderStatus,

        val cost: Int,

        val isActive: Boolean,

        @ManyToOne(cascade = [CascadeType.ALL])
        @JoinColumn(name = "client_id", referencedColumnName = "id")
        val client: Client,

        @ManyToOne(cascade = [CascadeType.ALL])
        @JoinColumn(name = "manager_id", referencedColumnName = "id")
        val manager: Manager? = null,

        @ManyToOne(cascade = [CascadeType.ALL])
        @JoinColumn(name = "operator_id", referencedColumnName = "id")
        val operator: Operator? = null,

        @ManyToOne(cascade = [CascadeType.ALL])
        @JoinColumn(name = "courier_id", referencedColumnName = "id")
        val courier: Courier? = null,

        @OneToOne(mappedBy = "order")
        val payment: Payment? = null,

        @ManyToOne(cascade = [CascadeType.ALL])
        @JoinColumn(name = "promo_id", referencedColumnName = "id")
        val promo: Promo? = null,

        @OneToMany(mappedBy = "order")
        val pizza: MutableList<OrderPizza> = arrayListOf(),

        @CreatedDate
        @Temporal(TemporalType.TIMESTAMP)
        val createdAt: Date = Date(),

        @LastModifiedDate
        @Temporal(TemporalType.TIMESTAMP)
        val updatedAt: Date = Date()
) {

    fun fetchPizza() = Pizza.repository.qetPizzaFromApi(pizza.map { it.pizzaId })

    companion object {
        val repository: OrderRepository by RepositoryRegister
    }
}

