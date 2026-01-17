package com.example.myapp.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    val email: String,
    val password: String
)

interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<String>
}
