package com.robertas.storyapp

import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.domain.User
import com.robertas.storyapp.models.network.*

object DataDummy {
    fun generateUserDummy(): User {
        return User(
            userId = "abcd",
            name = "robertus agung",
            token = "quwuhibfasidf12",
        )
    }

    fun generateDummyStories(withLocation: Boolean): List<Story> {

        val result: ArrayList<Story> = arrayListOf()

        if (withLocation) {
            for (i in 0..4) {
                result.add(
                    Story(
                        id = "id-$i",
                        name = "name=$i",
                        createdAt = "08-Jan-2022 06:34:18",
                        photoUrl = "https://www.google.com/search?q=$i",
                        lat = 0.0,
                        lon = 0.0,
                        description = "description-$i"
                    )
                )
            }
        } else {
            for (i in 0..4) {
                result.add(
                    Story(
                        id = "id-$i",
                        name = "name=$i",
                        createdAt = "08-Jan-2022 06:34:18",
                        photoUrl = "https://www.google.com/search?q=$i",
                        lat = null,
                        lon = null,
                        description = "description-$i"
                    )
                )
            }
        }
        return result
    }

    fun generateUserResponseDummy(isError: Boolean): UserResponse {

        val user = generateUserDummy()

        return UserResponse(
            message = "error is $isError",
            isError,
            if (isError) null else UserNetwork(userId = user.userId, name = user.name, token = user.token)
        )
    }

    fun generateStoryResponseDummy(isError: Boolean): StoryResponse {
        val listStory = generateDummyStories(false)

        val listStoryNetwork = listStory.map {
            StoryNetwork(
                id = it.id,
                name = it.name,
                description = it.description,
                photoUrl = it.photoUrl,
                createdAt = "2022-01-08T06:34:18.598Z",
                lat = it.lat,
                lon = it.lon
            )
        }.toList()

        return if(!isError) {
            StoryResponse("ok", false, listStoryNetwork)
        } else {
            StoryResponse("error", true, null)
        }
    }

    fun generateBaseResponseDummy(): UserResponse {
        return UserResponse("ok", true, null)
    }

    fun generateDummyToken(): String {
        return "Bearer token1234567909009102i310391"
    }
}