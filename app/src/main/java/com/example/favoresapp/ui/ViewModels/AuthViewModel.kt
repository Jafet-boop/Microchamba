package com.example.favoresapp.ui.ViewModels

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Validaciones
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidName(name: String): Boolean {
        return name.length >= 3
    }

    // Registro con Email/Password
    suspend fun registerWithEmail(
        email: String,
        password: String,
        fullName: String
    ): Result<String> {
        return try {
            _authState.value = AuthState(isLoading = true)

            // Crear usuario en Firebase Auth
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("Error al crear usuario")

            // Crear documento del usuario en Firestore
            val userMap = hashMapOf(
                "fullName" to fullName,
                "email" to email,
                "phone" to "",
                "location" to "",
                "gender" to "",
                "presentation" to "",
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(userId)
                .set(userMap)
                .await()

            // Crear estadísticas iniciales
            val statsMap = hashMapOf(
                "userId" to userId,
                "favorsCompleted" to 0,
                "averageRating" to 0.0f,
                "totalRatings" to 0,
                "peopleHelped" to 0,
                "lastUpdated" to System.currentTimeMillis()
            )

            firestore.collection("userStats")
                .document(userId)
                .set(statsMap)
                .await()

            _authState.value = AuthState(isSuccess = true)
            Result.success(userId)

        } catch (e: FirebaseAuthWeakPasswordException) {
            _authState.value = AuthState(error = "La contraseña debe tener al menos 6 caracteres")
            Result.failure(e)
        } catch (e: FirebaseAuthUserCollisionException) {
            _authState.value = AuthState(error = "Este correo ya está registrado")
            Result.failure(e)
        } catch (e: Exception) {
            _authState.value = AuthState(error = "Error al registrar: ${e.message}")
            Result.failure(e)
        }
    }

    // Login con Email/Password
    suspend fun loginWithEmail(
        email: String,
        password: String
    ): Result<String> {
        return try {
            _authState.value = AuthState(isLoading = true)

            val result = auth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("Error al iniciar sesión")

            _authState.value = AuthState(isSuccess = true)
            Result.success(userId)

        } catch (e: FirebaseAuthInvalidCredentialsException) {
            _authState.value = AuthState(error = "Correo o contraseña incorrectos")
            Result.failure(e)
        } catch (e: Exception) {
            _authState.value = AuthState(error = "Error al iniciar sesión: ${e.message}")
            Result.failure(e)
        }
    }

    // Recuperar contraseña
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            _authState.value = AuthState(isLoading = true)

            auth.sendPasswordResetEmail(email).await()

            _authState.value = AuthState(isSuccess = true)
            Result.success(Unit)

        } catch (e: Exception) {
            _authState.value = AuthState(error = "Error al enviar correo: ${e.message}")
            Result.failure(e)
        }
    }

    fun clearError() {
        _authState.value = AuthState()
    }
}
