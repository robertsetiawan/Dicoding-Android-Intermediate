package com.robertas.storyapp.viewmodels

import androidx.lifecycle.ViewModel
import com.robertas.storyapp.abstractions.StoryRepository
import com.robertas.storyapp.abstractions.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(
    private val userAccountRepository: UserRepository,

    private val userStoryRepository: StoryRepository
): ViewModel() {
    fun getListStory() = userStoryRepository.getAllStories(userAccountRepository.getBearerToken(), true)
}