package com.example.peertayo_mobile.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peertayo_mobile.api.ApiService
import com.example.peertayo_mobile.api.RegisterRequest
import com.example.peertayo_mobile.api.User
import com.example.peertayo_mobile.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> = _registerState

    fun register(firstName: String, lastName: String, email: String, password: String) {
        // Validate
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
            _registerState.value = RegisterState.Error("All fields are required")
            return
        }

        if (password.length < 6) {
            _registerState.value = RegisterState.Error("Password must be at least 6 characters")
            return
        }

        _registerState.value = RegisterState.Loading

        viewModelScope.launch {
            try {
                val request = RegisterRequest(
                    firstName.trim(),
                    lastName.trim(),
                    email.trim(),
                    password
                )
                val response = apiService.register(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val authResponse = response.body()?.data
                    if (authResponse != null) {
                        tokenManager.saveToken(authResponse.token)
                        _registerState.value = RegisterState.Success(authResponse.user)
                    } else {
                        _registerState.value = RegisterState.Error("Invalid response")
                    }
                } else {
                    val errorMsg = response.body()?.error?.message ?: "Registration failed"
                    _registerState.value = RegisterState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Network error")
            }
        }
    }
}

sealed class RegisterState {
    object Loading : RegisterState()
    data class Success(val user: User) : RegisterState()
    data class Error(val message: String) : RegisterState()
}
