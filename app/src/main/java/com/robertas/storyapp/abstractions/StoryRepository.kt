package com.robertas.storyapp.abstractions

import android.content.Context
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.network.StoryNetwork
import java.io.File


abstract class StoryRepository: BaseRepository<Story, StoryNetwork>() {

    abstract suspend fun postStory(file: File, description: String): Boolean?

    abstract suspend fun getAllStories(): List<Story>?
}