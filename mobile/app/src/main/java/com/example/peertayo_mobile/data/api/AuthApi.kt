package com.example.peertayo_mobile.data.api

import com.example.peertayo_mobile.data.model.ApiResponse
import com.example.peertayo_mobile.data.model.AuthResponse
import com.example.peertayo_mobile.data.model.LoginRequest
import com.example.peertayo_mobile.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/google")
    suspend fun googleLogin(@Body request: Map<String, String>): Response<ApiResponse<AuthResponse>>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>
}
