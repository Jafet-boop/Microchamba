package com.NeighTech.favoresapp.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.NeighTech.favoresapp.ui.Model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationsViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    // Cambia esto para usar un Map<String, Notification>
    private val _notifications = MutableStateFlow<Map<String, Notification>>(emptyMap())
    val notifications: StateFlow<Map<String, Notification>> = _notifications

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    init {
        fetchNotifications()
    }

    private fun fetchNotifications() {
        if (currentUser == null) return

        viewModelScope.launch {
            firestore.collection("notifications")
                .whereEqualTo("recipientId", currentUser.uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("NotificationsViewModel", "Error fetching notifications", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        // Guarda como Map donde la key es el ID del documento
                        val notificationMap = snapshot.documents.associate { doc ->
                            doc.id to doc.toObject(Notification::class.java)!!
                        }
                        _notifications.value = notificationMap
                        _unreadCount.value = notificationMap.values.count { !it.isRead }
                    }
                }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("notifications")
                    .document(notificationId)
                    .update("isRead", true)
                    .await()
                Log.d("NotificationsViewModel", "Notification marked as read")
            } catch (e: Exception) {
                Log.e("NotificationsViewModel", "Error marking notification as read", e)
            }
        }
    }
}