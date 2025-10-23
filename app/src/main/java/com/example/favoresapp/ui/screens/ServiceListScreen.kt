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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import com.example.favoresapp.ui.Model.Notification
import com.google.firebase.firestore.Query
import kotlinx.coroutines.delay
import com.example.favoresapp.ui.Model.User
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.Person


data class ServiceStatus(
    val name: String,
    val color: Color,
    val icon: ImageVector,
    val gradient: List<Color>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceListScreen(onBack: () -> Unit,
                      onPublisherClick: (publisherId: String) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    var allServices by remember { mutableStateOf<List<Service>>(emptyList()) } // 🆕 Todos los servicios
    var serviceDocIds by remember { mutableStateOf<Map<Service, String>>(emptyMap()) }
    var listenerRegistration: ListenerRegistration? = null
    var selectedTab by remember { mutableIntStateOf(0) }
    var showContent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var showApplicantsDialog by remember { mutableStateOf(false) }
    var selectedService by remember { mutableStateOf<Pair<String, Service>?>(null) }

    val statusOptions = remember {
        listOf(
            ServiceStatus(
                "Pendiente", // 👈 Este está bien
                Color(0xFF667eea),
                Icons.Default.Schedule,
                listOf(Color(0xFF667eea), Color(0xFF764ba2))
            ),
            ServiceStatus(
                "En Curso", // 👈 Más corto
                Color(0xFF36d1dc),
                Icons.Default.Work,
                listOf(Color(0xFF36d1dc), Color(0xFF5b86e5))
            ),
            ServiceStatus(
                "Confirmar", // 👈 Más corto
                Color(0xFFFF9800),
                Icons.Default.HourglassEmpty,
                listOf(Color(0xFFFF9800), Color(0xFFFF6F00))
            ),
            ServiceStatus(
                "Completo", // 👈 Más corto
                Color(0xFF34A853),
                Icons.Default.CheckCircle,
                listOf(Color(0xFF34A853), Color(0xFF4CAF50))
            )
        )
    }
    val categories = listOf("Hogar", "Educación", "Tecnología", "Salud", "Transporte")

    // 🆕 Escucha TODOS los servicios sin filtro de estado
    LaunchedEffect(Unit) {
        delay(200)
        showContent = true
        isLoading = true

        listenerRegistration?.remove()
        listenerRegistration = firestore.collection("services")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firestore", "Error obteniendo servicios", e)
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val servicesList = snapshot.toObjects(Service::class.java)
                    val idsMap = mutableMapOf<Service, String>()

                    snapshot.documents.forEachIndexed { index, doc ->
                        if (index < servicesList.size) {
                            idsMap[servicesList[index]] = doc.id
                        }
                    }

                    allServices = servicesList
                    serviceDocIds = idsMap
                    isLoading = false
                }
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            listenerRegistration?.remove()
        }
    }

    // 🆕 Filtrar servicios en el cliente
    val filteredServices = remember(allServices, selectedTab, selectedCategory, searchQuery) {
        var filtered = allServices

        // Filtro por estado
        filtered = when (selectedTab) {
            0 -> filtered.filter { it.status == "pendiente" }
            1 -> filtered.filter { it.status == "en progreso" }
            2 -> filtered.filter { it.status == "pendiente_confirmacion" }
            3 -> filtered.filter { it.status == "completado" }
            else -> filtered
        }

        // Filtro por categoría
        if (selectedCategory != null) {
            filtered = filtered.filter { it.category == selectedCategory }
        }

        // Filtro por búsqueda
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }

        filtered
    }

    if (showApplicantsDialog && selectedService != null) {
        ApplicantsDialog(
            serviceId = selectedService!!.first,
            service = selectedService!!.second,
            firestore = firestore,
            onDismiss = {
                showApplicantsDialog = false
                selectedService = null
            }
        )
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
            CustomTopBar(onBack = onBack, totalServices = allServices.size)
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
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Buscar...") },
                            modifier = Modifier.weight(1f),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = Color.Black
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(
                                onClick = { expanded = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "Categoría",
                                    tint = Color.Black
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todas") },
                                    onClick = {
                                        selectedCategory = null
                                        expanded = false
                                    }
                                )
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category) },
                                        onClick = {
                                            selectedCategory = category
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomTabSection(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        statusOptions = statusOptions,
                        allServices = allServices // 🆕 Pasar TODOS los servicios
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                                    val docId = serviceDocIds[service] ?: ""
                                    ServiceCard(
                                        service = service,
                                        serviceId = docId,
                                        firestore = firestore,
                                        statusOptions = statusOptions,
                                        onViewApplicants = { selectedSrv ->
                                            val id = serviceDocIds[selectedSrv] ?: ""
                                            if (id.isNotEmpty()) {
                                                selectedService = Pair(id, selectedSrv)
                                                showApplicantsDialog = true
                                            } else {
                                                Log.e("ServiceList", "No se encontró el ID del servicio")
                                            }
                                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicantsDialog(
    serviceId: String,
    service: Service,
    firestore: FirebaseFirestore,
    onDismiss: () -> Unit
) {
    var applicants by remember { mutableStateOf<List<Pair<String, User>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(service.applicants) {
        if (service.applicants.isEmpty()) {
            isLoading = false
            return@LaunchedEffect
        }

        val loadedApplicants = mutableListOf<Pair<String, User>>()
        var processedCount = 0

        service.applicants.forEach { userId ->
            firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = document.toObject(User::class.java)
                        if (user != null) {
                            loadedApplicants.add(Pair(userId, user))
                        }
                    }
                    processedCount++
                    if (processedCount == service.applicants.size) {
                        applicants = loadedApplicants.toList()
                        isLoading = false
                    }
                }
                .addOnFailureListener {
                    processedCount++
                    if (processedCount == service.applicants.size) {
                        applicants = loadedApplicants.toList()
                        isLoading = false
                    }
                }
        }
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.8f)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Postulaciones",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A202C)
                        )
                        Text(
                            service.title,
                            fontSize = 14.sp,
                            color = Color(0xFF718096),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF718096)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF667eea))
                    }
                } else if (applicants.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.PersonSearch,
                                contentDescription = null,
                                tint = Color(0xFF718096),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Sin postulaciones aún",
                                fontSize = 16.sp,
                                color = Color(0xFF718096)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        itemsIndexed(applicants) { _, (userId, user) ->
                            ApplicantCardCompact(
                                userId = userId,
                                user = user,
                                serviceId = serviceId,
                                firestore = firestore,
                                onAccepted = onDismiss
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplicantCardCompact(
    userId: String,
    user: User,
    serviceId: String,
    firestore: FirebaseFirestore,
    onAccepted: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.fullName.firstOrNull()?.uppercase() ?: "U",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.fullName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A202C)
                    )
                    if (user.location.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF718096),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = user.location,
                                fontSize = 12.sp,
                                color = Color(0xFF718096)
                            )
                        }
                    }
                }
            }

            if (user.phone.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color(0xFF718096),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = user.phone,
                        fontSize = 12.sp,
                        color = Color(0xFF718096)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF34A853), Color(0xFF4CAF50))
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Aceptar",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Aceptar Postulación",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF34A853),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Confirmar Postulación",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("¿Deseas aceptar a ${user.fullName} para realizar este trabajo?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        firestore.collection("services")
                            .document(serviceId)
                            .update(
                                mapOf(
                                    "status" to "en progreso",
                                    "acceptedBy" to userId
                                )
                            )
                            .addOnSuccessListener {
                                Log.d("ApplicantsDialog", "Usuario aceptado correctamente")
                                showDialog = false
                                onAccepted()
                            }
                            .addOnFailureListener { e ->
                                Log.e("ApplicantsDialog", "Error al aceptar usuario", e)
                            }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF34A853)
                    )
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar", color = Color(0xFF718096))
                }
            }
        )
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
                    Icons.AutoMirrored.Filled.ArrowBack,
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
    allServices: List<Service> // 🆕 Recibe TODOS los servicios
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                // 🆕 Contar sobre TODOS los servicios, no sobre filtrados
                val serviceCount = when (index) {
                    0 -> allServices.count { it.status == "pendiente" }
                    1 -> allServices.count { it.status == "en progreso" }
                    2 -> allServices.count { it.status == "pendiente_confirmacion" }
                    3 -> allServices.count { it.status == "completado" }
                    else -> 0
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .clickable { onTabSelected(index) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
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
                            .padding(vertical = 12.dp, horizontal = 8.dp),
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
                                text = status.name,
                                fontSize = 10.sp, // 🆕 Texto más pequeño
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF718096),
                                maxLines = 2, // 🆕 Permite 2 líneas
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 10.sp // 🆕 Altura de línea ajustada
                            )
                            Spacer(modifier = Modifier.height(2.dp))
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
    serviceId: String,
    firestore: FirebaseFirestore,
    statusOptions: List<ServiceStatus>,
    onViewApplicants: (Service) -> Unit = {}
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var userProfile by remember { mutableStateOf<User?>(null) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(service.userId) {
        if (service.userId.isNotEmpty()) {
            firestore.collection("users")
                .document(service.userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userProfile = document.toObject(User::class.java)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ServiceCard", "Error cargando perfil", e)
                }
        }
    }

    val statusData = when (service.status) {
        "pendiente" -> statusOptions[0]
        "en progreso" -> statusOptions[1]
        "pendiente_confirmacion" -> statusOptions[2]
        "completado" -> statusOptions[3]
        else -> statusOptions[0]
    }

    // 🆕 Diálogo para marcar como completado
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "¿Trabajo Terminado?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("El dueño del servicio recibirá una notificación para confirmar que el trabajo se completó correctamente.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (serviceId.isNotEmpty()) {
                            firestore.collection("services")
                                .document(serviceId)
                                .update(
                                    mapOf(
                                        "status" to "pendiente_confirmacion",
                                        "completedBy" to currentUser?.uid
                                    )
                                )
                                .addOnSuccessListener {
                                    Log.d("ServiceCard", "Marcado como pendiente de confirmación")

                                    // Crear notificación al dueño
                                    firestore.collection("users").document(currentUser?.uid ?: "").get()
                                        .addOnSuccessListener { userDoc ->
                                            val workerName = userDoc.getString("fullName") ?: "El trabajador"

                                            val notification = Notification(
                                                recipientId = service.userId,
                                                senderId = currentUser?.uid ?: "",
                                                senderName = workerName,
                                                serviceId = serviceId,
                                                serviceTitle = service.title,
                                                type = "work_completed"
                                            )
                                            firestore.collection("notifications").add(notification)
                                        }

                                    showCompleteDialog = false
                                }
                                .addOnFailureListener { e ->
                                    Log.e("ServiceCard", "Error al marcar como pendiente", e)
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    )
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Cancelar", color = Color(0xFF718096))
                }
            }
        )
    }

// 🆕 Diálogo para confirmar trabajo completado
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF34A853),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Confirmar Trabajo",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("¿Confirmas que el trabajo se completó satisfactoriamente?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (serviceId.isNotEmpty()) {
                            firestore.collection("services")
                                .document(serviceId)
                                .update("status", "completado")
                                .addOnSuccessListener {
                                    Log.d("ServiceCard", "Trabajo confirmado como completado")

                                    // Notificar al trabajador
                                    firestore.collection("users").document(service.userId).get()
                                        .addOnSuccessListener { userDoc ->
                                            val ownerName = userDoc.getString("fullName") ?: "El dueño"

                                            val notification = Notification(
                                                recipientId = service.acceptedBy ?: "",
                                                senderId = currentUser?.uid ?: "",
                                                senderName = ownerName,
                                                serviceId = serviceId,
                                                serviceTitle = service.title,
                                                type = "work_confirmed"
                                            )
                                            firestore.collection("notifications").add(notification)
                                        }

                                    showConfirmDialog = false
                                }
                                .addOnFailureListener { e ->
                                    Log.e("ServiceCard", "Error al confirmar trabajo", e)
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF34A853)
                    )
                ) {
                    Text("Sí, Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar", color = Color(0xFF718096))
                }
            }
        )
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
                                text = when(service.status) {
                                    "pendiente_confirmacion" -> "Por Confirmar"
                                    else -> service.status.replaceFirstChar { it.uppercase() }
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))


            userProfile?.let { profile ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)) // 1. Recorta la forma para que el efecto "ripple" sea redondeado
                        .clickable(onClick = onPublisherClick), // 2. ¡Aquí se añade la acción de clic!
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF8FAFC)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .size(40.dp)
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
                            Text(
                                text = profile.fullName.firstOrNull()?.uppercase() ?: "U",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Publicado por",
                                fontSize = 11.sp,
                                color = Color(0xFF718096),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = profile.fullName.ifEmpty { "Usuario" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A202C),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (profile.location.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color(0xFF718096),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = profile.location,
                                        fontSize = 11.sp,
                                        color = Color(0xFF718096)
                                    )
                                }
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF667eea),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }


            // Description
            Text(
                text = service.description,
                fontSize = 14.sp,
                color = Color(0xFF718096),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            when (service.status) {
                "pendiente" -> {
                    if (currentUser?.uid == service.userId) {
                        val applicantCount = service.applicants.size

                        Button(
                            onClick = { onViewApplicants(service) },
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
                                    Icons.Default.People,
                                    contentDescription = "Ver postulaciones",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Ver Postulaciones ($applicantCount)",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        val hasApplied = service.applicants.contains(currentUser?.uid)

                        if (hasApplied) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF667eea).copy(alpha = 0.1f)
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
                                        contentDescription = "Ya postulado",
                                        tint = Color(0xFF667eea),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Ya te postulaste a este trabajo",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF667eea),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (currentUser != null && serviceId.isNotEmpty()) {
                                        firestore.collection("users").document(currentUser.uid).get()
                                            .addOnSuccessListener { userDoc ->
                                                val senderName = userDoc.getString("fullName") ?: "Alguien"

                                                val updatedApplicants = service.applicants.toMutableList().apply { add(currentUser.uid) }
                                                firestore.collection("services").document(serviceId)
                                                    .update("applicants", updatedApplicants)
                                                    .addOnSuccessListener {
                                                        Log.d("ServiceCard", "Postulación exitosa")

                                                        val notification = Notification(
                                                            recipientId = service.userId,
                                                            senderId = currentUser.uid,
                                                            senderName = senderName,
                                                            serviceId = serviceId,
                                                            serviceTitle = service.title,
                                                            type = "new_applicant"
                                                        )
                                                        firestore.collection("notifications").add(notification)
                                                            .addOnSuccessListener {
                                                                Log.d("ServiceCard", "Notificación creada exitosamente")
                                                            }
                                                            .addOnFailureListener { e ->
                                                                Log.e("ServiceCard", "Error al crear notificación", e)
                                                            }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e("ServiceCard", "Error al postularse", e)
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("ServiceCard", "Error al obtener nombre del postulante", e)
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
                                        Icons.Default.WorkOutline,
                                        contentDescription = "Postularme",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Postularme al Trabajo",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
                "en progreso" -> {
                    // 🆕 El trabajador puede marcar como "pendiente de confirmación"
                    if (service.acceptedBy == currentUser?.uid) {
                        Button(
                            onClick = { showCompleteDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFF9800),
                                            Color(0xFFFF6F00)
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
                                    Icons.Default.HourglassEmpty,
                                    contentDescription = "Marcar Completado",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "He Terminado el Trabajo",
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
                                    text = if (currentUser?.uid == service.userId) {
                                        "Tu servicio está siendo trabajado"
                                    } else {
                                        "Trabajo en Progreso"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF36d1dc),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
                // 🆕 NUEVO ESTADO: Pendiente de confirmación
                "pendiente_confirmacion" -> {
                    if (currentUser?.uid == service.userId) {
                        // El dueño puede confirmar el trabajo
                        Button(
                            onClick = { showConfirmDialog = true },
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
                                    contentDescription = "Confirmar",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Confirmar Trabajo Completado",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
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
                                    Icons.Default.HourglassEmpty,
                                    contentDescription = "Esperando confirmación",
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Esperando confirmación del dueño",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800),
                                    fontSize = 13.sp
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
                                "Trabajo Completado",
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