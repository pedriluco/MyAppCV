package com.example.myapp.data.model

data class CreateAppointmentRequest(
    val tenantId: Long,
    val serviceId: Long,
    val clientName: String,
    val date: String,
    val time: String
)
