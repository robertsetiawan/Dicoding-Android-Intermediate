package com.robertas.storyapp.repositories

import android.content.SharedPreferences
import com.robertas.storyapp.abstractions.IDomainMapper
import com.robertas.storyapp.abstractions.IStoryService
import com.robertas.storyapp.abstractions.UserRepository
import com.robertas.storyapp.models.domain.User
import com.robertas.storyapp.models.enums.CameraMode
import com.robertas.storyapp.models.enums.LanguageMode
import com.robertas.storyapp.models.network.UserNetwork
import com.robertas.storyapp.models.network.UserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class UserAccountRepository @Inject constructor(
    override val apiService: IStoryService,
    override val pref: SharedPreferences,
    override val domainMapper: IDomainMapper<UserNetwork, User>
) : UserRepository() {
    override suspend fun login(email: String, password: String): User? {
        val response: Response<UserResponse>

        withContext(Dispatchers.IO) {
            response = apiService.postLogin(email = email, password = password)
        }

        when (response.code()) {
            200 -> {
                val apiResponse = response.body()

                if (apiResponse?.error == true) {

                    throw Exception(apiResponse.message)

                } else {

                    return apiResponse?.data?.let { domainMapper.mapToEntity(it) }
                }
            }

            else -> throw Exception(getMessageFromApi(response))
        }
    }

    override suspend fun register(name: String, email: String, password: String): Boolean {
        val response: Response<UserResponse>

        withContext(Dispatchers.IO) {
            response = apiService.register(email = email, password = password, name = name)
        }

        when (response.code()) {
            201 -> {
                return if (response.body()?.error == false) {
                    true
                } else {
                    throw Exception(response.body()?.message)
                }
            }

            else -> throw Exception(getMessageFromApi(response))
        }
    }

    override fun isUserLoggedIn(): Boolean {
        val token = pref.getString(USER_TOKEN_KEY, null)
        val userId = pref.getString(USER_ID_KEY, null)
        val username = pref.getString(USER_NAME_KEY, null)

        return !(token.isNullOrBlank() || userId.isNullOrBlank() || username.isNullOrBlank())
    }

    override fun setLoggedInUser(user: User) {
        pref.edit().apply {

            putString(USER_ID_KEY, user.userId)

            putString(USER_NAME_KEY, user.name)

            putString(USER_TOKEN_KEY, user.token)

            apply()
        }
    }

    override fun logOut() {
        pref.edit().clear().apply()
    }

    private fun getMessageFromApi(response: Response<*>): String {
        val jsonObj = JSONObject(response.errorBody()?.charStream()?.readText().orEmpty())

        return jsonObj.getString("message").orEmpty()
    }

    override fun getCameraMode(): String {

        return when (pref.getString(CAMERA_KEY, "")) {
            "CameraX" -> CameraMode.CAMERA_X

            "System" -> CameraMode.SYSTEM

            else -> CameraMode.CAMERA_X
        }
    }

    override fun getLanguageMode(): String {
        return when (pref.getString(LANGUAGE_KEY, "")) {
            "id" -> LanguageMode.ID

            "en" -> LanguageMode.EN

            else -> LanguageMode.DEFAULT
        }
    }

    override fun setCameraMode(mode: String) {
        pref.edit().putString(
            CAMERA_KEY, when (mode) {
                "CameraX" -> CameraMode.CAMERA_X

                "System" -> CameraMode.SYSTEM

                else -> CameraMode.CAMERA_X
            }
        ).apply()
    }

    override fun setLanguageMode(mode: String) {
        pref.edit().putString(
            LANGUAGE_KEY, when (mode) {
                "id" -> LanguageMode.ID

                "en" -> LanguageMode.EN

                else -> LanguageMode.DEFAULT
            }
        ).apply()
    }

    companion object {
        const val USER_TOKEN_KEY = "user_token_key"

        const val USER_NAME_KEY = "user_name_key"

        const val USER_ID_KEY = "user_id_key"

        const val CAMERA_KEY = "camera_key"

        const val LANGUAGE_KEY = "language_key"
    }
}