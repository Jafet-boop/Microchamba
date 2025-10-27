package com.example.favoresapp.ui.Model

data class Service(
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val price: String = "",
    val location: String = "",
    val userId: String = "",
    val publisherName: String = "",
    val status: String = "pendiente",      // pendiente, en progreso, completado
    val acceptedBy: String? = null,
    val applicants: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val isRated: Boolean = false,
    val completedAt: Long? = null // 🆕 Timestamp cuando se marcó como completado por el trabajador
)