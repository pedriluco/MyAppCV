package com.example.myapp.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BusinessHoursApi {

    @GET("businesses/{tenantId}/hours")
    suspend fun getAll(@Path("tenantId") tenantId: Long): List<BusinessHoursDto>

    @POST("businesses/{tenantId}/hours")
    suspend fun saveAll(
        @Path("tenantId") tenantId: Long,
        @Body items: List<BusinessHoursDto>
    ): List<BusinessHoursDto>
}
