package com.example.favoresapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.favoresapp.ui.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(navController: NavController, onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var user by remember { mutableStateOf(User()) }
    var isLoading by remember { mutableStateOf(true) }

    // Leer datos de Firestore
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val data = document.toObject(User::class.java)
                        data?.let { user = it }
                    }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileScreen", "Error cargando datos", e)
                    isLoading = false
                }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Mi Perfil", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Nombre: ${user.fullName.ifEmpty { "No especificado" }}")
            Text(text = "Ubicación: ${user.location.ifEmpty { "No especificado" }}")
            Text(text = "Teléfono: ${user.phone.ifEmpty { "No especificado" }}")
            Text(text = "Presentación: ${user.presentation.ifEmpty { "No especificado" }}")
            Text(text = "Género: ${user.gender.ifEmpty { "No especificado" }}")

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { navController.navigate("editProfile") }) {
                Text("Editar Perfil")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { auth.signOut(); navController.navigate("login") }) {
                Text("Cerrar Sesión")
            }
        }
    }
}
