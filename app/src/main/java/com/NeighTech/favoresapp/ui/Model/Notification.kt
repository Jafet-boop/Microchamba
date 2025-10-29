package com.NeighTech.favoresapp.ui.Model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Notification(
    val id: String = "",
    val recipientId: String = "", // ID del dueño de la tarea
    val senderId: String = "",    // ID de quien se postula
    val senderName: String = "",  // Nombre de quien se postula
    val serviceId: String = "",   // ID de la tarea
    val serviceTitle: String = "",// Título de la tarea
    val type: String = "new_applicant", // Tipo de notificación
    @ServerTimestamp
    val timestamp: Date? = null,
    val isRead: Boolean = false
)
