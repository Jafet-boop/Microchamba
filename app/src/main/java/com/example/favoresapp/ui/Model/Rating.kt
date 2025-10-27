package com.example.favoresapp.ui.Model

import java.util.UUID

class Rating (
    val id: String = UUID.randomUUID().toString(),
    val favorId: String = "",
    val fromUserId: String = "", // Quien califica
    val toUserId: String = "",   // Quien recibe la calificación
    val rating: Float = 0f,      // 1.0 a 5.0
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)