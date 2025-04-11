package com.plantecare.appmobile.models

import androidx.compose.ui.semantics.Role

data class LoginResponse(
    val message: String,
    val username: String,
    val role: Int,
    val token: String
)
