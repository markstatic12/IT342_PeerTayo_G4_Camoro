package com.example.peertayo_mobile.api

import com.google.gson.annotations.SerializedName

// Register Request
data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)

// Login Request
data class LoginRequest(
    val email: String,
    val password: String
)
