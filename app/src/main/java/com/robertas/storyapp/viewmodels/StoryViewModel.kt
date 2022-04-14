package com.robertas.storyapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robertas.storyapp.abstractions.INavigation
import com.robertas.storyapp.abstractions.StoryRepository
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.enums.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val userStoryRepository: StoryRepository
): ViewModel(), INavigation {



    private val _loadStoryState = MutableLiveData<NetworkResult<List<Story>?>>()

    val loadStoryState get() = _loadStoryState

    private val _uploadStoryState = MutableLiveData<NetworkResult<Boolean>>()

    val uploadStoryState get() = _uploadStoryState

    fun getAllStories() {
        _loadStoryState.value = NetworkResult.Loading

        viewModelScope.launch {
            try {
                val storyList = userStoryRepository.getAllStories()

                _loadStoryState.value = NetworkResult.Success(storyList)
            } catch (e: Exception){
                _loadStoryState.value = NetworkResult.Error(e.message.toString())
            }
        }
    }

    fun uploadImage(file: File, description: String){

        _uploadStoryState.value = NetworkResult.Loading

        viewModelScope.launch {
            try {
                val error = userStoryRepository.postStory(file, description)

                _uploadStoryState.value = NetworkResult.Success(error)
            } catch (e: Exception){
                _uploadStoryState.value = NetworkResult.Error(e.message.toString())
            }
        }
    }

    override fun doneNavigating() {
        _uploadStoryState.value = NetworkResult.Loading
    }

    init {
        getAllStories()
    }
}