package com.example.favoresapp.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.favoresapp.ui.Model.Message
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    receiverId: String,
    onBack: () -> Unit
) {
    val viewModelFactory = ChatViewModelFactory(receiverId)
    val chatViewModel: ChatViewModel = viewModel(factory = viewModelFactory)

    val messages by chatViewModel.messages.collectAsState()
    val receiverProfile by chatViewModel.receiverProfile.collectAsState()

    var textState by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFE2E8F0)
                    )
                )
            )
    ) {
        Column(Modifier.fillMaxSize()) {
            ChatTopBar(
                contactName = receiverProfile?.fullName ?: "Cargando...",
                onBack = onBack
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                items(messages) { message ->
                    MessageBubble(message = message)
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            ChatInputBar(
                text = textState,
                onTextChange = { textState = it },
                onSendClick = {
                    chatViewModel.enviarMensaje(textState)
                    textState = ""
                }
            )
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
}

@Composable
private fun ChatTopBar(contactName: String, onBack: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(Color(0xFF667eea).copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color(0xFF667eea))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = contactName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A202C),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
private fun MessageBubble(message: Message) {
    val currentUserId = Firebase.auth.currentUser?.uid
    // Determina si el mensaje es del usuario actual para alinearlo a la derecha.
    val isCurrentUser = message.usuarioId == currentUserId

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        // Alinea la burbuja a la derecha si es del usuario, si no, a la izquierda.
        contentAlignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCurrentUser) 16.dp else 0.dp,
                bottomEnd = if (isCurrentUser) 0.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                // Cambia el color para distinguir los mensajes.
                containerColor = if (isCurrentUser) Color(0xFF667eea) else Color.White
            )
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(
                    text = message.nombreUsuario,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentUser) Color.White.copy(alpha = 0.8f) else Color(0xFF667eea)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.texto,
                    fontSize = 16.sp,
                    color = if (isCurrentUser) Color.White else Color(0xFF1A202C)
                )
            }
        }
    }
}


@Composable
private fun ChatInputBar(text: String, onTextChange: (String) -> Unit, onSendClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe un mensaje...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF667eea)
                ),
                maxLines = 4
            )
            IconButton(
                onClick = onSendClick,
                enabled = text.isNotBlank(), // El botón solo se activa si hay texto.
                modifier = Modifier
                    .padding(start = 8.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = if (text.isNotBlank()) listOf(Color(0xFF667eea), Color(0xFF764ba2))
                            else listOf(Color.Gray, Color.Gray)
                        ),
                        shape = CircleShape
                    )
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
            }
        }
    }
}