package com.example.peertayo_mobile.core.api

import com.example.peertayo_mobile.auth.shared.AuthResponse
import com.example.peertayo_mobile.auth.shared.User

/**
 * Core API request/response models shared across all feature slices.
 */

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: ErrorResponse?,
    val timestamp: String
)

data class ErrorResponse(
    val code: String,
    val message: String,
    val details: Any?
)

// Auth requests
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)
