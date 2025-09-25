package com.example.favoresapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.favoresapp.ui.Model.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

fun normalizeText(text: String): String {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return ""
    val singleSpaced = trimmed.replace(Regex("\\s+"), " ")
    return singleSpaced.lowercase().replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }
}

@Composable
fun PublishServiceScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Hogar") }
    var price by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val firestore = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "Publicar Servicio", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título del servicio") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Categoría") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Precio (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Ubicación") },
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
                    if (currentUser != null && title.isNotBlank() && description.isNotBlank()) {
                        isLoading = true
                        val service = Service(
                            title = normalizeText(title),
                            description = normalizeText(description),
                            category = normalizeText(category),
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
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Publicar")
                }
            }
        }
    }
}
