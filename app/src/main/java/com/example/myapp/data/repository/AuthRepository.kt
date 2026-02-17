package com.example.myapp.data.repository

import android.content.Context
import com.example.myapp.data.repository.TokenStore
import com.example.myapp.network.ApiClient
import com.example.myapp.network.LoginRequest
import com.example.myapp.network.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val context: Context
) {

    private val store = TokenStore(context)
    private var token: String? = null

    suspend fun login(email: String, password: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                ApiClient.clearToken()
                store.clear()
                token = null

                val response = ApiClient.authApi.login(
                    LoginRequest(
                        email = email,
                        password = password
                    )
                )

                if (!response.isSuccessful) {
                    throw Exception("Usuario o contraseña incorrectos")
                }

                val t = response.body()
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: throw Exception("Token vacío")

                token = t
                store.save(t)
                ApiClient.setToken(t)

                t
            }
        }

    suspend fun register(
        email: String,
        password: String,
        role: String
    ): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                ApiClient.clearToken()
                store.clear()
                token = null

                val normalizedRole = role.trim().uppercase()

                val req = RegisterRequest(
                    email = email.trim(),
                    password = password,
                    role = if (normalizedRole == "OWNER") "OWNER" else "USER"
                )

                val response = ApiClient.authApi.register(req)

                if (!response.isSuccessful) {
                    val code = response.code()
                    val body = runCatching { response.errorBody()?.string() }.getOrNull()
                    throw Exception(
                        "No se pudo registrar (HTTP $code)${
                            if (!body.isNullOrBlank()) ": $body" else ""
                        }"
                    )
                }

                val t = response.body()
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: throw Exception("Token vacío")

                token = t
                store.save(t)
                ApiClient.setToken(t)

                t
            }
        }

    suspend fun getToken(): String? =
        withContext(Dispatchers.IO) {
            token ?: store.load()
        }

    suspend fun loadTokenIntoClient(): Boolean =
        withContext(Dispatchers.IO) {
            val t = store.load()
            token = t
            ApiClient.setToken(t)
            !t.isNullOrBlank()
        }

    suspend fun logout() =
        withContext(Dispatchers.IO) {
            token = null
            store.clear()
            ApiClient.clearToken()
        }
}
