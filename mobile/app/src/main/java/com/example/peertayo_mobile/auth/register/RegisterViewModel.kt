package com.example.peertayo_mobile.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peertayo_mobile.data.model.AuthResponse
import com.example.peertayo_mobile.data.model.RegisterRequest
import com.example.peertayo_mobile.data.repository.AuthRepository
import kotlinx.coroutines.launch

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val response: AuthResponse) : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _registerState = MutableLiveData<RegisterState>(RegisterState.Idle)
    val registerState: LiveData<RegisterState> = _registerState

    fun register(request: RegisterRequest) {
        _registerState.value = RegisterState.Loading
        viewModelScope.launch {
            val result = repository.register(request)
            result.onSuccess { auth ->
                if (auth != null) _registerState.value = RegisterState.Success(auth)
                else _registerState.value = RegisterState.Error("Registration failed: Empty response")
            }.onFailure { e ->
                _registerState.value = RegisterState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun googleLogin(idToken: String) {
        _registerState.value = RegisterState.Loading
        viewModelScope.launch {
            val result = repository.googleLogin(idToken)
            result.onSuccess { auth ->
                if (auth != null) _registerState.value = RegisterState.Success(auth)
                else _registerState.value = RegisterState.Error("Google registration failed")
            }.onFailure { e ->
                _registerState.value = RegisterState.Error(e.message ?: "Google authentication failed")
            }
        }
    }
}
