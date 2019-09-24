package kspt.pizzaservicespring.repository

import org.springframework.data.jpa.repository.JpaRepository
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


object RepositoryRegister {

    lateinit var register: Map<KClass<*>, Any>

    fun init(vararg repos: Pair<KClass<*>, Any>) {
        register = mapOf(*repos)
    }

    inline operator fun <reified T> getValue(companion: Any, property: KProperty<*>): T =
            register[T::class]  as? T ?: throw IllegalStateException("Repository ${T::class.java} not found")

}

