package com.robertas.storyapp.repositories

import android.content.SharedPreferences
import com.robertas.storyapp.abstractions.IDomainMapper
import com.robertas.storyapp.abstractions.IStoryService
import com.robertas.storyapp.abstractions.StoryRepository
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.network.StoryNetwork
import com.robertas.storyapp.models.network.StoryResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.File
import javax.inject.Inject

class UserStoryRepository @Inject constructor(
    override val apiService: IStoryService,
    override val domainMapper: IDomainMapper<StoryNetwork, Story>,
    override val pref: SharedPreferences
) : StoryRepository() {
    override suspend fun postStory(file: File, description: String): Boolean? {
        TODO("Not yet implemented")
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

            return if (response.body()?.error == false) {
                response.body()?.data?.map { it -> domainMapper.mapToEntity(it) }.orEmpty()
            } else {
                null
            }
        }
    }

    companion object {
        const val IN_SESSION_TIMEOUT = "Login Session has ended"
    }
}