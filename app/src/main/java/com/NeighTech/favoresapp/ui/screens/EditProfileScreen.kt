package com.NeighTech.favoresapp.ui.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.NeighTech.favoresapp.ui.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // Estados del formulario
    var fullName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var presentation by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    // Animación de entrada
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true

        // Cargar datos existentes del usuario
        currentUser?.uid?.let { uid ->
            firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val user = document.toObject(User::class.java)
                        user?.let {
                            fullName = it.fullName
                            location = it.location
                            presentation = it.presentation
                            gender = it.gender
                            phone = it.phone
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("EditProfile", "Error cargando datos", e)
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()  //Importante para el ajuste del teclado
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFE2E8F0)
                    )
                )
            )
    ) {
        // Custom Top Bar
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(600)) +
                    slideInVertically(
                        animationSpec = tween(600),
                        initialOffsetY = { -it }
                    )
        ) {
            CustomTopBar(
                onBack = onBack,
                onSave = {
                    if (currentUser != null) {
                        isLoading = true
                        val userData = User(
                            fullName = fullName,
                            location = location,
                            presentation = presentation,
                            gender = gender,
                            phone = phone
                        )

                        firestore.collection("users")
                            .document(currentUser.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Log.d("Firestore", "Datos guardados correctamente")
                                isLoading = false
                                onSaveSuccess()
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error guardando los datos", e)
                                isLoading = false
                            }
                    } else {
                        Log.e("Firestore", "Usuario no autenticado")
                    }
                },
                isLoading = isLoading
            )
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()  //Ajuste automático del teclado
                .padding(bottom = 120.dp)  //Espacio extra al final
        )  {
            Spacer(modifier = Modifier.height(20.dp))

            // Profile Picture Section
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 200)) +
                        scaleIn(
                            animationSpec = tween(800, delayMillis = 200),
                            initialScale = 0.8f
                        )
            ) {
                ProfilePictureSection()
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Form Fields
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 400)) +
                        slideInVertically(
                            animationSpec = tween(800, delayMillis = 400),
                            initialOffsetY = { it / 2 }
                        )
            ) {
                FormSection(
                    fullName = fullName,
                    onFullNameChange = { fullName = it },
                    location = location,
                    onLocationChange = { location = it },
                    presentation = presentation,
                    onPresentationChange = { presentation = it },
                    gender = gender,
                    onGenderChange = { gender = it },
                    phone = phone,
                    onPhoneChange = { phone = it }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CustomTopBar(
    onBack: () -> Unit,
    onSave: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
            ),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(
                            Color(0xFF667eea).copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color(0xFF667eea)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    "Editar Perfil",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A202C)
                )
            }

            // Save Button
            Button(
                onClick = onSave,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF667eea),
                                Color(0xFF764ba2)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Done,
                            contentDescription = "Guardar",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Guardar",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfilePictureSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.shadow(
                elevation = 12.dp,
                shape = CircleShape
            ),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF667eea),
                                    Color(0xFF764ba2)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Foto",
                            modifier = Modifier.size(50.dp),
                            tint = Color(0xFF667eea)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { /* TODO: Implementar selección de foto */ },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF667eea)
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            )
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "Cambiar foto",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Cambiar foto")
        }
    }
}

@Composable
private fun FormSection(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    presentation: String,
    onPresentationChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Full Name Field
        CustomTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            label = "Nombre completo",
            icon = Icons.Default.Person
        )

        // Location Field
        CustomTextField(
            value = location,
            onValueChange = onLocationChange,
            label = "Ubicación",
            icon = Icons.Default.LocationOn
        )

        // Phone Field
        CustomTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = "Teléfono",
            icon = Icons.Default.Phone
        )

        // Presentation Field
        CustomTextField(
            value = presentation,
            onValueChange = onPresentationChange,
            label = "Presentación",
            icon = Icons.Default.Info,
            maxLines = 3,
            minLines = 2
        )

        // Gender Selection
        GenderSelectionCard(
            selectedGender = gender,
            onGenderChange = onGenderChange
        )
    }
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    maxLines: Int = 1,
    minLines: Int = 1
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color(0xFF667eea),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF718096)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp, max = 200.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF667eea),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedLabelColor = Color(0xFF667eea),
                    focusedTextColor =  Color(0xFF1A202C),
                    unfocusedTextColor = Color(0xFF1A202C),
                    cursorColor = Color(0xFF667eea)
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = maxLines,
                minLines = minLines
            )
        }
    }
}

@Composable
private fun GenderSelectionCard(
    selectedGender: String,
    onGenderChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Género",
                    tint = Color(0xFF667eea),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Género",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF718096)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GenderOption(
                    text = "Hombre",
                    selected = selectedGender == "Hombre",
                    onSelect = { onGenderChange("Hombre") },
                    modifier = Modifier.weight(1f)
                )

                GenderOption(
                    text = "Mujer",
                    selected = selectedGender == "Mujer",
                    onSelect = { onGenderChange("Mujer") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun GenderOption(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = onSelect
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                Color(0xFF667eea).copy(alpha = 0.1f)
            } else {
                Color(0xFFF8FAFC)
            }
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = if (selected) 2.dp else 1.dp,
            brush = if (selected) {
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            } else {
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFE2E8F0),
                        Color(0xFFE2E8F0)
                    )
                )
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF667eea),
                    unselectedColor = Color(0xFF718096)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color(0xFF667eea) else Color(0xFF718096)
            )
        }
    }
}