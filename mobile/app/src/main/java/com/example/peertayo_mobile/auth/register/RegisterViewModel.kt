package com.example.peertayo_mobile.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peertayo_mobile.data.model.ApiResponse
import com.example.peertayo_mobile.data.model.AuthResponse
import com.example.peertayo_mobile.data.model.RegisterRequest
import com.example.peertayo_mobile.data.repository.AuthRepository
import kotlinx.coroutines.launch
import org.json.JSONObject

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
            try {
                val response = repository.register(request)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                        _registerState.value = RegisterState.Success(apiResponse.data)
                    } else {
                        _registerState.value = RegisterState.Error(apiResponse?.error?.message ?: "Registration failed")
                    }
                } else {
                    val errorStr = response.errorBody()?.string()
                    val message = try {
                        val errorJson = JSONObject(errorStr)
                        errorJson.optJSONObject("error")?.optString("message") 
                            ?: errorJson.optString("message", "Registration failed")
                    } catch (e: Exception) {
                        "Registration failed"
                    }
                    _registerState.value = RegisterState.Error(message)
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Network error")
            }
        }
    }
}
