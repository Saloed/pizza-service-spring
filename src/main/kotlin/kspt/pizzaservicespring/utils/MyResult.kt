package kspt.pizzaservicespring.utils

sealed class MyResult<T> {
    data class Success<T>(val data: T) : MyResult<T>()
    data class Error<T>(val message: String) : MyResult<T>()
}

