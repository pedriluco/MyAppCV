package com.example.myapp.data.repository

import com.example.myapp.data.TokenStore
import com.example.myapp.network.ApiClient
import com.example.myapp.network.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val tokenStore: TokenStore
) {

    // LOGIN: regresa el token
    suspend fun login(email: String, password: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = ApiClient.authApi.login(
                    LoginRequest(email = email, password = password)
                )

                if (!response.isSuccessful) {
                    throw Exception("Usuario o contraseña incorrectos")
                }

                val token = response.body()?.trim()
                    ?: throw Exception("Token vacío")

                tokenStore.saveToken(token)
                ApiClient.setToken(token)

                token
            }
        }

    suspend fun getToken(): String? =
        withContext(Dispatchers.IO) { tokenStore.getToken() }

    suspend fun loadTokenIntoClient(): Boolean =
        withContext(Dispatchers.IO) {
            val token = tokenStore.getToken()
            ApiClient.setToken(token)
            !token.isNullOrBlank()
        }

    suspend fun logout() =
        withContext(Dispatchers.IO) {
            tokenStore.clear()
            ApiClient.setToken(null)
        }
}
