package com.robertas.storyapp.abstractions

import com.robertas.storyapp.models.domain.User
import com.robertas.storyapp.models.network.UserNetwork

abstract class UserRepository: BaseRepository<User, UserNetwork>() {

    abstract suspend fun postLogin(email: String, password: String): User?

    abstract suspend fun register(name: String, email: String, password: String): Boolean

    abstract fun isUserLoggedIn(): Boolean

    abstract fun setLoggedInUser(user: User)

    abstract fun logOut()
}