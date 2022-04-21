package com.robertas.storyapp.abstractions

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.google.android.gms.maps.model.LatLng
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.network.StoryNetwork
import kotlinx.coroutines.flow.Flow
import java.io.File

abstract class StoryRepository: BaseRepository<Story, StoryNetwork>() {

    abstract val storyDatabase: StoryDatabase

    abstract suspend fun postStory(file: File, description: String, rotation: Float): Boolean

    abstract suspend fun postStory(file: File, description: String, rotation: Float, latLng: LatLng): Boolean

    abstract suspend fun getAllStories(withLocation: Boolean): List<Story>

    abstract fun getAllStories(): Flow<PagingData<Story>>
}