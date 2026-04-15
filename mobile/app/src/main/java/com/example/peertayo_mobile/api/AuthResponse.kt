package com.example.peertayo_mobile.api

import com.google.gson.annotations.SerializedName

// API Response wrapper
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: ErrorResponse?,
    val timestamp: String
)

// Error response
data class ErrorResponse(
    val code: String,
    val message: String,
    val details: Any?
)

// User data
data class User(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val roles: List<String>
) {
    val fullName: String get() = "$firstName $lastName"
}

// Auth response (login/register)
data class AuthResponse(
    val user: User,
    val token: String
)
