package com.example.data.model

enum class SocietyRole(val label: String, val subtitle: String) {
    RESIDENT("Resident", "View dues & log flat/common repairs"),
    MANAGER("Committee", "Collect maintenance & dispatch contractors"),
    CONTRACTOR("Contractor", "Execute auto-scheduled repair visits");

    companion object {
        fun fromRaw(raw: String?): SocietyRole =
            entries.firstOrNull { it.name == raw } ?: RESIDENT
    }
}

enum class TradeCategory(val label: String, val shortCode: String) {
    PLUMBING("Plumbing", "PLM"),
    ELECTRICAL("Electrical", "ELC"),
    ELEVATOR("Elevator", "ELV"),
    HVAC("HVAC & Cooling", "HVC"),
    CIVIL("Masonry & Civil", "CVL"),
    SECURITY("Access & Security", "SEC");

    companion object {
        fun fromRaw(raw: String?): TradeCategory =
            entries.firstOrNull { it.name == raw } ?: PLUMBING
    }
}

enum class TicketPriority(val label: String, val rank: Int) {
    EMERGENCY("Emergency", 4),
    HIGH("High", 3),
    MEDIUM("Medium", 2),
    LOW("Low", 1);

    companion object {
        fun fromRaw(raw: String?): TicketPriority =
            entries.firstOrNull { it.name == raw } ?: MEDIUM
    }
}

enum class TicketStatus(val label: String) {
    LOGGED("Logged"),
    SCHEDULED("Scheduled"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    companion object {
        fun fromRaw(raw: String?): TicketStatus =
            entries.firstOrNull { it.name == raw } ?: LOGGED
    }
}

enum class DueStatus(val label: String) {
    PENDING("Pending"),
    OVERDUE("Overdue"),
    PAID("Paid");

    companion object {
        fun fromRaw(raw: String?): DueStatus =
            entries.firstOrNull { it.name == raw } ?: PENDING
    }
}

enum class PaymentMethod(val label: String) {
    UPI("UPI Instant"),
    BANK_TRANSFER("Bank / NEFT"),
    CARD("Card"),
    CASH("Society Office Cash");

    companion object {
        fun fromRaw(raw: String?): PaymentMethod =
            entries.firstOrNull { it.name == raw } ?: UPI
    }
}

enum class ScheduleSlot(
    val label: String,
    val windowText: String,
    val startHour: Int,
    val endHour: Int
) {
    EMERGENCY_NOW("Immediate Dispatch", "Urgent Same-Day", 0, 24),
    MORNING_09_12("Morning Slot", "09:00 – 12:00", 9, 12),
    AFTERNOON_13_16("Afternoon Slot", "13:00 – 16:00", 13, 16),
    EVENING_17_20("Evening Slot", "17:00 – 20:00", 17, 20);

    companion object {
        fun fromRaw(raw: String?): ScheduleSlot =
            entries.firstOrNull { it.name == raw } ?: MORNING_09_12

        val standardSlots: List<ScheduleSlot> = listOf(MORNING_09_12, AFTERNOON_13_16, EVENING_17_20)
    }
}

enum class ScheduleStatus(val label: String) {
    SCHEDULED("Scheduled"),
    EN_ROUTE("En Route"),
    IN_WORK("In Work"),
    DONE("Completed"),
    CANCELLED("Cancelled");

    companion object {
        fun fromRaw(raw: String?): ScheduleStatus =
            entries.firstOrNull { it.name == raw } ?: SCHEDULED
    }
}

data class UserProfile(
    val id: String = "local_profile",
    val societyName: String = "Shafi Homes Arcade",
    val residentName: String = "Society Member",
    val unitLabel: String = "Tower A - 101",
    val defaultMonthlyFee: Double = 3500.0,
    val activeRole: String = SocietyRole.RESIDENT.name,
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L
) {
    val roleEnum: SocietyRole get() = SocietyRole.fromRaw(activeRole)
}

data class MaintenanceDue(
    val id: String = "",
    val unitNumber: String = "",
    val residentName: String = "",
    val billingPeriod: String = "",
    val baseAmount: Double = 0.0,
    val sinkingFund: Double = 0.0,
    val utilityAmount: Double = 0.0,
    val lateFee: Double = 0.0,
    val totalAmount: Double = 0.0,
    val status: String = DueStatus.PENDING.name,
    val paymentMethod: String? = null,
    val receiptNumber: String? = null,
    val notes: String? = null,
    val dueDateEpochDay: Long = 0L,
    val paidAtEpochMillis: Long? = null,
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L
) {
    val statusEnum: DueStatus get() = DueStatus.fromRaw(status)
    val paymentMethodEnum: PaymentMethod? get() = paymentMethod?.let { PaymentMethod.fromRaw(it) }
}

data class Contractor(
    val id: String = "",
    val name: String = "",
    val company: String = "",
    val specialty: String = TradeCategory.PLUMBING.name,
    val phoneSuffix: String? = null,
    val shiftStartHour: Long = 8L,
    val shiftEndHour: Long = 20L,
    val dailyCapacity: Long = 4L,
    val hourlyRate: Double = 400.0,
    val available: Boolean = true,
    val completedJobs: Long = 0L,
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L
) {
    val specialtyEnum: TradeCategory get() = TradeCategory.fromRaw(specialty)
}

data class RepairTicket(
    val id: String = "",
    val unitLocation: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = TradeCategory.PLUMBING.name,
    val priority: String = TicketPriority.MEDIUM.name,
    val status: String = TicketStatus.LOGGED.name,
    val estimatedCost: Double = 0.0,
    val actualCost: Double = 0.0,
    val workNotes: String? = null,
    val photoLabel: String? = null,
    val photoUri: String? = null,
    val assignedContractorId: String? = null,
    val assignedContractorName: String? = null,
    val scheduledDateEpochDay: Long? = null,
    val scheduledSlot: String? = null,
    val autoScheduled: Boolean = false,
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L
) {
    val categoryEnum: TradeCategory get() = TradeCategory.fromRaw(category)
    val priorityEnum: TicketPriority get() = TicketPriority.fromRaw(priority)
    val statusEnum: TicketStatus get() = TicketStatus.fromRaw(status)
    val slotEnum: ScheduleSlot? get() = scheduledSlot?.let { ScheduleSlot.fromRaw(it) }
}

data class ScheduleEntry(
    val id: String = "",
    val ticketId: String = "",
    val ticketTitle: String = "",
    val unitLocation: String = "",
    val contractorId: String = "",
    val contractorName: String = "",
    val specialty: String = TradeCategory.PLUMBING.name,
    val priority: String = TicketPriority.MEDIUM.name,
    val scheduledDateEpochDay: Long = 0L,
    val timeSlot: String = ScheduleSlot.MORNING_09_12.name,
    val status: String = ScheduleStatus.SCHEDULED.name,
    val autoScheduled: Boolean = true,
    val matchReason: String = "",
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L
) {
    val specialtyEnum: TradeCategory get() = TradeCategory.fromRaw(specialty)
    val priorityEnum: TicketPriority get() = TicketPriority.fromRaw(priority)
    val slotEnum: ScheduleSlot get() = ScheduleSlot.fromRaw(timeSlot)
    val statusEnum: ScheduleStatus get() = ScheduleStatus.fromRaw(status)
}
