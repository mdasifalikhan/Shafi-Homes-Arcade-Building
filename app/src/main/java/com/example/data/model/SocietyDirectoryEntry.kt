package com.example.data.model

data class DirectoryEntry(
    val id: String,
    val name: String,
    val unitNumber: String,
    val role: String,
    val category: String, // "COMMITTEE", "RESIDENT", "STAFF"
    val phoneNumber: String,
    val email: String? = null
)
