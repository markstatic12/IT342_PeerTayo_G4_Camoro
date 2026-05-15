package com.example.peertayo_mobile.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peertayo_mobile.data.model.AuthResponse
import com.example.peertayo_mobile.data.model.LoginRequest
import com.example.peertayo_mobile.data.repository.AuthRepository
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val response: AuthResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    fun login(request: LoginRequest) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val result = repository.login(request)
            result.onSuccess { auth ->
                if (auth != null) _loginState.value = LoginState.Success(auth)
                else _loginState.value = LoginState.Error("Empty response from server")
            }.onFailure { e ->
                _loginState.value = LoginState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun googleLogin(idToken: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val result = repository.googleLogin(idToken)
            result.onSuccess { auth ->
                if (auth != null) _loginState.value = LoginState.Success(auth)
                else _loginState.value = LoginState.Error("Google sign-in failed")
            }.onFailure { e ->
                _loginState.value = LoginState.Error(e.message ?: "Google authentication failed")
            }
        }
    }
}
