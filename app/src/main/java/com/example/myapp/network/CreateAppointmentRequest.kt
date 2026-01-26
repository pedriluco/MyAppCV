package com.example.myapp.network

data class CreateAppointmentRequest(
        val tenantId: Long,
        val serviceId: Long,
        val clientName: String,
        val date: String,   // YYYY-MM-DD
        val time: String    // HH:mm
)
