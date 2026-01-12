package com.example.myapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val tenantApi: TenantApi by lazy { retrofit.create(TenantApi::class.java) }
    val appointmentApi: AppointmentApi by lazy { retrofit.create(AppointmentApi::class.java) }
}
