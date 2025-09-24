package com.example.favoresapp.ui.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.ImeAction
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
import com.example.favoresapp.ui.Model.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.Computer

// --- Helper: normaliza texto antes de guardar ---
fun normalizeText(text: String): String {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return ""
    // colapsa espacios múltiples a uno
    val singleSpaced = trimmed.replace(Regex("\\s+"), " ")
    // pasar todo a minúsculas y capitalizar la primera letra
    return singleSpaced.lowercase().replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }
}

data class CategoryOption(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishServiceScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    // Estados del formulario
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Hogar") }
    var price by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    val firestore = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

    val categories = remember {
        listOf(
            CategoryOption("Hogar", Icons.Default.Home, Color(0xFF4285F4)),
            CategoryOption("Educación", Icons.Default.School, Color(0xFF34A853)),
            CategoryOption("Tecnología", Icons.Filled.Computer, Color(0xFF9C27B0)),
            CategoryOption("Salud", Icons.Default.LocalHospital, Color(0xFFE53E3E)),
            CategoryOption("Transporte", Icons.Default.DirectionsCar, Color(0xFFFF6B35))
        )
    }

    val selectedCategoryData = categories.find { it.name == selectedCategory }
        ?: categories.first()

    // Animación de entrada
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

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
                onPublish = {
                    if (currentUser != null && title.isNotBlank() && description.isNotBlank()) {
                        isLoading = true
                        val service = Service(
                            title = normalizeText(title),
                            description = normalizeText(description),
                            category = selectedCategory,
                            price = price.trim(),
                            location = normalizeText(location),
                            userId = currentUser.uid,
                            timestamp = System.currentTimeMillis()
                        )

                        firestore.collection("services")
                            .add(service)
                            .addOnSuccessListener {
                                Log.d("Firestore", "Servicio publicado con éxito")
                                isLoading = false
                                onSaveSuccess()
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error publicando servicio", e)
                                isLoading = false
                            }
                    } else {
                        Log.e("Firestore", "Datos incompletos o usuario no autenticado")
                    }
                },
                isLoading = isLoading,
                isValid = title.isNotBlank() && description.isNotBlank()
            )
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Header Section
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 200)) +
                        slideInVertically(
                            animationSpec = tween(800, delayMillis = 200),
                            initialOffsetY = { it / 2 }
                        )
            ) {
                HeaderSection()
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Form Section
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 400)) +
                        slideInVertically(
                            animationSpec = tween(800, delayMillis = 400),
                            initialOffsetY = { it / 2 }
                        )
            ) {
                FormSection(
                    title = title,
                    onTitleChange = { title = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    selectedCategory = selectedCategory,
                    onCategoryChange = { selectedCategory = it },
                    categories = categories,
                    selectedCategoryData = selectedCategoryData,
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    price = price,
                    onPriceChange = { price = it },
                    location = location,
                    onLocationChange = { location = it }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CustomTopBar(
    onBack: () -> Unit,
    onPublish: () -> Unit,
    isLoading: Boolean,
    isValid: Boolean
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

                Column {
                    Text(
                        "Publicar Servicio",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A202C)
                    )
                    Text(
                        "Comparte tu talento",
                        fontSize = 12.sp,
                        color = Color(0xFF718096)
                    )
                }
            }

            // Publish Button
            Button(
                onClick = onPublish,
                enabled = !isLoading && isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .background(
                        brush = if (isValid) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF34A853),
                                    Color(0xFF4CAF50)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Gray.copy(alpha = 0.3f),
                                    Color.Gray.copy(alpha = 0.3f)
                                )
                            )
                        },
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
                            Icons.Default.Publish,
                            contentDescription = "Publicar",
                            tint = if (isValid) Color.White else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Publicar",
                            color = if (isValid) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF36d1dc),
                            Color(0xFF5b86e5)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.WorkOutline,
                        contentDescription = "Servicio",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        "¡Ofrece tu servicio!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Completa los datos y empieza a ganar",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormSection(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    categories: List<CategoryOption>,
    selectedCategoryData: CategoryOption,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Field
        CustomTextField(
            value = title,
            onValueChange = onTitleChange,
            label = "Título del servicio",
            icon = Icons.Default.Title,
            keyboardOptions = KeyboardOptions(
                autoCorrect = true,
                imeAction = ImeAction.Next
            )
        )

        // Description Field
        CustomTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = "Descripción detallada",
            icon = Icons.Default.Description,
            maxLines = 4,
            minLines = 2,
            keyboardOptions = KeyboardOptions(
                autoCorrect = true,
                imeAction = ImeAction.Next
            )
        )

        // Category Selection
        CategorySelectionField(
            selectedCategory = selectedCategory,
            onCategoryChange = onCategoryChange,
            categories = categories,
            selectedCategoryData = selectedCategoryData,
            expanded = expanded,
            onExpandedChange = onExpandedChange
        )

        // Price Field
        CustomTextField(
            value = price,
            onValueChange = onPriceChange,
            label = "Precio (opcional)",
            icon = Icons.Default.AttachMoney,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        // Location Field
        CustomTextField(
            value = location,
            onValueChange = onLocationChange,
            label = "Ubicación",
            icon = Icons.Default.LocationOn,
            keyboardOptions = KeyboardOptions(
                autoCorrect = true,
                imeAction = ImeAction.Done
            )
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
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
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
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF667eea),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedLabelColor = Color(0xFF667eea),
                    focusedTextColor = Color(0xFF1A202C),
                    unfocusedTextColor = Color(0xFF1A202C),
                    cursorColor = Color(0xFF667eea)
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = maxLines,
                minLines = minLines,
                keyboardOptions = keyboardOptions
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySelectionField(
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    categories: List<CategoryOption>,
    selectedCategoryData: CategoryOption,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
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
                    imageVector = Icons.Default.Category,
                    contentDescription = "Categoría",
                    tint = Color(0xFF667eea),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Categoría del servicio",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF718096)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = onExpandedChange
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = selectedCategoryData.icon,
                            contentDescription = selectedCategory,
                            tint = selectedCategoryData.color,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF667eea),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedTextColor = Color(0xFF1A202C),
                        unfocusedTextColor = Color(0xFF1A202C)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandedChange(false) },
                    modifier = Modifier.background(Color.White)
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = category.icon,
                                        contentDescription = category.name,
                                        tint = category.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        category.name,
                                        color = Color(0xFF1A202C)
                                    )
                                }
                            },
                            onClick = {
                                onCategoryChange(category.name)
                                onExpandedChange(false)
                            }
                        )
                    }
                }
            }
        }
    }
}