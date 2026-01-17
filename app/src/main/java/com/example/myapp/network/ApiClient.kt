package com.example.myapp.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import com.example.myapp.network.AuthApi


object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    // 🔑 Token en memoria (el interceptor SOLO usa esto)
    @Volatile
    private var token: String? = null

    fun setToken(newToken: String?) {
        token = newToken
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                val currentToken = token
                if (!currentToken.isNullOrBlank()) {
                    requestBuilder.addHeader(
                        "Authorization",
                        "Bearer $currentToken"
                    )
                }
                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create()) // <- primero
            .addConverterFactory(GsonConverterFactory.create())    // <- después
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val tenantApi: TenantApi by lazy { retrofit.create(TenantApi::class.java) }
    val appointmentApi: AppointmentApi by lazy { retrofit.create(AppointmentApi::class.java) }
    val serviceApi: ServiceApi by lazy { retrofit.create(ServiceApi::class.java) }
    val hoursApi: BusinessHoursApi = retrofit.create(BusinessHoursApi::class.java)
}
