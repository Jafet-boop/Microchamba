package com.example.favoresapp.ui.screens.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.favoresapp.ui.Model.Message
import com.example.favoresapp.ui.Model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModelFactory(private val receiverId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(receiverId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class ChatViewModel(private val receiverId: String) : ViewModel() {

    // ... tus variables (db, auth, rtdb, etc.) no cambian ...
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val rtdb = Firebase.database.reference
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()
    private val _receiverProfile = MutableStateFlow<User?>(null)
    val receiverProfile = _receiverProfile.asStateFlow()
    private val _isReceiverTyping = MutableStateFlow(false)
    val isReceiverTyping = _isReceiverTyping.asStateFlow()
    private var chatId: String? = null
    private var typingStatusRef: com.google.firebase.database.DatabaseReference? = null
    private var receiverTypingListener: ValueEventListener? = null
    private var debounceJob: Job? = null

    init {
        obtenerChatId()
        fetchReceiverProfile()
        markConversationAsRead()
    }

    // --- 2. NUEVA FUNCIÓN PARA MARCAR EL CHAT COMO LEÍDO ---
    private fun markConversationAsRead() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null && chatId != null) {
            db.collection("chats").document(chatId!!)
                .update(
                    "lastMessageReadBy", FieldValue.arrayUnion(currentUserId)
                )
                .addOnSuccessListener { Log.d("ChatViewModel", "Chat marcado como leído.") }
                .addOnFailureListener { e -> Log.w("ChatViewModel", "Error al marcar como leído", e) }
        }
    }


    private fun obtenerChatId() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            chatId = if (currentUserId > receiverId) {
                "$currentUserId-$receiverId"
            } else {
                "$receiverId-$currentUserId"
            }
            escucharMensajes()
            setupTypingListeners()
        }
    }

    private fun fetchReceiverProfile() {
        db.collection("users").document(receiverId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    _receiverProfile.value = document.toObject(User::class.java)
                }
            }
    }

    private fun escucharMensajes() {
        chatId?.let { id ->
            db.collection("chats").document(id).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshots, error ->
                    if (error == null && snapshots != null) {
                        _messages.value = snapshots.toObjects(Message::class.java)
                    }
                }
        }
    }

    private fun setupTypingListeners() {
        val currentUserId = auth.currentUser?.uid ?: return
        chatId?.let { id ->
            typingStatusRef = rtdb.child("typing_status").child(id).child(currentUserId)
            typingStatusRef?.onDisconnect()?.setValue(false)
            val receiverTypingRef = rtdb.child("typing_status").child(id).child(receiverId)

            receiverTypingListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _isReceiverTyping.value = snapshot.getValue(Boolean::class.java) ?: false
                }
                override fun onCancelled(error: DatabaseError) {}
            }
            receiverTypingRef.addValueEventListener(receiverTypingListener!!)
        }
    }

    fun updateUserTypingStatus(isTyping: Boolean) {
        debounceJob?.cancel()
        if (isTyping) {
            typingStatusRef?.setValue(true)
            debounceJob = viewModelScope.launch {
                delay(2000L)
                typingStatusRef?.setValue(false)
            }
        } else {
            typingStatusRef?.setValue(false)
        }
    }

    fun enviarMensaje(texto: String) {
        val currentUser = auth.currentUser
        if (currentUser == null || texto.isBlank() || chatId == null) {
            Log.e("ChatViewModel_Send", "No se puede enviar el mensaje. Usuario, texto o chatId nulos.")
            return
        }

        val receiverName = receiverProfile.value?.fullName
        if (receiverName == null) {
            Log.e("ChatViewModel_Send", "¡FALLO! El perfil del receptor aún no se ha cargado.")
            return
        }

        // 🆕 AGREGAR: Cargar el fullName del usuario actual
        viewModelScope.launch {
            try {
                val currentUserDoc = db.collection("users").document(currentUser.uid).get().await()
                val currentUserFullName = currentUserDoc.toObject(User::class.java)?.fullName ?: "Usuario"

                val message = Message(
                    texto = texto,
                    usuarioId = currentUser.uid,
                    nombreUsuario = currentUserFullName  // 🔄 Cambio aquí también
                )
                val chatDocRef = db.collection("chats").document(chatId!!)

                Log.d("ChatViewModel_Send", "Intentando guardar mensaje en lote...")
                db.runBatch { batch ->
                    batch.set(chatDocRef.collection("messages").document(), message)

                    val conversationData = mapOf(
                        "participants" to listOf(currentUser.uid, receiverId),
                        "lastMessageText" to texto,
                        "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                        "lastMessageSenderId" to currentUser.uid,
                        "participantNames" to mapOf(
                            currentUser.uid to currentUserFullName,  // ✅ ARREGLADO
                            receiverId to receiverName
                        ),
                        "lastMessageReadBy" to listOf(currentUser.uid)
                    )
                    batch.set(chatDocRef, conversationData, SetOptions.merge())
                }
                    .addOnSuccessListener { Log.d("ChatViewModel_Send", "¡ÉXITO! Mensaje guardado.") }
                    .addOnFailureListener { e -> Log.e("ChatViewModel_Send", "¡FALLO! El lote no se pudo completar.", e) }
            } catch (e: Exception) {
                Log.e("ChatViewModel_Send", "Error al cargar perfil del usuario actual", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        receiverTypingListener?.let {
            rtdb.child("typing_status").child(chatId ?: "").child(receiverId).removeEventListener(it)
        }
        typingStatusRef?.setValue(false)
    }
}