package com.robertas.storyapp.abstractions

interface IOnItemClickListener<T, V> {
    fun onClick(item: T, binding: V)
}