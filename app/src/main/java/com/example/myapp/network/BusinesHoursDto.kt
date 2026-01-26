package com.example.myapp.network

data class BusinessHoursDto(
    val tenantId: Long,
    val dayOfWeek: Int,
    val openTime: String?,   // nullable porque tu UI usa null cuando closed=true
    val closeTime: String?,  // nullable
    val closed: Boolean
)
