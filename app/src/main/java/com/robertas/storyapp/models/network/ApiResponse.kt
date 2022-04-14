package com.robertas.storyapp.models.network

abstract class ApiResponse<T> {
    abstract val message: String

    abstract val error: Boolean

    abstract val data: T?
}