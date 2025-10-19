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
    val acceptedBy: String? = null,        // UID del usuario que aceptó
    val timestamp: Long = System.currentTimeMillis() // opcional, para ordenar
)