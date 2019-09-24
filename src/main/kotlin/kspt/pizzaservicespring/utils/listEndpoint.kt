package kspt.pizzaservicespring.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.ResponseEntity


enum class SortOrder {
    ASC, DESC
}

data class Sort(val field: String, val order: SortOrder)

data class ListQueryParams<T>(val filter: T?, val range: IntRange?, val sort: List<Sort>?)

inline fun <reified T> getListQueryParams(params: Map<String, String>): ListQueryParams<T> {
    val mapper = jacksonObjectMapper()
//    val stringListType = TypeToken.getParameterized(List::class.java, String::class.java).type
//    val intListType = TypeToken.getParameterized(List::class.java, Integer::class.java).type
    val filter = params["filter"]?.let { mapper.readValue<T>(it) }
    val range = params["range"]?.let { mapper.readValue<List<Int>>(it) }
            ?.let { (from, to) -> IntRange(from, to) }
    val sort = params["sort"]?.let { mapper.readValue<List<String>>(it) }
            ?.chunked(2) { (field, order) ->
                Sort(field, SortOrder.valueOf(order))
            }

    return ListQueryParams(filter, range, sort)
}

inline fun <reified T> ResponseEntity.BodyBuilder.responseListRange(data: List<T>, range: IntRange?) = when {
    data.isEmpty() -> {
        contentRange(0..0L, 0)
        body(data)
    }
    range == null -> {
        contentRange(data.indices.toLong(), data.size.toLong())
        body(data)
    }
    else -> {
        val realRange = range.intersect(data.indices)
        contentRange(realRange.toLong(), data.size.toLong())
        val result = data.subList(realRange.first, realRange.endInclusive + 1)
        body(result)
    }
}
//
//inline fun <reified T : Any> Sort.buildComparator(): Comparator<T> {
//    val property = T::class.memberProperties.find { it.name == field }
//        ?: throw IllegalArgumentException("Unknown sort key: $field")
//
//}
//
//inline fun <reified T : Any> ApplicationCall.responseListSort(data: List<T>, sort: Sort): List<T> {
//    val comparator = sort.buildComparator<T>()
//    return data.sortedWith(comparator)
//}
//
//inline fun <reified T : Any> ApplicationCall.responseListSort(data: List<T>, sorters: List<Sort>?): List<T> {
//    if (sorters == null || sorters.isEmpty()) return data
//    var newData = data
//    for (sort in sorters) {
//        newData = responseListSort(newData, sort)
//    }
//    return newData
//}
