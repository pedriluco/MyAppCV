package com.example.myapp.network

data class CreateAppointmentRequest(
        val serviceId: Long,
        val clientName: String,
        val startAt: String
)
