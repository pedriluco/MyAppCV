package com.example.myapp.util

import android.util.Base64
import org.json.JSONObject

object JwtUtils {
    fun getRole(token: String): String? = runCatching {
        val payload = token.split(".")[1]
        val json = String(Base64.decode(payload, Base64.URL_SAFE))
        JSONObject(json).optString("role", null)
    }.getOrNull()
}
