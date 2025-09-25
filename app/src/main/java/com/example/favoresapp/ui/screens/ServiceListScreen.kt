package com.example.favoresapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.favoresapp.ui.Model.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun ServiceListScreen(onBack: () -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    var services by remember { mutableStateOf<List<Service>>(emptyList()) }
    var listenerRegistration: ListenerRegistration? = null
    var selectedTab by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    // Escucha en tiempo real
    LaunchedEffect(Unit) {
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Servicios Disponibles", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { selectedTab = 0 }) { Text("Pendientes") }
            Button(onClick = { selectedTab = 1 }) { Text("En Progreso") }
            Button(onClick = { selectedTab = 2 }) { Text("Completados") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (filteredServices.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay servicios en esta categoría")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredServices) { service ->
                        ServiceItem(service = service, firestore = firestore)
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceItem(service: Service, firestore: FirebaseFirestore) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text("Título: ${service.title}", style = MaterialTheme.typography.titleMedium)
        Text("Descripción: ${service.description}")
        Text("Categoría: ${service.category}")
        if (service.price.isNotBlank()) Text("Precio: ${service.price}")
        if (service.location.isNotBlank()) Text("Ubicación: ${service.location}")
        Text("Estado: ${service.status}")

        Spacer(modifier = Modifier.height(8.dp))

        when (service.status) {
            "pendiente" -> {
                if (currentUser != null) {
                    Button(onClick = {
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
                    }) {
                        Text("Aceptar Trabajo")
                    }
                }
            }
            "en progreso" -> {
                if (service.acceptedBy == currentUser?.uid) {
                    Button(onClick = {
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
                    }) {
                        Text("Marcar como Completado")
                    }
                } else {
                    Text("Trabajo en Progreso")
                }
            }
            "completado" -> {
                Text("Trabajo Completado")
            }
        }
    }
}
