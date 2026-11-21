package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DomesticHelper

@Entity(tableName = "domestic_helpers")
data class DomesticHelperEntity(
    @PrimaryKey val id: String,
    val name: String,
    val serviceType: String,
    val phone: String,
    val assignedUnits: String,
    val isInPremises: Boolean,
    val lastCheckInTime: String?
) {
    fun toDomain(): DomesticHelper = DomesticHelper(
        id = id,
        name = name,
        serviceType = serviceType,
        phone = phone,
        assignedUnits = assignedUnits,
        isInPremises = isInPremises,
        lastCheckInTime = lastCheckInTime
    )
}
