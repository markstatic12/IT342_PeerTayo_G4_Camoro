package com.example.peertayo_mobile.data.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: T?,
    @SerializedName("error") val error: ErrorPayload?,
    @SerializedName("timestamp") val timestamp: String
)

data class ErrorPayload(
    @SerializedName("code") val code: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("details") val details: Any?
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class AuthResponse(
    @SerializedName("token") val token: String?,
    @SerializedName("user") val user: UserResponse?
)

data class UserResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("provider") val provider: String?,
    @SerializedName("roles") val roles: List<String>?
) {
    val fullName: String get() = "$firstName $lastName"
    val primaryRole: String get() = roles?.firstOrNull() ?: "RESPONDENT"
}
