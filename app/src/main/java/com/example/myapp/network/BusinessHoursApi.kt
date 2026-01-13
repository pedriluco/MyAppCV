package com.example.myapp.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface BusinessHoursApi {

    @GET("/api/tenants/{tenantId}/hours")
    suspend fun getAll(
        @Path("tenantId") tenantId: Long
    ): List<BusinessHoursDto>

    @PUT("/api/tenants/{tenantId}/hours")
    suspend fun saveAll(
        @Path("tenantId") tenantId: Long,
        @Body items: List<BusinessHoursDto>
    )
}
