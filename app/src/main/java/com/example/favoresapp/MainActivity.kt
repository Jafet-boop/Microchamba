package com.example.favoresapp

import com.example.favoresapp.ui.screens.LoginScreen
import com.example.favoresapp.ui.screens.HomeScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.favoresapp.ui.theme.FavoresAppTheme
import androidx.compose.runtime.*
import androidx.compose.material3.*
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
import com.example.favoresapp.ui.screens.SplashScreen
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa Firebase
        FirebaseApp.initializeApp(this)

        setContent {
            FavoresAppTheme {
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = true
                val statusBarColor = MaterialTheme.colorScheme.primary

                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = statusBarColor,
                        darkIcons = useDarkIcons
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                onNavigateToPublish = { navController.navigate("publishService") },
                onNavigateToFavorList = { navController.navigate("serviceList") },
                onNavigateToChat = { navController.navigate("conversations_screen") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToNotifications = { navController.navigate("notifications") }
            )
        }

        // ... aquí puedes dejar el resto de tus rutas existentes:
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
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }

        composable("editProfile") {
            EditProfileScreen(
                onBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        composable("publishService") {
            PublishServiceScreen(
                onBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        composable("serviceList") {
            ServiceListScreen(
                onBack = { navController.popBackStack() },
                onPublisherClick = { publisherId ->
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    if (publisherId.isNotEmpty() && publisherId != currentUserId) {
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
}