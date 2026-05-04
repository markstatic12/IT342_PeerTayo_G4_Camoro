package com.example.peertayo_mobile.auth.register

import com.example.peertayo_mobile.auth.shared.User

sealed class RegisterState {
    object Loading : RegisterState()
    data class Success(val user: User) : RegisterState()
    data class Error(val message: String) : RegisterState()
}
