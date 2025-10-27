package com.example.favoresapp.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.favoresapp.ui.Model.Rating
import com.example.favoresapp.ui.Model.UserStats
import com.example.favoresapp.ui.Model.FavorStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserStatsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _userStats = MutableStateFlow(UserStats(userId = ""))
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUserStats()
    }

    fun loadUserStats(userId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val targetUserId = userId ?: auth.currentUser?.uid ?: return@launch

                val statsDoc = firestore.collection("userStats")
                    .document(targetUserId)
                    .get()
                    .await()

                if (statsDoc.exists()) {
                    _userStats.value = statsDoc.toObject(UserStats::class.java)
                        ?: UserStats(userId = targetUserId)
                } else {
                    // Crear stats iniciales si no existen
                    val newStats = UserStats(userId = targetUserId)
                    firestore.collection("userStats")
                        .document(targetUserId)
                        .set(newStats)
                        .await()
                    _userStats.value = newStats
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun addRating(rating: Rating) {
        try {
            // Guardar la calificación
            firestore.collection("ratings")
                .document(rating.id)
                .set(rating)
                .await()

            // Actualizar stats del usuario calificado
            updateUserStatsAfterRating(rating.toUserId)

            // Marcar el favor como calificado
            firestore.collection("favors")
                .document(rating.favorId)
                .update(
                    mapOf(
                        "isRated" to true,
                        "status" to FavorStatus.RATED.name
                    )
                )
                .await()

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private suspend fun updateUserStatsAfterRating(userId: String) {
        try {
            // Obtener todas las calificaciones del usuario
            val ratingsSnapshot = firestore.collection("ratings")
                .whereEqualTo("toUserId", userId)
                .get()
                .await()

            val ratings = ratingsSnapshot.documents.mapNotNull {
                it.toObject(Rating::class.java)
            }

            // Calcular estadísticas
            val totalRatings = ratings.size
            val averageRating = if (totalRatings > 0) {
                ratings.map { it.rating }.average().toFloat()
            } else 0f

            // Contar favores completados (únicos)
            val uniqueFavors = ratings.map { it.favorId }.distinct().size

            // Contar personas ayudadas (únicos fromUserId)
            val uniqueHelped = ratings.map { it.fromUserId }.distinct().size

            // Actualizar stats
            val updatedStats = UserStats(
                userId = userId,
                favorsCompleted = uniqueFavors,
                averageRating = averageRating,
                totalRatings = totalRatings,
                peopleHelped = uniqueHelped,
                lastUpdated = System.currentTimeMillis()
            )

            firestore.collection("userStats")
                .document(userId)
                .set(updatedStats)
                .await()

            // Si es el usuario actual, actualizar el estado
            if (userId == auth.currentUser?.uid) {
                _userStats.value = updatedStats
            }

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun getUserRatings(userId: String): List<Rating> {
        return try {
            val snapshot = firestore.collection("ratings")
                .whereEqualTo("toUserId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(Rating::class.java) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}