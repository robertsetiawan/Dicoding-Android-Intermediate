package com.robertas.storyapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robertas.storyapp.abstractions.INavigation
import com.robertas.storyapp.abstractions.UserRepository
import com.robertas.storyapp.models.domain.User
import com.robertas.storyapp.models.enums.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userAccountRepository: UserRepository
): ViewModel(){

    suspend fun login(email: String, password: String) = userAccountRepository.login(email, password)

    suspend fun register(name: String,
                         email: String,
                         password: String) = userAccountRepository.register(name, email, password)

    fun isUserLoggedIn() = userAccountRepository.isUserLoggedIn()

    fun logOut() {
        userAccountRepository.logOut()
    }

    fun getLanguageMode() = userAccountRepository.getLanguageMode()

    fun setLanguageMode(languageMode: String) {
        userAccountRepository.setLanguageMode(languageMode)
    }
}