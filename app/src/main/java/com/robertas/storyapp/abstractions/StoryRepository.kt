package com.robertas.storyapp.abstractions

import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.network.StoryNetwork
import java.io.File


abstract class StoryRepository: BaseRepository<Story, StoryNetwork>() {

    abstract suspend fun postStory(file: File, description: String, rotation: Float): Boolean

    abstract suspend fun getAllStories(): List<Story>?
}