package com.example.data.model

data class EmergencyContact(
    val id: String,
    val title: String,
    val subtitle: String,
    val phoneNumber: String,
    val category: String, // "SECURITY", "MEDICAL", "POLICE", "FIRE", "SOCIETY", "UTILITY"
    val is24x7: Boolean = true
)
