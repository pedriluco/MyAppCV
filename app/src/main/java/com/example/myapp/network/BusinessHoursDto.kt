package com.example.myapp.network

data class BusinessHoursDto(
    val dayOfWeek: Int,
    val openTime: String?,
    val closeTime: String?,
    val closed: Boolean
)