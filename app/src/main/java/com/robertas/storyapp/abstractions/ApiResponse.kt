package com.robertas.storyapp.abstractions

abstract class ApiResponse<T>{

    abstract val message: String

    abstract val error: Boolean

    abstract val data: T?
}