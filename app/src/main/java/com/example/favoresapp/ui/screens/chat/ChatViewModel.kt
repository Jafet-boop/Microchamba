package com.example.favoresapp.ui.screens.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.favoresapp.ui.Model.Message
import com.example.favoresapp.ui.Model.User // <-- Asegúrate de que esta importación sea correcta
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.asStateFlow

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

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    // --- 1. NUEVO STATEFLOW PARA GUARDAR EL PERFIL DEL OTRO USUARIO ---
    private val _receiverProfile = MutableStateFlow<User?>(null)
    val receiverProfile = _receiverProfile.asStateFlow()


    private var chatId: String? = null

    init {
        obtenerChatId()
        fetchReceiverProfile() // <-- 2. LLAMAMOS A LA NUEVA FUNCIÓN
    }

    // --- 3. NUEVA FUNCIÓN PARA BUSCAR LOS DATOS DEL CONTACTO ---
    private fun fetchReceiverProfile() {
        // Busca en la colección "users" el documento que coincida con el ID del receptor.
        db.collection("users").document(receiverId).get()
            .addOnSuccessListener { document ->
                // Si lo encuentra, convierte el documento a un objeto User y actualiza el StateFlow.
                if (document != null && document.exists()) {
                    _receiverProfile.value = document.toObject(User::class.java)
                } else {
                    Log.w("ChatViewModel", "No se encontró el perfil del receptor con ID: $receiverId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ChatViewModel", "Error al obtener el perfil del receptor", exception)
            }
    }

    private fun obtenerChatId() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            // Algoritmo para crear un ID de chat único y consistente.
            chatId = if (currentUserId > receiverId) {
                "$currentUserId-$receiverId"
            } else {
                "$receiverId-$currentUserId"
            }
            escucharMensajes()
        }
    }

    private fun escucharMensajes() {
        chatId?.let { id ->
            db.collection("chats").document(id).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e("ChatViewModel", "Error al escuchar mensajes", error)
                        return@addSnapshotListener
                    }
                    val messageList = snapshots?.toObjects(Message::class.java) ?: emptyList()
                    _messages.value = messageList
                }
        }
    }

    fun enviarMensaje(texto: String) {
        val currentUser = auth.currentUser
        if (currentUser != null && texto.isNotBlank() && chatId != null) {
            val message = Message(
                texto = texto,
                usuarioId = currentUser.uid,
                nombreUsuario = currentUser.displayName ?: "Anónimo"
            )

            val chatDocRef = db.collection("chats").document(chatId!!)

            db.runBatch { batch ->
                // 1. Añade el nuevo mensaje a la sub-colección 'messages'
                batch.set(chatDocRef.collection("messages").document(), message)

                // 2. Prepara los datos para actualizar el documento principal de la conversación
                val conversationData = mapOf(
                    "participants" to listOf(currentUser.uid, receiverId),
                    "lastMessageText" to texto,
                    "lastMessageTimestamp" to System.currentTimeMillis(),
                    "lastMessageSenderId" to currentUser.uid,
                    "participantNames" to mapOf(
                        currentUser.uid to (currentUser.displayName ?: "Anónimo"),
                        receiverId to (receiverProfile.value?.fullName ?: "")
                    )
                )
                // 3. Actualiza el documento principal del chat
                batch.set(chatDocRef, conversationData, SetOptions.merge())
            }
                .addOnSuccessListener { Log.d("ChatViewModel", "¡Mensaje y conversación actualizados!") }
                .addOnFailureListener { e -> Log.e("ChatViewModel", "Error en la escritura por lotes", e) }
        }
    }
}