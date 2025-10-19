package com.example.favoresapp.ui.Model


data class Conversation(
    // Lista de los UIDs de los dos participantes.
    val participants: List<String> = emptyList(),

    // Información del último mensaje para mostrar en la vista previa.
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long? = null,
    val lastMessageSenderId: String = "",

    // Nombres y UIDs de los participantes para fácil acceso.
    val participantNames: Map<String, String> = emptyMap()
)