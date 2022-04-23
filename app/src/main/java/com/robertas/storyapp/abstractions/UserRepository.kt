package com.robertas.storyapp.abstractions

import android.content.SharedPreferences
import com.robertas.storyapp.models.domain.User
import com.robertas.storyapp.models.enums.NetworkResult
import com.robertas.storyapp.models.network.UserNetwork
import kotlinx.coroutines.flow.Flow

abstract class UserRepository: BaseRepository<User, UserNetwork>() {

    abstract val pref: SharedPreferences

    abstract suspend fun login(email: String, password: String): Flow<NetworkResult<User?>>

    abstract suspend fun register(name: String, email: String, password: String): Flow<NetworkResult<Boolean>>

    abstract fun isUserLoggedIn(): Boolean

    abstract fun setLoggedInUser(user: User)

    abstract fun getUserToken(): String?

    abstract fun getBearerToken(): String

    abstract fun getCameraMode(): String

    abstract fun getLanguageMode(): String

    abstract fun setCameraMode(mode: String)

    abstract fun setLanguageMode(mode: String)

    abstract fun logOut()
}