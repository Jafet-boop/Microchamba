package com.example.favoresapp.ui.Model

data class Service(
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val price: String = "",
    val location: String = "",
    val userId: String = "",
    val status: String = "pendiente",
    val acceptedBy: String? = null,
    val applicants: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)