package com.robertas.storyapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.android.gms.maps.model.LatLng
import com.robertas.storyapp.abstractions.INavigation
import com.robertas.storyapp.abstractions.StoryRepository
import com.robertas.storyapp.abstractions.UserRepository
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.enums.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val userStoryRepository: StoryRepository,

    private val userAccountRepository: UserRepository
) : ViewModel(), INavigation {

    private val _isStoriesInvalid = MutableLiveData(false)

    val isStoriesInvalid get() = _isStoriesInvalid

    private val _uploadStoryState = MutableLiveData<NetworkResult<Boolean>>()

    val uploadStoryState get() = _uploadStoryState

    fun invalidateStories() {
        _isStoriesInvalid.value = true
    }

    fun validateStories() {
        _isStoriesInvalid.value = false
    }

    private var _paginatedStories: Flow<PagingData<Story>>? = null

    fun getPaginatedStories(): Flow<PagingData<Story>> {

        if (_paginatedStories == null) {
            _paginatedStories =
                userStoryRepository.getAllStories().cachedIn(viewModelScope)
        }

        return _paginatedStories as Flow<PagingData<Story>>
    }

    fun uploadImage(file: File, description: String, rotation: Float) {

        _uploadStoryState.value = NetworkResult.Loading

        viewModelScope.launch {
            try {
                val error = userStoryRepository.postStory(file, description, rotation)

                _uploadStoryState.value = NetworkResult.Success(error)
            } catch (e: Exception) {
                _uploadStoryState.value = NetworkResult.Error(e.message.toString())
            }
        }
    }

    fun uploadImage(file: File, description: String, rotation: Float, latLng: LatLng) {

        _uploadStoryState.value = NetworkResult.Loading

        viewModelScope.launch {
            try {
                val error = userStoryRepository.postStory(file, description, rotation, latLng)

                _uploadStoryState.value = NetworkResult.Success(error)
            } catch (e: Exception) {
                _uploadStoryState.value = NetworkResult.Error(e.message.toString())
            }
        }
    }

    fun getCameraMode() = userAccountRepository.getCameraMode()

    fun setCameraMode(mode: String) {
        userAccountRepository.setCameraMode(mode)
    }

    override fun doneNavigating() {
        _uploadStoryState.value = NetworkResult.Loading
    }
}