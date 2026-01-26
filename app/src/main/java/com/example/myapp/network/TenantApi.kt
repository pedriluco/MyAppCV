package com.example.myapp.network

import com.example.myapp.data.model.Tenant
import retrofit2.http.*

interface TenantApi {

    @POST("tenants")
    suspend fun createTenant(@Body tenant: Tenant): Tenant

    @GET("tenants")
    suspend fun getAllTenants(): List<Tenant>

    @GET("tenants/search")
    suspend fun searchTenants(@Query("q") q: String): List<Tenant>

    @POST("tenants/{id}/approve")
    suspend fun approveTenant(@Path("id") id: Long): Tenant
}
