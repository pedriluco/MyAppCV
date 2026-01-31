package com.example.myapp.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class AppointmentDto(
    val id: Long,
    val tenantId: Long,
    val serviceId: Long,
    val clientName: String,
    val startAt: String,
    val endAt: String,
    val status: String
)

interface AppointmentApi {

    @GET("businesses/{tenantId}/appointments")
    suspend fun list(
        @Path("tenantId") tenantId: Long
    ): List<AppointmentDto>

    @POST("businesses/{tenantId}/appointments")
    suspend fun create(
        @Path("tenantId") tenantId: Long,
        @Body req: CreateAppointmentRequest
    ): AppointmentDto

    @POST("businesses/{tenantId}/appointments/{id}/approve")
    suspend fun approve(
        @Path("tenantId") tenantId: Long,
        @Path("id") id: Long
    )

    @POST("businesses/{tenantId}/appointments/{id}/reject")
    suspend fun reject(
        @Path("tenantId") tenantId: Long,
        @Path("id") id: Long
    )
}
