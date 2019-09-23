package kspt.pizzaservicespring.repository

import org.springframework.data.jpa.repository.JpaRepository
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


object RepositoryRegister {

    lateinit var register: Map<KClass<*>, JpaRepository<*, *>>

    fun init(vararg repos: Pair<KClass<*>, JpaRepository<*, *>>) {
        register = mapOf(*repos)
    }

    inline operator fun <reified T : JpaRepository<*, *>> getValue(companion: Any, property: KProperty<*>): T =
            register[T::class]  as? T ?: throw IllegalStateException("Repository ${T::class.java} not found")

}

