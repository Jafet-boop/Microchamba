import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.example.favoresapp.ui.Model.Service
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceListScreen(onBack: () -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    var services by remember { mutableStateOf<List<Service>>(emptyList()) }
    var listenerRegistration: ListenerRegistration? = null

    // Escucha en tiempo real
    LaunchedEffect(Unit) {
        listenerRegistration = firestore.collection("services")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firestore", "Error obteniendo servicios", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    services = snapshot.toObjects(Service::class.java)
                }
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            listenerRegistration?.remove()
        }
    }

    // Pestañas para filtrar por estado
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pendientes", "En progreso", "Completadas")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Servicios publicados") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {

            // TabRow para filtrar por estado
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filtrar servicios según la pestaña
            val filteredServices = when (selectedTab) {
                0 -> services.filter { it.status == "pendiente" }
                1 -> services.filter { it.status == "en progreso" }
                2 -> services.filter { it.status == "completado" }
                else -> services
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredServices) { service ->
                    ServiceCard(service, firestore)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ServiceCard(service: Service, firestore: FirebaseFirestore) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(service.title, style = MaterialTheme.typography.titleLarge)
            Text(service.description, style = MaterialTheme.typography.bodyMedium)
            Text("Categoría: ${service.category}", style = MaterialTheme.typography.bodySmall)
            Text("Precio: ${service.price}", style = MaterialTheme.typography.bodySmall)
            Text("Ubicación: ${service.location}", style = MaterialTheme.typography.bodySmall)
            Text("Estado: ${service.status}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            if (service.status == "pendiente" && currentUser != null) {
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
                    Text("Aceptar trabajo")
                }
            }

            if (service.status == "en progreso" && service.acceptedBy == currentUser?.uid) {
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
                    Text("Marcar como completado")
                }
            }

            if (service.status == "completado") {
                Text("✅ Trabajo completado")
            }
        }
    }
}