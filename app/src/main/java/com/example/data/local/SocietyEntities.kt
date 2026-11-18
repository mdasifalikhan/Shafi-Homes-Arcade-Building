package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Contractor
import com.example.data.model.MaintenanceDue
import com.example.data.model.RepairTicket
import com.example.data.model.ScheduleEntry
import com.example.data.model.UserProfile

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "local_profile",
    val societyName: String,
    val residentName: String,
    val unitLabel: String,
    val defaultMonthlyFee: Double,
    val activeRole: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
) {
    fun toDomain(): UserProfile = UserProfile(
        id = id,
        societyName = societyName,
        residentName = residentName,
        unitLabel = unitLabel,
        defaultMonthlyFee = defaultMonthlyFee,
        activeRole = activeRole,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis
    )
}

@Entity(tableName = "maintenance_dues")
data class MaintenanceDueEntity(
    @PrimaryKey val id: String,
    val unitNumber: String,
    val residentName: String,
    val billingPeriod: String,
    val baseAmount: Double,
    val sinkingFund: Double,
    val utilityAmount: Double,
    val lateFee: Double,
    val totalAmount: Double,
    val status: String,
    val paymentMethod: String?,
    val receiptNumber: String?,
    val notes: String?,
    val dueDateEpochDay: Long,
    val paidAtEpochMillis: Long?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
) {
    fun toDomain(): MaintenanceDue = MaintenanceDue(
        id = id,
        unitNumber = unitNumber,
        residentName = residentName,
        billingPeriod = billingPeriod,
        baseAmount = baseAmount,
        sinkingFund = sinkingFund,
        utilityAmount = utilityAmount,
        lateFee = lateFee,
        totalAmount = totalAmount,
        status = status,
        paymentMethod = paymentMethod,
        receiptNumber = receiptNumber,
        notes = notes,
        dueDateEpochDay = dueDateEpochDay,
        paidAtEpochMillis = paidAtEpochMillis,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis
    )
}

@Entity(tableName = "contractors")
data class ContractorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val company: String,
    val specialty: String,
    val phoneSuffix: String?,
    val shiftStartHour: Long,
    val shiftEndHour: Long,
    val dailyCapacity: Long,
    val hourlyRate: Double,
    val available: Boolean,
    val completedJobs: Long,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
) {
    fun toDomain(): Contractor = Contractor(
        id = id,
        name = name,
        company = company,
        specialty = specialty,
        phoneSuffix = phoneSuffix,
        shiftStartHour = shiftStartHour,
        shiftEndHour = shiftEndHour,
        dailyCapacity = dailyCapacity,
        hourlyRate = hourlyRate,
        available = available,
        completedJobs = completedJobs,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis
    )
}

@Entity(tableName = "repair_tickets")
data class RepairTicketEntity(
    @PrimaryKey val id: String,
    val unitLocation: String,
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val status: String,
    val estimatedCost: Double,
    val actualCost: Double,
    val workNotes: String?,
    val photoLabel: String?,
    val photoUri: String?,
    val assignedContractorId: String?,
    val assignedContractorName: String?,
    val scheduledDateEpochDay: Long?,
    val scheduledSlot: String?,
    val autoScheduled: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
) {
    fun toDomain(): RepairTicket = RepairTicket(
        id = id,
        unitLocation = unitLocation,
        title = title,
        description = description,
        category = category,
        priority = priority,
        status = status,
        estimatedCost = estimatedCost,
        actualCost = actualCost,
        workNotes = workNotes,
        photoLabel = photoLabel,
        photoUri = photoUri,
        assignedContractorId = assignedContractorId,
        assignedContractorName = assignedContractorName,
        scheduledDateEpochDay = scheduledDateEpochDay,
        scheduledSlot = scheduledSlot,
        autoScheduled = autoScheduled,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis
    )
}

@Entity(tableName = "schedule_entries")
data class ScheduleEntryEntity(
    @PrimaryKey val id: String,
    val ticketId: String,
    val ticketTitle: String,
    val unitLocation: String,
    val contractorId: String,
    val contractorName: String,
    val specialty: String,
    val priority: String,
    val scheduledDateEpochDay: Long,
    val timeSlot: String,
    val status: String,
    val autoScheduled: Boolean,
    val matchReason: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
) {
    fun toDomain(): ScheduleEntry = ScheduleEntry(
        id = id,
        ticketId = ticketId,
        ticketTitle = ticketTitle,
        unitLocation = unitLocation,
        contractorId = contractorId,
        contractorName = contractorName,
        specialty = specialty,
        priority = priority,
        scheduledDateEpochDay = scheduledDateEpochDay,
        timeSlot = timeSlot,
        status = status,
        autoScheduled = autoScheduled,
        matchReason = matchReason,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis
    )
}
