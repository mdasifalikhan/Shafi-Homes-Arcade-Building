package com.example.data.model

data class DomesticHelper(
    val id: String,
    val name: String,
    val serviceType: String, // "MAID", "COOK", "DRIVER", "CAR_CLEANER", "LAUNDRY"
    val phone: String,
    val assignedUnits: String,
    val isInPremises: Boolean,
    val lastCheckInTime: String?
)
