package com.example.data.model

data class Amenity(
    val id: String,
    val name: String,
    val iconName: String,
    val description: String,
    val capacity: Int,
    val operationalHours: String,
    val bookingPrice: String,
    val rules: String
)

data class AmenityBooking(
    val id: String,
    val amenityId: String,
    val amenityName: String,
    val residentName: String,
    val unitLabel: String,
    val dateEpochDay: Long,
    val timeSlot: String,
    val status: String, // "CONFIRMED", "CANCELLED"
    val purpose: String,
    val createdAtMillis: Long
)
