package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DirectoryEntry

@Entity(tableName = "society_directory")
data class DirectoryEntryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val unitNumber: String,
    val role: String,
    val category: String,
    val phoneNumber: String,
    val email: String?
) {
    fun toDomain(): DirectoryEntry = DirectoryEntry(
        id = id,
        name = name,
        unitNumber = unitNumber,
        role = role,
        category = category,
        phoneNumber = phoneNumber,
        email = email
    )
}
