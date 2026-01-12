package com.example.myapp.data.repository

import com.example.myapp.data.TokenStore
import com.example.myapp.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val tokenStore: TokenStore
) {
    suspend fun login(email: String, password: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val token = ApiClient.authApi.login(email, password)
                tokenStore.saveToken(token)
                ApiClient.setToken(token)
            }
        }

    suspend fun loadTokenIntoClient(): Boolean =
        withContext(Dispatchers.IO) {
            val token = tokenStore.getToken()
            ApiClient.setToken(token)
            token != null
        }

    suspend fun logout() =
        withContext(Dispatchers.IO) {
            tokenStore.clear()
            ApiClient.setToken(null)
        }
}
