package com.NeighTech.favoresapp.ui.Model

data class Applicant(
    val userId: String = "",
    val userName: String = "",
    val userLocation: String = "",
    val appliedAt: Long = System.currentTimeMillis()
)
