package com.example.favoresapp.ui.Model
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Message(
    val texto: String = "",
    val usuarioId: String = "",
    val nombreUsuario: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)