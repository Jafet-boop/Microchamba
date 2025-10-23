package com.example.favoresapp

import android.app.ActionBar.OnNavigationListener
import com.example.favoresapp.ui.screens.LoginScreen
import com.example.favoresapp.ui.screens.HomeScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.favoresapp.ui.theme.FavoresAppTheme
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.runtime.SideEffect
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.favoresapp.ui.screens.EditProfileScreen
import com.example.favoresapp.ui.screens.NotificationsScreen
import com.example.favoresapp.ui.screens.ProfileScreen
import com.example.favoresapp.ui.screens.PublishServiceScreen
import com.example.favoresapp.ui.screens.ServiceListScreen
import com.example.favoresapp.ui.screens.chat.ChatScreen
import com.example.favoresapp.ui.screens.conversations.ConversationsScreen
import com.google.firebase.FirebaseApp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Inicializar Firebase
        FirebaseApp.initializeApp(this)

        // Edge-to-Edge moderno
        enableEdgeToEdge()

        setContent {
            FavoresAppTheme {

                // ⚡ Composable: control de barra de estado
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = true // iconos blancos
                val statusBarColor = MaterialTheme.colorScheme.primary

                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = statusBarColor,
                        darkIcons = useDarkIcons
                    )
                }

                // Surface principal
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars),
                    color = MaterialTheme.colorScheme.background
                ) {
                    YourAppContent()
                }
            }
        }
    }
}

@Composable
fun YourAppContent() {
    var isLoggedIn by remember { mutableStateOf(false) }
    val navController = rememberNavController()

    if (isLoggedIn) {
        // Si ya inició sesión -> manejamos navegación
        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToPublish = { navController.navigate("publishService") },
                    onNavigateToFavorList = { navController.navigate("serviceList") },
                    onNavigateToChat = { navController.navigate("conversations_screen") },
                    onNavigateToProfile = { navController.navigate("profile") }
                )
            }

            composable("conversations_screen") {
                ConversationsScreen(
                    onConversationClick = { receiverId ->
                        navController.navigate("chat_screen/$receiverId")
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "chat_screen/{receiverId}",
                // Añadí el 'arguments' que es necesario para que esta ruta funcione
                arguments = listOf(navArgument("receiverId") { type = NavType.StringType })
            ) { backStackEntry ->
                val receiverId = backStackEntry.arguments?.getString("receiverId")
                if (receiverId != null) {
                    ChatScreen(
                        receiverId = receiverId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable("profile") {
                ProfileScreen(
                    navController = navController, // 🔹 PASARLO AQUÍ
                    onBack = { navController.popBackStack() }
                )
            }
            composable("editProfile") {
                EditProfileScreen(
                    onBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() } // regresa al perfil
                )
            }
            composable("publishService") {
                PublishServiceScreen(
                    onBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }

            composable("serviceList") {
                ServiceListScreen (
                    onBack = { navController.popBackStack() },

                    onPublisherClick = { publisherId ->
                        if (publisherId.isNotEmpty()) {
                            navController.navigate("chat_screen/$publisherId")
                        }
                    }
                )
            }

            composable("notifications") {
                NotificationsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    } else {
        // Si no está logueado, mostramos la pantalla de Login
        LoginScreen(onLoginSuccess = { isLoggedIn = true })
    }
}