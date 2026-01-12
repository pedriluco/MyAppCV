package com.example.myapp.network

import com.example.myapp.data.model.CreateAppointmentRequest
import com.example.myapp.data.model.AppointmentResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AppointmentApi {

    @GET("appointments")
    suspend fun getAppointments(@Query("tenantId") tenantId: Long): List<AppointmentResponse>

    @POST("appointments")
    suspend fun createAppointment(@Body req: CreateAppointmentRequest): AppointmentResponse
}
