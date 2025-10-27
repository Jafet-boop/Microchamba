package com.example.favoresapp.ui.Model
import java.util.Date

data class Conversation(
    val participants: List<String> = emptyList(), // Lista de los UIDs de los dos participantes.
    val lastMessageText: String = "", // Información del último mensaje para mostrar en la vista previa.
    val lastMessageTimestamp: Date? = null,
    val lastMessageSenderId: String = "",
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessageReadBy: List<String> = emptyList()
)