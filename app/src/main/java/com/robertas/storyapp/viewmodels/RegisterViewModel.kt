package com.robertas.storyapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robertas.storyapp.abstractions.INavigation
import com.robertas.storyapp.abstractions.UserRepository
import com.robertas.storyapp.models.enums.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userAccountRepository: UserRepository
): ViewModel(), INavigation {
    override fun doneNavigating() {
        _registerState.value = NetworkResult.Loading
    }

    private val _registerState = MutableLiveData<NetworkResult<Boolean?>>()

    val registerState get() = _registerState

    fun register(name: String, email: String, password: String) {
        _registerState.value = NetworkResult.Loading

        viewModelScope.launch {
            try {
                val status = userAccountRepository.register(name, email, password)

                _registerState.value = NetworkResult.Success(status)
            } catch (e: Exception) {
                _registerState.value = NetworkResult.Error(e.message.toString())
            }
        }
    }

}