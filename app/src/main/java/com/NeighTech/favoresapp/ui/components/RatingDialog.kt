package com.NeighTech.favoresapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

@Composable
fun RatingDialog(
    onDismiss: () -> Unit,
    onSubmit: (rating: Float, comment: String) -> Unit,
    userName: String = "este usuario"
) {
    var selectedRating by remember { mutableStateOf(0f) }
    var comment by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¿Cómo fue tu experiencia?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A202C),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Califica a $userName",
                    fontSize = 14.sp,
                    color = Color(0xFF718096),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Estrellas
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 1..5) {
                        StarButton(
                            index = i,
                            isSelected = i <= selectedRating.toInt(),
                            onClick = { selectedRating = i.toFloat() }
                        )
                    }
                }

                if (selectedRating > 0) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = getRatingText(selectedRating),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = getRatingColor(selectedRating)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Campo de comentario
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    textStyle = TextStyle(color = Color.Black),
                    placeholder = {
                        Text(
                            "Comparte tu experiencia (opcional)",
                            color = Color(0xFF718096)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF667eea),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 🔴 Botón Cancelar
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White, // texto blanco
                            containerColor = Color(0xFFFF6B6B) // rojo
                        ),
                        enabled = !isSubmitting
                    ) {
                        Text("Cancelar", fontSize = 12.sp)
                    }

                    // 🟢 Botón Enviar
                    Button(
                        onClick = {
                            if (selectedRating > 0) {
                                isSubmitting = true
                                onSubmit(selectedRating, comment)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF34A853), // verde
                            disabledContainerColor = Color(0xFF34A853).copy(alpha = 0.5f),
                            contentColor = Color.Black // texto negro
                        ),
                        enabled = selectedRating > 0 && !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Enviar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StarButton(
    index: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }

    Icon(
        imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star,
        contentDescription = "Star $index",
        tint = if (isSelected) Color(0xFFD69E2E) else Color(0xFFE2E8F0),
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            }
            .padding(4.dp)
    )
}

private fun getRatingText(rating: Float): String {
    return when (rating.toInt()) {
        1 -> "Muy malo"
        2 -> "Malo"
        3 -> "Regular"
        4 -> "Bueno"
        5 -> "¡Excelente!"
        else -> ""
    }
}

private fun getRatingColor(rating: Float): Color {
    return when (rating.toInt()) {
        1 -> Color(0xFFE53E3E)
        2 -> Color(0xFFED8936)
        3 -> Color(0xFFD69E2E)
        4 -> Color(0xFF48BB78)
        5 -> Color(0xFF38A169)
        else -> Color(0xFF718096)
    }
}