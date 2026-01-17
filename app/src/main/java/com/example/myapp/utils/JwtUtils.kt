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
        } catch (_: Exception) {
            null
        }
    }

    fun getClaim(token: String?, key: String): String? =
        decodePayload(token)?.optString(key, null)

    fun getRole(token: String?): String? =
        getClaim(token, "role")

    fun getUserId(token: String?): Long? =
        getClaim(token, "sub")?.toLongOrNull()
}
