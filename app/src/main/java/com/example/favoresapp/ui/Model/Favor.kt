package com.example.favoresapp.ui.Model

import java.util.UUID

data class Favor(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val userId: String = "",              // Quien publica el favor
    val assignedToUserId: String? = null, // Quien acepta el favor
    val status: FavorStatus = FavorStatus.AVAILABLE,
    val timestamp: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val isRated: Boolean = false
)

enum class FavorStatus {
    AVAILABLE,      // Disponible para tomar
    IN_PROGRESS,    // Alguien lo está haciendo
    COMPLETED,      // Completado pero sin calificar
    RATED           // Completado y calificado
}
