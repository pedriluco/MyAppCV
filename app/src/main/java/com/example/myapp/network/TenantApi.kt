package com.example.myapp.network

import com.example.myapp.data.model.Tenant
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TenantApi {

    @POST("tenants")
    suspend fun createTenant(
        @Body tenant: Tenant
    ): Tenant

    @GET("tenants")
    suspend fun getAllTenants(): List<Tenant>

    @GET("tenants/search")
    suspend fun searchTenants(
        @Query("q") q: String
    ): List<Tenant>
}
