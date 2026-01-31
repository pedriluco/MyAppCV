package com.example.myapp.data.repository

import com.example.myapp.network.ApiClient
import com.example.myapp.network.AppointmentDto
import com.example.myapp.network.CreateAppointmentRequest

class AppointmentRepository {

    suspend fun list(tenantId: Long): List<AppointmentDto> =
        ApiClient.appointmentApi.list(tenantId)

    suspend fun create(tenantId: Long, req: CreateAppointmentRequest): AppointmentDto =
        ApiClient.appointmentApi.create(tenantId, req)

    suspend fun approve(tenantId: Long, id: Long) {
        ApiClient.appointmentApi.approve(tenantId, id)
    }

    suspend fun reject(tenantId: Long, id: Long) {
        ApiClient.appointmentApi.reject(tenantId, id)
    }
}
