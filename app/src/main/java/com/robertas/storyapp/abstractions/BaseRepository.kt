package com.robertas.storyapp.abstractions

abstract class BaseRepository <T, V> {
    abstract val apiService: IStoryService

    abstract val networkMapper: IDomainMapper<V, T>
}