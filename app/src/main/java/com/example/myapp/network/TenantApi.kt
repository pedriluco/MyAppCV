package com.example.myapp.network

import com.example.myapp.data.model.Tenant
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TenantApi {

    @GET("tenants")
    suspend fun getAllTenants(): List<Tenant>

    @GET("tenants/search")
    suspend fun searchTenants(@Query("q") q: String): List<Tenant>

    @POST("tenants")
    suspend fun createTenant(@retrofit2.http.Body tenant: Tenant): Tenant

    @POST("tenants/{id}/approve")
    suspend fun approveTenant(@Path("id") id: Long): Tenant

    @GET("tenants/pending")
    suspend fun getPendingTenants(): List<Tenant>
}
