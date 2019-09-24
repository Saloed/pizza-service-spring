package kspt.pizzaservicespring.utils

import org.springframework.http.ResponseEntity

sealed class MyResult<T> {
    data class Success<T>(val data: T) : MyResult<T>()
    data class Error<T>(val message: String) : MyResult<T>()

    fun response(onSuccess: (T) -> ResponseEntity<T> = { ResponseEntity.ok(it) }): ResponseEntity<*> = when (this) {
        is Error -> ResponseEntity.badRequest().body(this)
        is Success -> onSuccess(data)
    }
}
