package com.robertas.storyapp.repositories

import android.content.SharedPreferences
import com.robertas.storyapp.abstractions.IDomainMapper
import com.robertas.storyapp.abstractions.IStoryService
import com.robertas.storyapp.abstractions.StoryRepository
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.network.StoryNetwork
import com.robertas.storyapp.models.network.StoryResponse
import com.robertas.storyapp.utils.reduceFileImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import javax.inject.Inject

class UserStoryRepository @Inject constructor(
    override val apiService: IStoryService,
    override val domainMapper: IDomainMapper<StoryNetwork, Story>,
    override val pref: SharedPreferences
) : StoryRepository() {
    override suspend fun postStory(file: File, description: String): Boolean {

        val token = pref.getString(UserAccountRepository.USER_TOKEN_KEY, "")

        val authToken = "Bearer $token"

        val response: Response<StoryResponse>

        if (token.isNullOrBlank()) {

            throw Exception(IN_SESSION_TIMEOUT)

        } else {

            withContext(Dispatchers.IO){

                val reducedFile = reduceFileImage(file)

                val desc = description.toRequestBody("text/plain".toMediaType())

                val img = file.asRequestBody("image/jpeg".toMediaTypeOrNull())

                val multiPart = MultipartBody.Part.createFormData(
                    "photo",
                    reducedFile.name,
                    img)

                response = apiService.postStory(authToken, multiPart, desc)
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
    }

    override suspend fun getAllStories(): List<Story>? {
        val token = pref.getString(UserAccountRepository.USER_TOKEN_KEY, "")

        val authToken = "Bearer $token"

        val response: Response<StoryResponse>

        if (token.isNullOrBlank()) {
            throw Exception(IN_SESSION_TIMEOUT)
        } else {
            withContext(Dispatchers.IO) {
                response = apiService.getAllStories(authToken)
            }

            when (response.code()) {
                200 -> {
                    return if (response.body()?.error == false) {
                        response.body()?.data?.map { it -> domainMapper.mapToEntity(it) }.orEmpty()
                    } else {
                        null
                    }
                }

                else -> throw Exception(getMessageFromApi(response))
            }
        }
    }

    private fun getMessageFromApi(response: Response<*>): String {
        val jsonObj = JSONObject(response.errorBody()?.charStream()?.readText().orEmpty())

        return jsonObj.getString("message").orEmpty()
    }

    companion object {
        const val IN_SESSION_TIMEOUT = "Login Session has ended"
    }
}