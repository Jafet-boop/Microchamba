package com.example.favoresapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class BottomNavItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    onNavigateToPublish: () -> Unit = {},
    onNavigateToFavorList: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val bottomNavItems = remember {
        listOf(
            BottomNavItem("Publicar", Icons.Default.Add, onNavigateToPublish),
            BottomNavItem("Servicios", Icons.Default.Search, onNavigateToFavorList),
            BottomNavItem("Chat", Icons.Default.Email, onNavigateToChat),
            BottomNavItem("Perfil", Icons.Default.Person, onNavigateToProfile)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "¡Hola! 👋")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "¿Cómo puedes ayudar hoy?")

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Tu Actividad")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Favores realizados: 0")
        Text(text = "Calificación promedio: 0.0")
        Text(text = "Personas ayudadas: 0")

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Actividad Reciente")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "¡Empieza a ayudar! Publica tu primer servicio o explora lo que otros necesitan")

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Navigation simplificado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            bottomNavItems.forEach { item ->
                Button(onClick = item.onClick, modifier = Modifier.weight(1f)) {
                    Text(item.title)
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}
