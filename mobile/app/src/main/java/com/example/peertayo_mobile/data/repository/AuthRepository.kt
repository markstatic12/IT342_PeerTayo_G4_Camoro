package com.example.peertayo_mobile.data.repository

import com.example.peertayo_mobile.data.api.AuthApi
import com.example.peertayo_mobile.data.model.ApiResponse
import com.example.peertayo_mobile.data.model.AuthResponse
import com.example.peertayo_mobile.data.model.LoginRequest
import com.example.peertayo_mobile.data.model.RegisterRequest
import retrofit2.Response

class AuthRepository(private val authApi: AuthApi) {
    suspend fun login(request: LoginRequest): Response<ApiResponse<AuthResponse>> {
        return authApi.login(request)
    }

    suspend fun register(request: RegisterRequest): Response<ApiResponse<AuthResponse>> {
        return authApi.register(request)
    }
}
