package com.example.favoresapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.favoresapp.ui.Model.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType


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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishServiceScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val categories = listOf("Hogar", "Educación", "Tecnología", "Salud", "Transporte")
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var price by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    val firestore = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Publicar servicio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(autoCorrect = true), // 👈 autocorrección
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                keyboardOptions = KeyboardOptions(autoCorrect = true), // 👈 autocorrección
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
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

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Precio") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )


            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Ubicación") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(autoCorrect = true), // 👈 autocorrección
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (currentUser != null) {
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
                                onSaveSuccess()
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error publicando servicio", e)
                            }
                    } else {
                        Log.e("Firestore", "Usuario no autenticado")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Publicar")
            }
        }
    }
}