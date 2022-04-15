package com.robertas.storyapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robertas.storyapp.abstractions.INavigation
import com.robertas.storyapp.abstractions.UserRepository
import com.robertas.storyapp.models.domain.User
import com.robertas.storyapp.models.enums.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userAccountRepository: UserRepository
): ViewModel(), INavigation {
    override fun doneNavigating() {
        _loginState.value = NetworkResult.Loading
    }

    private val _loginState = MutableLiveData<NetworkResult<User?>>()

    val loginState get() = _loginState

    fun login(email: String, password: String) {

        _loginState.value = NetworkResult.Loading

        viewModelScope.launch {
            try {

                val user = userAccountRepository.login(email, password)

                _loginState.value = NetworkResult.Success(user)

                user?.let { userAccountRepository.setLoggedInUser(it) }

            } catch (e: Exception) {
                _loginState.value = NetworkResult.Error(e.message.toString())
            }
        }
    }

    fun isUserLoggedIn() = userAccountRepository.isUserLoggedIn()

    fun logOut() {
        userAccountRepository.logOut()
    }

    fun getLanguageMode() = userAccountRepository.getLanguageMode()

    fun setLanguageMode(languageMode: String) {
        userAccountRepository.setLanguageMode(languageMode)
    }
}