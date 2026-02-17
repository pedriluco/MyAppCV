package com.example.myapp.data.repository

import android.content.Context

class TokenStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun save(token: String?) {
        prefs.edit().putString("jwt_token", token?.trim()).apply()
    }

    fun load(): String? {
        return prefs.getString("jwt_token", null)?.trim()?.takeIf { it.isNotBlank() }
    }

    fun clear() {
        prefs.edit().remove("jwt_token").apply()
    }
}
