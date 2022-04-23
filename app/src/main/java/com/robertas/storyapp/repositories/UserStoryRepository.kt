package com.robertas.storyapp.repositories

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.android.gms.maps.model.LatLng
import com.robertas.storyapp.abstractions.IDomainMapper
import com.robertas.storyapp.abstractions.IStoryService
import com.robertas.storyapp.abstractions.StoryDatabase
import com.robertas.storyapp.abstractions.StoryRepository
import com.robertas.storyapp.data.StoryRemoteMediator
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.enums.NetworkResult
import com.robertas.storyapp.models.network.StoryNetwork
import com.robertas.storyapp.models.network.StoryResponse
import com.robertas.storyapp.utils.reduceThenRotateFileImage
import com.robertas.storyapp.utils.wrapEspressoIdlingResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class UserStoryRepository @Inject constructor(
    override val apiService: IStoryService,
    override val networkMapper: IDomainMapper<StoryNetwork, Story>,
    override val storyDatabase: StoryDatabase,
) : StoryRepository() {
    override suspend fun postStory(
        token: String,
        file: File,
        description: String,
        rotation: Float
    ): Flow<NetworkResult<Boolean>> = flow {

        wrapEspressoIdlingResource {

            emit(NetworkResult.Loading)

            val reducedFile = reduceThenRotateFileImage(file, rotation)

            val desc = description.toRequestBody("text/plain".toMediaType())

            val img = file.asRequestBody("image/jpeg".toMediaTypeOrNull())

            val multiPart = MultipartBody.Part.createFormData(
                "photo",
                reducedFile.name,
                img
            )

            try {
                val response: StoryResponse = apiService.postStory(token, multiPart, desc)

                if (response.error) {
                    emit(NetworkResult.Error(response.message))
                } else {
                    emit(NetworkResult.Success(!response.error))
                }
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message.toString()))
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun postStory(
        token: String,
        file: File,
        description: String,
        rotation: Float,
        latLng: LatLng
    ): Flow<NetworkResult<Boolean>> = flow {

        wrapEspressoIdlingResource {

            emit(NetworkResult.Loading)

            val reducedFile = reduceThenRotateFileImage(file, rotation)

            val desc = description.toRequestBody("text/plain".toMediaType())

            val lat = latLng.latitude.toString().toRequestBody("text/plain".toMediaType())

            val lon = latLng.longitude.toString().toRequestBody("text/plain".toMediaType())

            val img = file.asRequestBody("image/jpeg".toMediaTypeOrNull())

            val multiPart = MultipartBody.Part.createFormData(
                "photo",
                reducedFile.name,
                img
            )

            try {
                val response: StoryResponse = apiService.postStory(token, multiPart, desc, lat, lon)

                if (response.error) {
                    emit(NetworkResult.Error(response.message))
                } else {
                    emit(NetworkResult.Success(!response.error))
                }

            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message.toString()))
            }

        }

    }.flowOn(Dispatchers.IO)

    override fun getAllStories(token: String): Flow<PagingData<Story>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(token, storyDatabase, apiService, networkMapper),
            pagingSourceFactory = {
                storyDatabase.storyDao.getAllStories()
            }
        ).flow
    }

    override fun getAllStories(
        token: String,
        isLocationOnly: Boolean
    ): Flow<NetworkResult<List<Story>>> = flow {

        wrapEspressoIdlingResource {

            emit(NetworkResult.Loading)

            try {
                val response: StoryResponse = if (isLocationOnly) apiService.getAllStories(
                    token,
                    1
                ) else apiService.getAllStories(token, 0)

                if (response.error) {
                    emit(NetworkResult.Error(response.message))
                } else {

                    val listStory: List<Story> =
                        response.data?.map { networkMapper.mapToEntity(it) }.orEmpty()

                    emit(NetworkResult.Success(listStory))
                }
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message.toString()))
            }
        }
    }
}