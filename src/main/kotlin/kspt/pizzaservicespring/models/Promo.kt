package kspt.pizzaservicespring.models

import kspt.pizzaservicespring.repository.PromoClientRepository
import kspt.pizzaservicespring.repository.PromoRepository
import kspt.pizzaservicespring.repository.RepositoryRegister
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.util.*
import javax.persistence.*


enum class PromoStatus {
    NEW, ACTIVE, FINISHED, CLOSED
}


enum class PromoEffect {
    DISCOUNT_5, DISCOUNT_10, DISCOUNT_15
}

@Entity
data class Promo(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int = -1,

        @Enumerated(EnumType.STRING)
        val status: PromoStatus,

        val result: String?,

        @ManyToOne(cascade = [CascadeType.ALL])
        @JoinColumn(name = "manager_id", referencedColumnName = "id")
        val manager: Manager,

        val description: String,

        @Enumerated(EnumType.STRING)
        val effect: PromoEffect,

        @CreatedDate
        @Temporal(TemporalType.TIMESTAMP)
        val createdAt: Date = Date(),

        @LastModifiedDate
        @Temporal(TemporalType.TIMESTAMP)
        val updatedAt: Date = Date(),

        @OneToMany(mappedBy = "promo")
        val clients: List<PromoClient> = arrayListOf()

) {
    companion object {
        val repository: PromoRepository by RepositoryRegister
    }
}

enum class PromoClientStatus {
    NOTINFORMED,
    PROCESSING,
    INFORMED
}

@Entity
data class PromoClient(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int = -1,

        @ManyToOne(cascade = [CascadeType.ALL])
        @JoinColumn(name = "operator_id", referencedColumnName = "id")
        val operator: Operator?,

        @ManyToOne(cascade = [CascadeType.ALL])
        @JoinColumn(name = "promo_id", referencedColumnName = "id")
        val promo: Promo,

        @ManyToOne(cascade = [CascadeType.ALL])
        @JoinColumn(name = "client_id", referencedColumnName = "id")
        val client: Client,

        @Enumerated(EnumType.STRING)
        val status: PromoClientStatus,

        @CreatedDate
        @Temporal(TemporalType.TIMESTAMP)
        val createdAt: Date = Date(),

        @LastModifiedDate
        @Temporal(TemporalType.TIMESTAMP)
        val updatedAt: Date = Date()
) {
    companion object {
        val repository: PromoClientRepository by RepositoryRegister
    }
}
