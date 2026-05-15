package com.example.peertayo_mobile.data.repository

import com.example.peertayo_mobile.data.api.AuthApi
import com.example.peertayo_mobile.data.model.*
import com.google.gson.Gson
import okhttp3.ResponseBody

class AuthRepository(private val api: AuthApi) {

    private val gson = Gson()

    suspend fun login(request: LoginRequest): Result<AuthResponse?> = runCatching {
        val response = api.login(request)
        if (response.isSuccessful) {
            response.body()?.data
        } else {
            val errorMsg = parseError(response.errorBody())
            throw Exception(errorMsg ?: "Login failed")
        }
    }

    suspend fun googleLogin(idToken: String): Result<AuthResponse?> = runCatching {
        val response = api.googleLogin(mapOf("idToken" to idToken))
        if (response.isSuccessful) {
            response.body()?.data
        } else {
            val errorMsg = parseError(response.errorBody())
            throw Exception(errorMsg ?: "Google sign-in failed")
        }
    }

    suspend fun register(request: RegisterRequest): Result<AuthResponse?> = runCatching {
        val response = api.register(request)
        if (response.isSuccessful) {
            response.body()?.data
        } else {
            val errorMsg = parseError(response.errorBody())
            throw Exception(errorMsg ?: "Registration failed")
        }
    }

    private fun parseError(errorBody: ResponseBody?): String? {
        return try {
            val json = errorBody?.string()
            val apiResponse = gson.fromJson(json, ApiResponse::class.java)
            apiResponse.error?.message
        } catch (e: Exception) {
            null
        }
    }
}
