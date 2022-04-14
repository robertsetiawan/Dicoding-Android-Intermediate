package com.robertas.storyapp.abstractions

import android.content.SharedPreferences

abstract class BaseRepository <T, V> {
    abstract val apiService: IStoryService

    abstract val pref: SharedPreferences

    abstract val domainMapper: IDomainMapper<V, T>
}