package com.plantecare.appmobile.models

data class PotResponse(
    val id: Int,
    val userId: Int,
    val macAddress: String,
    val name: String,
    val createdAt: String,
    val updatedAt: String
)
