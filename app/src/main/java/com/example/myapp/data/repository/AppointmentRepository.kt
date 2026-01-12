package com.example.myapp.data.repository

import com.example.myapp.data.model.CreateAppointmentRequest
import com.example.myapp.data.model.AppointmentResponse
import com.example.myapp.network.ApiClient

class AppointmentRepository {

    suspend fun getAppointments(tenantId: Long): List<AppointmentResponse> =
        ApiClient.appointmentApi.getAppointments(tenantId)

    suspend fun createAppointment(req: CreateAppointmentRequest): AppointmentResponse =
        ApiClient.appointmentApi.createAppointment(req)
}
