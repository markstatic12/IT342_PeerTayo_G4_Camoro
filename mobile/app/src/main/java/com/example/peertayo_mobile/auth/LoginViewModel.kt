package com.example.peertayo_mobile.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peertayo_mobile.api.ApiService
import com.example.peertayo_mobile.api.LoginRequest
import com.example.peertayo_mobile.api.User
import com.example.peertayo_mobile.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun login(email: String, password: String) {
        // Validate
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Email and password are required")
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                val response = apiService.login(LoginRequest(email.trim(), password))
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val authResponse = response.body()?.data
                    if (authResponse != null) {
                        tokenManager.saveToken(authResponse.token)
                        _loginState.value = LoginState.Success(authResponse.user)
                    } else {
                        _loginState.value = LoginState.Error("Invalid response")
                    }
                } else {
                    val errorMsg = response.body()?.error?.message ?: "Login failed"
                    _loginState.value = LoginState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Network error")
            }
        }
    }
}

sealed class LoginState {
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}
