package com.robertas.storyapp

import com.robertas.storyapp.abstractions.IStoryService
import com.robertas.storyapp.models.network.StoryResponse
import com.robertas.storyapp.models.network.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody

class FakeStoryService(private val isErrorScenario: Boolean): IStoryService {
    override suspend fun postLogin(email: String, password: String): UserResponse {
        return DataDummy.generateUserResponseDummy(isErrorScenario)
    }

    override suspend fun register(name: String, email: String, password: String): UserResponse {
        return DataDummy.generateBaseResponseDummy(isErrorScenario)
    }

    override suspend fun postStory(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody
    ): StoryResponse {
        return DataDummy.generateStoryResponseDummy(isErrorScenario)
    }

    override suspend fun postStory(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody,
        lon: RequestBody
    ): StoryResponse {
        return DataDummy.generateStoryResponseDummy(isErrorScenario)
    }

    override suspend fun getAllStories(token: String, withLocation: Int): StoryResponse {
        return DataDummy.generateStoryResponseDummy(isErrorScenario)
    }

    override suspend fun getAllStories(
        token: String,
        withLocation: Int,
        page: Int,
        size: Int
    ): StoryResponse {
        return DataDummy.generateStoryResponseDummy(isErrorScenario)
    }
}