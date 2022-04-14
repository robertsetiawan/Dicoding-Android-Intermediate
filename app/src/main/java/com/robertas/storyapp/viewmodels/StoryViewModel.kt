package com.robertas.storyapp.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.robertas.storyapp.abstractions.StoryRepository
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.enums.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val userStoryRepository: StoryRepository
): ViewModel() {

    private val _loadStoryState = MutableLiveData<NetworkResult<List<Story>?>>()

    val loadStoryState get() = _loadStoryState

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

    init {
        getAllStories()
    }
}