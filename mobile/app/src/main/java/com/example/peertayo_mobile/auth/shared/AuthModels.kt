package com.example.peertayo_mobile.auth.shared

/**
 * Shared auth domain models used across login and register sub-slices.
 */

data class User(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val roles: List<String>
) {
    val fullName: String get() = "$firstName $lastName"
}

data class AuthResponse(
    val user: User,
    val token: String
)
