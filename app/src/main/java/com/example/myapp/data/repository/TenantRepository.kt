package com.example.myapp.data.repository

import com.example.myapp.data.model.Tenant
import com.example.myapp.network.ApiClient

class TenantRepository {

    suspend fun createTenant(name: String): Tenant {
        return ApiClient.tenantApi.createTenant(Tenant(name = name))
    }

    suspend fun getAllTenants(): List<Tenant> {
        return ApiClient.tenantApi.getAllTenants()
    }

    suspend fun searchTenants(query: String): List<Tenant> {
        return ApiClient.tenantApi.searchTenants(query)
    }
}
