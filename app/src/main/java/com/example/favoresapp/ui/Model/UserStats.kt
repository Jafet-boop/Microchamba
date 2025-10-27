package com.example.favoresapp.ui.Model

data class UserStats(
    val userId: String = "",
    val favorsCompleted: Int = 0,
    val averageRating: Float = 0.0f,
    val totalRatings: Int = 0,
    val peopleHelped: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
