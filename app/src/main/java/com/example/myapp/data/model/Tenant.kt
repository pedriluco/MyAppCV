package com.example.myapp.data.model

data class Tenant(
    val id: Long? = null,
    val name: String,
    val logoUrl: String? = null,
    val imageUrls: List<String> = emptyList()
)
