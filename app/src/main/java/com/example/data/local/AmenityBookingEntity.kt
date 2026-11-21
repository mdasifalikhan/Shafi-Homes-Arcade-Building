package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AmenityBooking

@Entity(tableName = "amenity_bookings")
data class AmenityBookingEntity(
    @PrimaryKey val id: String,
    val amenityId: String,
    val amenityName: String,
    val residentName: String,
    val unitLabel: String,
    val dateEpochDay: Long,
    val timeSlot: String,
    val status: String,
    val purpose: String,
    val createdAtMillis: Long
) {
    fun toDomain(): AmenityBooking = AmenityBooking(
        id = id,
        amenityId = amenityId,
        amenityName = amenityName,
        residentName = residentName,
        unitLabel = unitLabel,
        dateEpochDay = dateEpochDay,
        timeSlot = timeSlot,
        status = status,
        purpose = purpose,
        createdAtMillis = createdAtMillis
    )
}
