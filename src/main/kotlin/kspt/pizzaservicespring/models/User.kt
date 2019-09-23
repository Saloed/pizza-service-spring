package kspt.pizzaservicespring.models

import kspt.pizzaservicespring.repository.*
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import javax.persistence.*

enum class UserRoleType {
    Client, Manager, Operator, Courier
}

@Entity
@Inheritance
@Table(name = "_user")
sealed class User(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int = -1,
        open val login: String,
        open val password: String
): Authentication {
    abstract val role: UserRoleType

    @Transient
    private var isAuthenticated = false

    override fun getName() = login
    override fun getAuthorities() = listOf(GrantedAuthority {role.name})

    override fun isAuthenticated() = isAuthenticated
    override fun setAuthenticated(isAuthenticated: Boolean) {
        this.isAuthenticated = isAuthenticated
    }

    override fun getCredentials() = this
    override fun getPrincipal() = this
    override fun getDetails() = this


    companion object {
        val repository: UserRepository by RepositoryRegister
    }
}

@Entity
class Client(
        login: String,
        password: String,
        var address: String,
        var phone: String
) : User(login = login, password = password) {

    @Enumerated(EnumType.STRING)
    override val role = UserRoleType.Client

    companion object {
        val repository: ClientRepository by RepositoryRegister
    }

}

@Entity
class Manager(
        login: String,
        password: String,
        var restaurant: String
) : User(login = login, password = password) {
    @Enumerated(EnumType.STRING)
    override val role = UserRoleType.Manager

    companion object {
        val repository: ManagerRepository by RepositoryRegister
    }
}

@Entity
class Operator(
        login: String,
        password: String,
        var number: Int
) : User(login = login, password = password) {
    @Enumerated(EnumType.STRING)
    override val role = UserRoleType.Operator

    companion object {
        val repository: OperatorRepository by RepositoryRegister
    }
}

@Entity
class Courier(
        login: String,
        password: String
) : User(login = login, password = password) {
    @Enumerated(EnumType.STRING)
    override val role = UserRoleType.Courier

    companion object {
        val repository: CourierRepository by RepositoryRegister
    }
}

