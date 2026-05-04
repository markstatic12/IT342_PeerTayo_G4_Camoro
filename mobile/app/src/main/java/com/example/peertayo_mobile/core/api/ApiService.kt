package com.example.peertayo_mobile.core.api

import com.example.peertayo_mobile.auth.shared.AuthResponse
import com.example.peertayo_mobile.auth.shared.User
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API interface — all backend endpoints.
 */
interface ApiService {

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<ApiResponse<User>>

    @POST("api/v1/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<ApiResponse<Unit>>
}
