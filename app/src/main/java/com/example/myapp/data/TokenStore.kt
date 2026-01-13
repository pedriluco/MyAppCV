package com.example.myapp.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TokenStore(private val context: Context) {

    private val prefs by lazy {
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    }

    suspend fun saveToken(token: String) = withContext(Dispatchers.IO) {
        prefs.edit().putString("jwt", token).apply()
    }

    suspend fun getToken(): String? = withContext(Dispatchers.IO) {
        prefs.getString("jwt", null)
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        prefs.edit().remove("jwt").apply()
    }
}
