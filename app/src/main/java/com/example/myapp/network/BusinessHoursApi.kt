package com.example.myapp.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

data class BusinessHoursDto(
    val dayOfWeek: Int,      // 1 = Monday ... 7 = Sunday
    val openTime: String?,  // "09:00"
    val closeTime: String?, // "18:00"
    val closed: Boolean
)

interface BusinessHoursApi {

    @GET("businesses/{tenantId}/hours")
    suspend fun getAll(
        @Path("tenantId") tenantId: Long
    ): List<BusinessHoursDto>

    @PUT("businesses/{tenantId}/hours")
    suspend fun saveAll(
        @Path("tenantId") tenantId: Long,
        @Body hours: List<BusinessHoursDto>
    )
}
