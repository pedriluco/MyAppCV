package com.example.myapp.utils

import android.util.Base64
import org.json.JSONObject

object JwtUtils {

    private fun decodePayload(token: String?): JSONObject? {
        if (token.isNullOrBlank()) return null
        val parts = token.split(".")
        if (parts.size < 2) return null

        return try {
            val payload = String(
                Base64.decode(
                    parts[1],
                    Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                )
            )
            JSONObject(payload)
        } catch (e: Exception) {
            null
        }
    }

    fun getClaim(token: String?, key: String): String? {
        if (token.isNullOrBlank()) return null
        return try {
            val payload = decodePayload(token) ?: return null
            if (!payload.has(key)) null else payload.getString(key)
        } catch (e: Exception) {
            null
        }
    }

    fun getRole(token: String?): String? =
        getClaim(token, "role")

    fun getUserId(token: String?): Long? =
        getClaim(token, "sub")?.toLongOrNull()
}
