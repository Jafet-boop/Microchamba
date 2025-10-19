package com.example.favoresapp.ui.screens.conversations

import androidx.lifecycle.ViewModel
import com.example.favoresapp.ui.Model.Conversation
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log

class ConversationsViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations = _conversations.asStateFlow()

    init {
        fetchConversations()
    }

    private fun fetchConversations() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        // 1. Convertimos los documentos a nuestra clase Conversation.
                        val allConversations = snapshots.toObjects(Conversation::class.java)
                        // 2. Filtramos la lista para quedarnos solo con las conversaciones válidas
                        //    (las que sí tienen un timestamp y participantes).
                        val validConversations = allConversations.filter {
                            it.lastMessageTimestamp != null && it.participants.isNotEmpty()
                        }
                        _conversations.value = validConversations
                    }
                }
        }
    }
}