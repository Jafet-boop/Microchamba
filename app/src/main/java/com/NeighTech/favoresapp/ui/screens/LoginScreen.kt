package com.NeighTech.favoresapp.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.NeighTech.favoresapp.ui.ViewModels.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Error
import com.NeighTech.favoresapp.R


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Login, 1 = Registro
    var showContent by remember { mutableStateOf(false) }

    // Estados para Login
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Estados para Registro
    var registerName by remember { mutableStateOf("") }
    var registerEmail by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }
    var registerPasswordVisible by remember { mutableStateOf(false) }
    var registerConfirmPassword by remember { mutableStateOf("") }
    var registerConfirmPasswordVisible by remember { mutableStateOf(false) }

    // Estados generales
    var isGoogleLoading by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()

    // Configuración de Google Sign-In
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(Exception::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { signInResult ->
                    isGoogleLoading = false
                    if (signInResult.isSuccessful) {
                        onLoginSuccess()
                    }
                }
        } catch (e: Exception) {
            isGoogleLoading = false
            e.printStackTrace()
        }
    }

    // Animación de entrada
    LaunchedEffect(Unit) {
        delay(300)
        showContent = true
    }

    // Manejar éxito de autenticación
    LaunchedEffect(authState.isSuccess) {
        if (authState.isSuccess) {
            delay(500)
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxSize()
                .blur(2.dp), // Desenfoque sutil
            contentScale = ContentScale.Crop
        )

        // Overlay oscuro para mejorar legibilidad
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        // Contenido
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Logo
                Card(
                    modifier = Modifier
                        .size(100.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = CircleShape
                        ),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF9FA8DA)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Título
                Text(
                    text = "TiraParo!!!",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Conecta y ayuda a tu comunidad",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Card principal
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Tabs
                        CustomTabRow(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Contenido según tab
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
                                fadeIn() + slideInHorizontally() with
                                        fadeOut() + slideOutHorizontally()
                            }
                        ) { tab ->
                            when (tab) {
                                0 -> LoginContent(
                                    email = loginEmail,
                                    onEmailChange = { loginEmail = it },
                                    password = loginPassword,
                                    onPasswordChange = { loginPassword = it },
                                    passwordVisible = loginPasswordVisible,
                                    onPasswordVisibilityChange = { loginPasswordVisible = !loginPasswordVisible },
                                    isLoading = authState.isLoading,
                                    onLogin = {
                                        scope.launch {
                                            authViewModel.loginWithEmail(loginEmail, loginPassword)
                                        }
                                    },
                                    onForgotPassword = { showForgotPasswordDialog = true },
                                    authViewModel = authViewModel
                                )

                                1 -> RegisterContent(
                                    name = registerName,
                                    onNameChange = { registerName = it },
                                    email = registerEmail,
                                    onEmailChange = { registerEmail = it },
                                    password = registerPassword,
                                    onPasswordChange = { registerPassword = it },
                                    passwordVisible = registerPasswordVisible,
                                    onPasswordVisibilityChange = { registerPasswordVisible = !registerPasswordVisible },
                                    confirmPassword = registerConfirmPassword,
                                    onConfirmPasswordChange = { registerConfirmPassword = it },
                                    confirmPasswordVisible = registerConfirmPasswordVisible,
                                    onConfirmPasswordVisibilityChange = { registerConfirmPasswordVisible = !registerConfirmPasswordVisible },
                                    isLoading = authState.isLoading,
                                    onRegister = {
                                        scope.launch {
                                            authViewModel.registerWithEmail(
                                                registerEmail,
                                                registerPassword,
                                                registerName
                                            )
                                        }
                                    },
                                    authViewModel = authViewModel
                                )
                            }
                        }

                        // Mensaje de error
                        authState.error?.let { error ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE53E3E).copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = Color(0xFFE53E3E),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = error,
                                        color = Color(0xFFE53E3E),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Divider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Divider(modifier = Modifier.weight(1f))
                            Text(
                                text = "  o continúa con  ",
                                fontSize = 12.sp,
                                color = Color(0xFF718096)
                            )
                            Divider(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Botón de Google
                        OutlinedButton(
                            onClick = {
                                isGoogleLoading = true
                                val signInIntent = googleSignInClient.signInIntent
                                launcher.launch(signInIntent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = !isGoogleLoading && !authState.isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF1A202C)
                            )
                        ) {
                            if (isGoogleLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF4285F4)
                                )
                            } else {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = "Google",
                                    tint = Color(0xFF4285F4),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Google")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Términos
                Text(
                    text = "Al continuar, aceptas nuestros Términos de Servicio\ny Política de Privacidad",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Diálogo de recuperar contraseña
    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            email = forgotPasswordEmail,
            onEmailChange = { forgotPasswordEmail = it },
            onDismiss = {
                showForgotPasswordDialog = false
                forgotPasswordEmail = ""
            },
            onSend = {
                scope.launch {
                    authViewModel.resetPassword(forgotPasswordEmail)
                    delay(1000)
                    showForgotPasswordDialog = false
                    forgotPasswordEmail = ""
                }
            },
            isLoading = authState.isLoading,
            authViewModel = authViewModel
        )
    }
}
// ========== COMPONENTES AUXILIARES ==========

@Composable
private fun CustomTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7FAFC), RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        TabButton(
            text = "Ingresar",
            isSelected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f)
        )

        TabButton(
            text = "Registrarse",
            isSelected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedBackground by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.Transparent,
        animationSpec = tween(300)
    )

    val animatedTextColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF667eea) else Color(0xFF718096),
        animationSpec = tween(300)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(animatedBackground)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = animatedTextColor
        )
    }
}

@Composable
private fun LoginContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    isLoading: Boolean,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    authViewModel: AuthViewModel
) {
    val emailError = remember(email) {
        if (email.isNotEmpty() && !authViewModel.isValidEmail(email)) {
            "Correo inválido"
        } else null
    }

    val passwordError = remember(password) {
        if (password.isNotEmpty() && !authViewModel.isValidPassword(password)) {
            "Mínimo 6 caracteres"
        } else null
    }

    val canLogin = email.isNotEmpty() &&
            password.isNotEmpty() &&
            emailError == null &&
            passwordError == null

    Column {
        // Email
        OutlinedTextField(
            value = email,
            onValueChange = {
                onEmailChange(it)
                authViewModel.clearError()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Correo electrónico") },
            placeholder = { Text("ejemplo@correo.com") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null)
            },
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667eea),
                focusedLabelColor = Color(0xFF667eea),
                focusedLeadingIconColor = Color(0xFF667eea),
                focusedTextColor = Color(0xFF1A202C),        // AGREGAR
                unfocusedTextColor = Color(0xFF1A202C),      // AGREGAR
                cursorColor = Color(0xFF667eea)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = {
                onPasswordChange(it)
                authViewModel.clearError()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Contraseña") },
            placeholder = { Text("Tu contraseña") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            },
            trailingIcon = {
                IconButton(onClick = onPasswordVisibilityChange) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ocultar" else "Mostrar"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667eea),
                focusedLabelColor = Color(0xFF667eea),
                focusedLeadingIconColor = Color(0xFF667eea),
                focusedTextColor = Color(0xFF1A202C),        // AGREGAR
                unfocusedTextColor = Color(0xFF1A202C),      // AGREGAR
                cursorColor = Color(0xFF667eea)
            )
        )

        // Olvidé mi contraseña
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onForgotPassword,
                enabled = !isLoading
            ) {
                Text(
                    "¿Olvidaste tu contraseña?",
                    fontSize = 13.sp,
                    color = Color(0xFF667eea)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón de login
        Button(
            onClick = onLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = canLogin && !isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF667eea),
                disabledContainerColor = Color(0xFF667eea).copy(alpha = 0.5f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Iniciando sesión...")
            } else {
                Text(
                    "Iniciar Sesión",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun RegisterContent(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    confirmPasswordVisible: Boolean,
    onConfirmPasswordVisibilityChange: () -> Unit,
    isLoading: Boolean,
    onRegister: () -> Unit,
    authViewModel: AuthViewModel
) {
    val nameError = remember(name) {
        if (name.isNotEmpty() && !authViewModel.isValidName(name)) {
            "Mínimo 3 caracteres"
        } else null
    }

    val emailError = remember(email) {
        if (email.isNotEmpty() && !authViewModel.isValidEmail(email)) {
            "Correo inválido"
        } else null
    }

    val passwordError = remember(password) {
        if (password.isNotEmpty() && !authViewModel.isValidPassword(password)) {
            "Mínimo 6 caracteres"
        } else null
    }

    val confirmPasswordError = remember(password, confirmPassword) {
        if (confirmPassword.isNotEmpty() && password != confirmPassword) {
            "Las contraseñas no coinciden"
        } else null
    }

    val canRegister = name.isNotEmpty() &&
            email.isNotEmpty() &&
            password.isNotEmpty() &&
            confirmPassword.isNotEmpty() &&
            nameError == null &&
            emailError == null &&
            passwordError == null &&
            confirmPasswordError == null

    Column {
        // Nombre completo
        OutlinedTextField(
            value = name,
            onValueChange = {
                onNameChange(it)
                authViewModel.clearError()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre completo") },
            placeholder = { Text("Tu nombre") },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null)
            },
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it) } },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667eea),
                focusedLabelColor = Color(0xFF667eea),
                focusedLeadingIconColor = Color(0xFF667eea),
                focusedTextColor = Color(0xFF1A202C),        // AGREGAR
                unfocusedTextColor = Color(0xFF1A202C),      // AGREGAR
                cursorColor = Color(0xFF667eea)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = {
                onEmailChange(it)
                authViewModel.clearError()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Correo electrónico") },
            placeholder = { Text("ejemplo@correo.com") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null)
            },
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667eea),
                focusedLabelColor = Color(0xFF667eea),
                focusedLeadingIconColor = Color(0xFF667eea),
                focusedTextColor = Color(0xFF1A202C),        // AGREGAR
                unfocusedTextColor = Color(0xFF1A202C),      // AGREGAR
                cursorColor = Color(0xFF667eea)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = {
                onPasswordChange(it)
                authViewModel.clearError()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Contraseña") },
            placeholder = { Text("Mínimo 6 caracteres") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            },
            trailingIcon = {
                IconButton(onClick = onPasswordVisibilityChange) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ocultar" else "Mostrar"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667eea),
                focusedLabelColor = Color(0xFF667eea),
                focusedLeadingIconColor = Color(0xFF667eea),
                focusedTextColor = Color(0xFF1A202C),        // AGREGAR
                unfocusedTextColor = Color(0xFF1A202C),      // AGREGAR
                cursorColor = Color(0xFF667eea)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirmar Password
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                onConfirmPasswordChange(it)
                authViewModel.clearError()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Confirmar contraseña") },
            placeholder = { Text("Repite tu contraseña") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            },
            trailingIcon = {
                IconButton(onClick = onConfirmPasswordVisibilityChange) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (confirmPasswordVisible) "Ocultar" else "Mostrar"
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = confirmPasswordError != null,
            supportingText = confirmPasswordError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667eea),
                focusedLabelColor = Color(0xFF667eea),
                focusedLeadingIconColor = Color(0xFF667eea),
                focusedTextColor = Color(0xFF1A202C),        // AGREGAR
                unfocusedTextColor = Color(0xFF1A202C),      // AGREGAR
                cursorColor = Color(0xFF667eea)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de registro
        Button(
            onClick = onRegister,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = canRegister && !isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF667eea),
                disabledContainerColor = Color(0xFF667eea).copy(alpha = 0.5f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Creando cuenta...")
            } else {
                Text(
                    "Crear Cuenta",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ForgotPasswordDialog(
    email: String,
    onEmailChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean,
    authViewModel: AuthViewModel
) {
    val emailError = remember(email) {
        if (email.isNotEmpty() && !authViewModel.isValidEmail(email)) {
            "Correo inválido"
        } else null
    }

    val canSend = email.isNotEmpty() && emailError == null

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        icon = {
            Icon(
                Icons.Default.Email,
                contentDescription = null,
                tint = Color(0xFF667eea),
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "Recuperar Contraseña",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.",
                    fontSize = 14.sp,
                    color = Color(0xFF718096),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Correo electrónico") },
                    placeholder = { Text("ejemplo@correo.com") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(           // AGREGAR ESTA SECCIÓN
                        focusedTextColor = Color(0xFF1A202C),
                        unfocusedTextColor = Color(0xFF1A202C),
                        cursorColor = Color(0xFF667eea)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSend,
                enabled = canSend && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667eea)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Enviar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar", color = Color(0xFF718096))
            }
        }
    )
}