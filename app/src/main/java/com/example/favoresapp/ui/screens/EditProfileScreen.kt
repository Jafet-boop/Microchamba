package com.example.favoresapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.favoresapp.ui.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

    // Cargar datos existentes del usuario
    LaunchedEffect(currentUser?.uid) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "Editar Perfil", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Ubicación") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = presentation,
            onValueChange = { presentation = it },
            label = { Text("Presentación") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = gender,
            onValueChange = { gender = it },
            label = { Text("Género") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }

            Button(
                onClick = {
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
                                Log.e("Firestore", "Error guardando datos", e)
                                isLoading = false
                            }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Guardar")
                }
            }
        }
    }
}
