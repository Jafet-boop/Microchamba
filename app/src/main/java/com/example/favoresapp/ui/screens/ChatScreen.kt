package com.example.favoresapp.ui.screens.chat

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.favoresapp.ui.Model.Message
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- Funciones auxiliares (sin cambios) ---
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
            calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
            calendar.time
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))))
    ) {
        Column(Modifier.fillMaxSize()) {
            ChatTopBar(
                contactName = receiverProfile?.fullName ?: "Cargando...",
                onBack = onBack
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            ) {
                groupedMessages.forEach { (date, messagesInDay) ->
                    item { DateSeparator(date = formatDateSeparator(date)) }
                    items(messagesInDay) { message -> MessageBubble(message = message) }
                }
            }

            AnimatedVisibility(
                visible = isReceiverTyping,
                modifier = Modifier.padding(start = 18.dp, bottom = 4.dp, top = 4.dp)
            ) {
                Text(
                    text = "Escribiendo...",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

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

    LaunchedEffect(messages) {
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

// --- El resto de tus Composables (ChatTopBar, MessageBubble, etc.) no cambian ---
@Composable private fun ChatTopBar(contactName: String, onBack: () -> Unit) {
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
@Composable private fun DateSeparator(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE2E8F0))
        ) {
            Text(
                text = date.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF718096),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
@Composable private fun MessageBubble(message: Message) {
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
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isCurrentUser) 16.dp else 0.dp,
                bottomEnd = if (isCurrentUser) 0.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isCurrentUser) Color(0xFF667eea) else Color.White
            )
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
                    fontSize = 12.sp,
                    color = if (isCurrentUser) Color.White.copy(alpha = 0.7f) else Color.Gray,
                )
            }
        }
    }
}
@Composable private fun ChatInputBar(text: String, onTextChange: (String) -> Unit, onSendClick: () -> Unit) {
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
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSendClick() })
            )
            IconButton(
                onClick = onSendClick,
                enabled = text.isNotBlank(),
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