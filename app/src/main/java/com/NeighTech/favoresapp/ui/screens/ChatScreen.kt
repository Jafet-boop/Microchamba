package com.NeighTech.favoresapp.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.NeighTech.favoresapp.ui.Model.Message
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- Funciones auxiliares ---
private fun formatDateSeparator(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    return when {
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Hoy"
        calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Ayer"
        else -> SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES")).format(date)
    }
}

@Composable
fun ChatScreen(
    receiverId: String,
    onBack: () -> Unit
) {
    val viewModelFactory = ChatViewModelFactory(receiverId)
    val chatViewModel: ChatViewModel = viewModel(factory = viewModelFactory)

    val messages by chatViewModel.messages.collectAsState()
    val receiverProfile by chatViewModel.receiverProfile.collectAsState()
    val isReceiverTyping by chatViewModel.isReceiverTyping.collectAsState()

    var textState by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val groupedMessages = remember(messages) {
        messages.groupBy { message ->
            val calendar = Calendar.getInstance()
            calendar.time = message.timestamp ?: Date(0)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.time
        }
    }

    // ✅ Box en lugar de Scaffold para mejor control
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding() // ✅ CLAVE: Esto maneja el padding del teclado
        ) {
            // Header fijo
            ChatTopBar(
                contactName = receiverProfile?.fullName ?: "Cargando...",
                onBack = onBack
            )

            // Lista de mensajes
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 8.dp
                )
            ) {
                groupedMessages.forEach { (date, messagesInDay) ->
                    item { DateSeparator(date = formatDateSeparator(date)) }
                    items(messagesInDay) { message ->
                        MessageBubble(message = message)
                    }
                }
            }

            // Indicador de "Escribiendo..."
            AnimatedVisibility(
                visible = isReceiverTyping,
                modifier = Modifier.padding(start = 18.dp, bottom = 4.dp, top = 4.dp)
            ) {
                Text(
                    text = "Escribiendo...",
                    fontSize = 14.sp,
                    color = Color(0xFF667eea),
                    fontWeight = FontWeight.Medium
                )
            }

            // Barra de input
            ChatInputBar(
                text = textState,
                onTextChange = { newText ->
                    textState = newText
                    chatViewModel.updateUserTypingStatus(newText.isNotEmpty())
                },
                onSendClick = {
                    val trimmedText = textState.trim()
                    if (trimmedText.isNotBlank()) {
                        chatViewModel.enviarMensaje(trimmedText)
                        textState = ""
                        chatViewModel.updateUserTypingStatus(false)
                    }
                }
            )
        }
    }

    // Auto-scroll cuando llegan mensajes nuevos
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                val targetIndex = listState.layoutInfo.totalItemsCount - 1
                if (targetIndex >= 0) {
                    listState.animateScrollToItem(targetIndex)
                }
            }
        }
    }
}

// ✅ HEADER FIJO - Siempre visible
@Composable
private fun ChatTopBar(contactName: String, onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF667eea),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding() // ✅ Respeta la barra de estado
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = contactName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DateSeparator(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFE2E8F0)
        ) {
            Text(
                text = date.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                    else it.toString()
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF718096),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun MessageBubble(message: Message) {
    val currentUserId = Firebase.auth.currentUser?.uid
    val isCurrentUser = message.usuarioId == currentUserId
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = message.timestamp?.let { timeFormatter.format(it) } ?: ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isCurrentUser) 4.dp else 16.dp
            ),
            color = if (isCurrentUser) Color(0xFF667eea) else Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = message.texto,
                    modifier = Modifier.weight(1f, fill = false),
                    fontSize = 16.sp,
                    color = if (isCurrentUser) Color.White else Color(0xFF1A202C)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    color = if (isCurrentUser) Color.White.copy(alpha = 0.8f) else Color(0xFF718096)
                )
            }
        }
    }
}

// ✅ Input bar optimizado
@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TextField con mejor contraste
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF5F5F5)
            ) {
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Escribe un mensaje...",
                            color = Color(0xFF9E9E9E)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF667eea),
                        focusedTextColor = Color(0xFF1A202C),
                        unfocusedTextColor = Color(0xFF1A202C)
                    ),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSendClick() })
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botón de enviar
            IconButton(
                onClick = onSendClick,
                enabled = text.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = if (text.isNotBlank()) {
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFBDBDBD), Color(0xFFBDBDBD))
                            )
                        },
                        shape = CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Enviar",
                    tint = Color.White
                )
            }
        }
    }
}