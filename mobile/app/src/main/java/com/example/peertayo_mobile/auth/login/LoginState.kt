package com.example.peertayo_mobile.auth.login

import com.example.peertayo_mobile.auth.shared.User

sealed class LoginState {
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}
