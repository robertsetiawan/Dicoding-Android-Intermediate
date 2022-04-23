package com.robertas.storyapp.abstractions

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.google.android.gms.maps.model.LatLng
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.enums.NetworkResult
import com.robertas.storyapp.models.network.StoryNetwork
import kotlinx.coroutines.flow.Flow
import java.io.File

abstract class StoryRepository: BaseRepository<Story, StoryNetwork>() {

    abstract val storyDatabase: StoryDatabase

    abstract suspend fun postStory(token: String,file: File, description: String, rotation: Float): Flow<NetworkResult<Boolean>>

    abstract suspend fun postStory(token: String, file: File, description: String, rotation: Float, latLng: LatLng): Flow<NetworkResult<Boolean>>

    abstract fun getAllStories(token: String, isLocationOnly: Boolean): Flow<NetworkResult<List<Story>>>

    abstract fun getAllStories(token: String): Flow<PagingData<Story>>
}