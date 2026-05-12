package com.example.peertayo_mobile.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peertayo_mobile.data.model.ApiResponse
import com.example.peertayo_mobile.data.model.AuthResponse
import com.example.peertayo_mobile.data.model.LoginRequest
import com.example.peertayo_mobile.data.repository.AuthRepository
import kotlinx.coroutines.launch
import org.json.JSONObject

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
            try {
                val response = repository.login(request)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                        _loginState.value = LoginState.Success(apiResponse.data)
                    } else {
                        _loginState.value = LoginState.Error(apiResponse?.error?.message ?: "Login failed")
                    }
                } else {
                    // Extract error message from JSON error body
                    val errorStr = response.errorBody()?.string()
                    val message = try {
                        val errorJson = JSONObject(errorStr)
                        errorJson.optJSONObject("error")?.optString("message") 
                            ?: errorJson.optString("message", "Login failed")
                    } catch (e: Exception) {
                        "Login failed"
                    }
                    _loginState.value = LoginState.Error(message)
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Network error")
            }
        }
    }
}
