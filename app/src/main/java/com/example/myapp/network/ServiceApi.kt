package com.example.myapp.network

import retrofit2.http.*

data class ServiceDto(
    val id: Long,
    val tenantId: Long,
    val name: String,
    val durationMinutes: Int,
    val active: Boolean
)

data class CreateServiceRequest(
    val name: String,
    val durationMinutes: Int
)

interface ServiceApi {

    @GET("businesses/{tenantId}/services")
    suspend fun list(
        @Path("tenantId") tenantId: Long
    ): List<ServiceDto>

    @POST("businesses/{tenantId}/services")
    suspend fun create(
        @Path("tenantId") tenantId: Long,
        @Body body: CreateServiceRequest
    ): ServiceDto

    @PATCH("businesses/{tenantId}/services/{id}/active")
    suspend fun setActive(
        @Path("tenantId") tenantId: Long,
        @Path("id") id: Long,
        @Query("active") active: Boolean
    )
}
