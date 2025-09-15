package com.example.favoresapp.ui.screens

import com.example.favoresapp.R
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as Activity
    val auth = FirebaseAuth.getInstance()

    var isLoading by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    // Configuración de Google Sign-In
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    // Launcher para iniciar actividad de Google Sign-In
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(Exception::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { signInResult ->
                    isLoading = false
                    if (signInResult.isSuccessful) {
                        onLoginSuccess()
                    }
                }
        } catch (e: Exception) {
            isLoading = false
            e.printStackTrace()
        }
    }

    // Animación de entrada
    LaunchedEffect(Unit) {
        delay(300)
        showContent = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            )
    ) {
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically(
                initialOffsetY = { it / 2 }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo o icono de la app
                Card(
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(60.dp),
                            ambientColor = Color.Black.copy(alpha = 0.2f)
                        ),
                    shape = RoundedCornerShape(60.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "App Logo",
                            modifier = Modifier.size(60.dp),
                            tint = Color(0xFF667eea)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Título de la app
                Text(
                    text = "TiraParo!!!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Conecta y ayuda a tu comunidad",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Card contenedor del botón de login
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Iniciar Sesión",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D3748),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Usa tu cuenta de Google para continuar",
                            fontSize = 14.sp,
                            color = Color(0xFF718096),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Botón de Google Sign-In
                        Button(
                            onClick = {
                                isLoading = true
                                val signInIntent: Intent = googleSignInClient.signInIntent
                                launcher.launch(signInIntent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4285F4),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Iniciando sesión...")
                            } else {
                                // Aquí puedes usar el ícono de Google si tienes el recurso
                                // Icon(
                                //     painter = painterResource(id = R.drawable.ic_google),
                                //     contentDescription = "Google",
                                //     modifier = Modifier.size(20.dp)
                                // )
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Google",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Continuar con Google",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Texto de términos y condiciones
                        Text(
                            text = "Al continuar, aceptas nuestros Términos de Servicio y Política de Privacidad",
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}