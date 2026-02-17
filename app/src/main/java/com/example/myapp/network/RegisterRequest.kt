package com.example.myapp.network

data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String,
    val businessName: String? = null
)
