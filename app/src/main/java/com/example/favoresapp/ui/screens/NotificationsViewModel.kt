package com.example.favoresapp.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.favoresapp.ui.Model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

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
                        val notificationList = snapshot.toObjects(Notification::class.java)
                        _notifications.value = notificationList
                        _unreadCount.value = notificationList.count { !it.isRead }
                    }
                }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            firestore.collection("notifications").document(notificationId)
                .update("isRead", true)
                .addOnSuccessListener {
                    Log.d("NotificationsViewModel", "Notification marked as read")
                }
                .addOnFailureListener { e ->
                    Log.e("NotificationsViewModel", "Error marking notification as read", e)
                }
        }
    }
}
