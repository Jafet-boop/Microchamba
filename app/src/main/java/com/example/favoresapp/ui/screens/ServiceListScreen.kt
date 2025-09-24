package com.example.favoresapp.ui.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.example.favoresapp.ui.Model.Service
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import kotlinx.coroutines.delay

data class ServiceStatus(
    val name: String,
    val color: Color,
    val icon: ImageVector,
    val gradient: List<Color>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceListScreen(onBack: () -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    var services by remember { mutableStateOf<List<Service>>(emptyList()) }
    var listenerRegistration: ListenerRegistration? = null
    var selectedTab by remember { mutableStateOf(0) }
    var showContent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val statusOptions = remember {
        listOf(
            ServiceStatus(
                "Pendientes",
                Color(0xFF667eea),
                Icons.Default.Schedule,
                listOf(Color(0xFF667eea), Color(0xFF764ba2))
            ),
            ServiceStatus(
                "En Progreso",
                Color(0xFF36d1dc),
                Icons.Default.Work,
                listOf(Color(0xFF36d1dc), Color(0xFF5b86e5))
            ),
            ServiceStatus(
                "Completadas",
                Color(0xFF34A853),
                Icons.Default.CheckCircle,
                listOf(Color(0xFF34A853), Color(0xFF4CAF50))
            )
        )
    }

    // Escucha en tiempo real
    LaunchedEffect(Unit) {
        delay(200)
        showContent = true

        listenerRegistration = firestore.collection("services")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firestore", "Error obteniendo servicios", e)
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    services = snapshot.toObjects(Service::class.java)
                    isLoading = false
                }
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            listenerRegistration?.remove()
        }
    }

    // Filtrar servicios según la pestaña
    val filteredServices = when (selectedTab) {
        0 -> services.filter { it.status == "pendiente" }
        1 -> services.filter { it.status == "en progreso" }
        2 -> services.filter { it.status == "completado" }
        else -> services
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
            CustomTopBar(onBack = onBack, totalServices = services.size)
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Tab Section
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 200)) +
                        slideInVertically(
                            animationSpec = tween(800, delayMillis = 200),
                            initialOffsetY = { it / 2 }
                        )
            ) {
                CustomTabSection(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    statusOptions = statusOptions,
                    services = services
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Services List
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF667eea)
                    )
                }
            } else {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(800, delayMillis = 400))
                ) {
                    if (filteredServices.isEmpty()) {
                        EmptyStateSection(
                            selectedStatus = statusOptions[selectedTab]
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(filteredServices) { index, service ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(
                                        animationSpec = tween(600, delayMillis = index * 100)
                                    ) + slideInVertically(
                                        animationSpec = tween(600, delayMillis = index * 100),
                                        initialOffsetY = { it / 2 }
                                    )
                                ) {
                                    ServiceCard(
                                        service = service,
                                        firestore = firestore,
                                        statusOptions = statusOptions
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomTopBar(
    onBack: () -> Unit,
    totalServices: Int
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
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Servicios Disponibles",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A202C)
                )
                Text(
                    "$totalServices servicios publicados",
                    fontSize = 14.sp,
                    color = Color(0xFF718096)
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF667eea).copy(alpha = 0.2f),
                                Color(0xFF764ba2).copy(alpha = 0.1f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.WorkOutline,
                    contentDescription = "Servicios",
                    tint = Color(0xFF667eea),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun CustomTabSection(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    statusOptions: List<ServiceStatus>,
    services: List<Service>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            statusOptions.forEachIndexed { index, status ->
                val isSelected = selectedTab == index
                val serviceCount = when (index) {
                    0 -> services.count { it.status == "pendiente" }
                    1 -> services.count { it.status == "en progreso" }
                    2 -> services.count { it.status == "completado" }
                    else -> 0
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .clickable { onTabSelected(index) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color.Transparent else Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = if (isSelected) {
                                    Brush.horizontalGradient(status.gradient)
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Transparent
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = status.icon,
                                contentDescription = status.name,
                                tint = if (isSelected) Color.White else status.color,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = status.name.split(" ").first(),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF718096),
                                maxLines = 1
                            )
                            Text(
                                text = "$serviceCount",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else status.color
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateSection(selectedStatus: ServiceStatus) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            selectedStatus.color.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = selectedStatus.icon,
                        contentDescription = null,
                        tint = selectedStatus.color,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No hay servicios ${selectedStatus.name.lowercase()}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A202C)
                )

                Text(
                    text = "Los servicios aparecerán aquí cuando estén disponibles",
                    fontSize = 14.sp,
                    color = Color(0xFF718096),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ServiceCard(
    service: Service,
    firestore: FirebaseFirestore,
    statusOptions: List<ServiceStatus>
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val statusData = when (service.status) {
        "pendiente" -> statusOptions[0]
        "en progreso" -> statusOptions[1]
        "completado" -> statusOptions[2]
        else -> statusOptions[0]
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = service.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A202C),
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(12.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(statusData.gradient),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = statusData.icon,
                                contentDescription = service.status,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = service.status.replaceFirstChar { it.uppercase() },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = service.description,
                fontSize = 14.sp,
                color = Color(0xFF718096),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Service details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ServiceDetailItem(
                    icon = Icons.Default.Category,
                    label = service.category,
                    color = Color(0xFF9C27B0)
                )

                if (service.price.isNotBlank()) {
                    ServiceDetailItem(
                        icon = Icons.Default.AttachMoney,
                        label = service.price,
                        color = Color(0xFF34A853)
                    )
                }
            }

            if (service.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                ServiceDetailItem(
                    icon = Icons.Default.LocationOn,
                    label = service.location,
                    color = Color(0xFF4285F4)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            when (service.status) {
                "pendiente" -> {
                    if (currentUser != null) {
                        Button(
                            onClick = {
                                firestore.collection("services")
                                    .whereEqualTo("title", service.title)
                                    .whereEqualTo("userId", service.userId)
                                    .get()
                                    .addOnSuccessListener { snapshot ->
                                        if (!snapshot.isEmpty) {
                                            val docId = snapshot.documents[0].id
                                            firestore.collection("services")
                                                .document(docId)
                                                .update(
                                                    mapOf(
                                                        "status" to "en progreso",
                                                        "acceptedBy" to currentUser.uid
                                                    )
                                                )
                                        }
                                    }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
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
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.CheckBox,
                                    contentDescription = "Aceptar",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Aceptar Trabajo",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                "en progreso" -> {
                    if (service.acceptedBy == currentUser?.uid) {
                        Button(
                            onClick = {
                                firestore.collection("services")
                                    .whereEqualTo("title", service.title)
                                    .whereEqualTo("userId", service.userId)
                                    .get()
                                    .addOnSuccessListener { snapshot ->
                                        if (!snapshot.isEmpty) {
                                            val docId = snapshot.documents[0].id
                                            firestore.collection("services")
                                                .document(docId)
                                                .update("status", "completado")
                                        }
                                    }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF34A853),
                                            Color(0xFF4CAF50)
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Completar",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Marcar como Completado",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF36d1dc).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Work,
                                    contentDescription = "En progreso",
                                    tint = Color(0xFF36d1dc),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Trabajo en Progreso",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF36d1dc)
                                )
                            }
                        }
                    }
                }
                "completado" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF34A853).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Completado",
                                tint = Color(0xFF34A853),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "✅ Trabajo Completado",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34A853)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceDetailItem(
    icon: ImageVector,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF718096),
            fontWeight = FontWeight.Medium
        )
    }
}