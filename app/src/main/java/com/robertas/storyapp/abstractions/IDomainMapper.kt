package com.robertas.storyapp.abstractions

interface IDomainMapper <T, V> {
    fun mapToEntity(source: T): V
}