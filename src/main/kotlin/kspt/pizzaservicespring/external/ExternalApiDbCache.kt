package kspt.pizzaservicespring.external

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kspt.pizzaservicespring.repository.RepositoryRegister
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import java.util.*
import java.util.concurrent.TimeUnit
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

data class CachedResourceStr(
        val updatedAt: Date,
        val resource: String
)

@Entity
data class ApiCache(
        @Id val resource: String,
        @LastModifiedDate val updatedAt: Date,
        @Column(columnDefinition = "TEXT") val data: String
) {
    companion object {
        val repository: ApiCacheRepository by RepositoryRegister
    }
}

interface ApiCacheRepository : JpaRepository<ApiCache, String>


object ExternalApiDbCache {
    inline fun <reified T : Any> get(resource: String, onExpired: () -> T): T {
        val cachedData = ApiCache.repository.findByIdOrNull(resource)?.let { CachedResourceStr(it.updatedAt, it.data) }

        val mapper = jacksonObjectMapper()
        val oneWeekEarlier = Date(Date().time - TimeUnit.DAYS.toMillis(7))
        if (cachedData == null || cachedData.updatedAt.before(oneWeekEarlier)) {
            val newData = onExpired()
            val dataStr = mapper.writeValueAsString(newData)
            val entry = ApiCache(resource, Date(), dataStr)
            ApiCache.repository.save(entry)
            return newData
        }
//        val resultListType = TypeToken.getParameterized(List::class.java, T::class.java).type
        return mapper.readValue(cachedData.resource, T::class.java)
    }
}
