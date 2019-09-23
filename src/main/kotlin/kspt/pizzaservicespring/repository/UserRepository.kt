package kspt.pizzaservicespring.repository

import kspt.pizzaservicespring.models.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.transaction.annotation.Transactional


@NoRepositoryBean
interface UserBaseRepository<T : User> : JpaRepository<T, Int>

@Transactional
interface UserRepository : UserBaseRepository<User> {
    fun findByLogin(login: String): User?
}

@Transactional
interface ClientRepository : UserBaseRepository<Client>

@Transactional
interface CourierRepository : UserBaseRepository<Courier>

@Transactional
interface OperatorRepository : UserBaseRepository<Operator>

@Transactional
interface ManagerRepository : UserBaseRepository<Manager>
