package com.example.myapp.network

import android.util.Base64
import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    @Volatile
    private var token: String? = null

    @Volatile
    private var onAuthError: (() -> Unit)? = null

    fun setOnAuthError(cb: (() -> Unit)?) {
        onAuthError = cb
    }

    fun setToken(newToken: String?) {
        token = newToken?.trim()
        Log.d(
            "API_AUTH",
            "setToken called tokenPresent=${!token.isNullOrBlank()} len=${token?.length}"
        )
    }

    fun clearToken() {
        token = null
        Log.d("API_AUTH", "clearToken called tokenPresent=false")
    }

    private fun decodeJwtPayload(jwt: String?): String? {
        if (jwt.isNullOrBlank()) return null
        val parts = jwt.split(".")
        if (parts.size < 2) return null

        return try {
            val payload = parts[1]
                .replace('-', '+')
                .replace('_', '/')
                .let { p ->
                    val pad = (4 - p.length % 4) % 4
                    p + "=".repeat(pad)
                }

            String(Base64.decode(payload, Base64.DEFAULT))
        } catch (_: Exception) {
            null
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val t = token?.trim()

                val builder = original.newBuilder()
                if (!t.isNullOrBlank()) {
                    builder.header("Authorization", "Bearer $t")
                }

                val out = builder.build()

                val payloadFirst = t
                    ?.split(".")
                    ?.getOrNull(1)
                    ?.take(20)

                Log.d(
                    "API_HTTP",
                    "REQ ${out.method} ${out.url} tokenPresent=${!t.isNullOrBlank()} payloadFirst20=$payloadFirst"
                )

                val payloadJson = decodeJwtPayload(t)
                Log.d("API_HTTP", "JWT payloadJson=$payloadJson")

                val response = try {
                    chain.proceed(out)
                } catch (e: Exception) {
                    Log.e("API_HTTP", "RES EXCEPTION ${out.url} ${e.message}", e)
                    throw e
                }

                Log.d(
                    "API_HTTP",
                    "RES ${response.code} ${out.url}"
                )

                if (response.code == 401 || response.code == 403) {
                    clearToken()
                    onAuthError?.invoke()
                }

                if (!response.isSuccessful) {
                    val peek = response.peekBody(2048).string()
                    Log.e(
                        "API_HTTP",
                        "RES_BODY ${response.code} ${out.url} body=$peek"
                    )
                }

                response
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val tenantApi: TenantApi by lazy { retrofit.create(TenantApi::class.java) }
    val appointmentApi: AppointmentApi by lazy { retrofit.create(AppointmentApi::class.java) }
    val serviceApi: ServiceApi by lazy { retrofit.create(ServiceApi::class.java) }
    val hoursApi: BusinessHoursApi by lazy { retrofit.create(BusinessHoursApi::class.java) }
}
