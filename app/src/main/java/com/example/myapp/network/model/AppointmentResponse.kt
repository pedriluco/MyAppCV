package com.example.myapp.data.model

data class AppointmentResponse(
    val id: Long,
    val tenantId: Long,
    val serviceId: Long,
    val clientName: String,
    val date: String,
    val time: String
)
