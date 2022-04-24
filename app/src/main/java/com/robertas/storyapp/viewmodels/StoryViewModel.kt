package com.robertas.storyapp.viewmodels

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.android.gms.maps.model.LatLng
import com.robertas.storyapp.abstractions.StoryRepository
import com.robertas.storyapp.abstractions.UserRepository
import com.robertas.storyapp.models.domain.Story
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val userStoryRepository: StoryRepository,

    private val userAccountRepository: UserRepository,
) : ViewModel(){

    private val _isStoriesInvalid = MutableLiveData(false)

    val isStoriesInvalid get() = _isStoriesInvalid

    fun invalidateStories() {
        _isStoriesInvalid.value = true
    }

    fun validateStories() {
        _isStoriesInvalid.value = false
    }

    private var _oldPaginatedData: LiveData<PagingData<Story>> ?= null

    fun getPaginatedData(): LiveData<PagingData<Story>>{

        if (_oldPaginatedData == null){
            _oldPaginatedData = userStoryRepository.getAllStories(userAccountRepository.getBearerToken()).cachedIn(viewModelScope).asLiveData()
        }

        return _oldPaginatedData as LiveData<PagingData<Story>>
    }


    suspend fun uploadImage(file: File, description: String, rotation: Float) = userStoryRepository.postStory(
        userAccountRepository.getBearerToken(),
        file,
        description,
        rotation
    )

    suspend fun uploadImage(file: File, description: String, rotation: Float, location: LatLng) = userStoryRepository.postStory(
        userAccountRepository.getBearerToken(),
        file,
        description,
        rotation,
        location
    )

    fun getCameraMode() = userAccountRepository.getCameraMode()

    fun setCameraMode(mode: String) {
        userAccountRepository.setCameraMode(mode)
    }
}