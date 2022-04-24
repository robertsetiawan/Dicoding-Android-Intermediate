package com.robertas.storyapp.viewmodels

import androidx.lifecycle.ViewModel
import com.robertas.storyapp.abstractions.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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