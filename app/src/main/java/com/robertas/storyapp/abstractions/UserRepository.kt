package com.robertas.storyapp.abstractions

import com.robertas.storyapp.models.domain.User
import com.robertas.storyapp.models.network.UserNetwork

abstract class UserRepository: BaseRepository<User, UserNetwork>() {

    abstract suspend fun login(email: String, password: String): User?

    abstract suspend fun register(name: String, email: String, password: String): Boolean

    abstract fun isUserLoggedIn(): Boolean

    abstract fun setLoggedInUser(user: User)

    abstract fun getCameraMode(): String

    abstract fun getLanguageMode(): String

    abstract fun setCameraMode(mode: String)

    abstract fun setLanguageMode(mode: String)

    abstract fun logOut()
}