package com.example.data.local

import android.content.Context
import com.example.data.model.Contractor
import com.example.data.model.DueStatus
import com.example.data.model.MaintenanceDue
import com.example.data.model.PaymentMethod
import com.example.data.model.RepairTicket
import com.example.data.model.ScheduleEntry
import com.example.data.model.ScheduleSlot
import com.example.data.model.ScheduleStatus
import com.example.data.model.SocietyRole
import com.example.data.model.TicketPriority
import com.example.data.model.TicketStatus
import com.example.data.model.TradeCategory
import com.example.data.model.UserProfile
import com.example.data.scheduling.ScheduleProposal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class SocietyRepository(private val dao: SocietyDao) {

    constructor(context: Context) : this(SocietyDatabase.getInstance(context).societyDao())

    fun observeUserProfile(): Flow<UserProfile?> =
        dao.observeProfile().map { it?.toDomain() }

    fun observeMaintenanceDues(): Flow<List<MaintenanceDue>> =
        dao.observeMaintenanceDues().map { list -> list.map { it.toDomain() } }

    fun observeContractors(): Flow<List<Contractor>> =
        dao.observeContractors().map { list -> list.map { it.toDomain() } }

    fun observeRepairTickets(): Flow<List<RepairTicket>> =
        dao.observeRepairTickets().map { list -> list.map { it.toDomain() } }

    fun observeScheduleEntries(): Flow<List<ScheduleEntry>> =
        dao.observeScheduleEntries().map { list -> list.map { it.toDomain() } }

    suspend fun saveUserProfile(
        societyName: String,
        residentName: String,
        unitLabel: String,
        defaultMonthlyFee: Double,
        activeRole: SocietyRole
    ): Result<Unit> = runSafe {
        val cleanSociety = societyName.trim().take(80)
        val cleanName = residentName.trim().take(60)
        val cleanUnit = unitLabel.trim().take(24)
        require(cleanSociety.length >= 2) { "Society name must be at least 2 characters." }
        require(cleanName.length >= 2) { "Name must be at least 2 characters." }
        require(cleanUnit.isNotEmpty()) { "Unit label is required." }
        require(defaultMonthlyFee in 0.0..1_000_000.0) { "Monthly fee out of range." }

        val now = System.currentTimeMillis()
        val existing = dao.getProfileOnce()
        dao.upsertProfile(
            UserProfileEntity(
                id = "local_profile",
                societyName = cleanSociety,
                residentName = cleanName,
                unitLabel = cleanUnit,
                defaultMonthlyFee = defaultMonthlyFee,
                activeRole = activeRole.name,
                createdAtMillis = existing?.createdAtMillis ?: now,
                updatedAtMillis = now
            )
        )
    }

    suspend fun updateActiveRole(role: SocietyRole): Result<Unit> = runSafe {
        val now = System.currentTimeMillis()
        val existing = dao.getProfileOnce()
        dao.upsertProfile(
            UserProfileEntity(
                id = "local_profile",
                societyName = existing?.societyName ?: "Shafi Homes Arcade",
                residentName = existing?.residentName ?: "Society Member",
                unitLabel = existing?.unitLabel ?: "Tower A - 101",
                defaultMonthlyFee = existing?.defaultMonthlyFee ?: 3500.0,
                activeRole = role.name,
                createdAtMillis = existing?.createdAtMillis ?: now,
                updatedAtMillis = now
            )
        )
    }

    suspend fun createMaintenanceDue(
        unitNumber: String,
        residentName: String,
        billingPeriod: String,
        baseAmount: Double,
        sinkingFund: Double,
        utilityAmount: Double,
        dueDateEpochDay: Long,
        notes: String? = null
    ): Result<Unit> = runSafe {
        val cleanUnit = unitNumber.trim().take(24)
        val cleanResident = residentName.trim().take(60)
        val cleanPeriod = billingPeriod.trim().take(32)
        require(cleanUnit.isNotEmpty()) { "Unit number is required." }
        require(cleanResident.length >= 2) { "Resident name must be at least 2 characters." }
        require(cleanPeriod.length >= 3) { "Billing period must be at least 3 characters." }
        require(baseAmount in 0.0..1_000_000.0) { "Base amount out of range." }
        require(sinkingFund in 0.0..1_000_000.0) { "Sinking fund out of range." }
        require(utilityAmount in 0.0..1_000_000.0) { "Utility amount out of range." }
        require(dueDateEpochDay >= 0L) { "Invalid due date." }

        val now = System.currentTimeMillis()
        val total = baseAmount + sinkingFund + utilityAmount
        val docId = "due_${UUID.randomUUID().toString().replace("-", "").take(16)}"

        dao.upsertMaintenanceDue(
            MaintenanceDueEntity(
                id = docId,
                unitNumber = cleanUnit,
                residentName = cleanResident,
                billingPeriod = cleanPeriod,
                baseAmount = baseAmount,
                sinkingFund = sinkingFund,
                utilityAmount = utilityAmount,
                lateFee = 0.0,
                totalAmount = total,
                status = DueStatus.PENDING.name,
                paymentMethod = null,
                receiptNumber = null,
                notes = notes?.trim()?.take(300)?.takeIf { it.isNotEmpty() },
                dueDateEpochDay = dueDateEpochDay,
                paidAtEpochMillis = null,
                createdAtMillis = now,
                updatedAtMillis = now
            )
        )
    }

    suspend fun recordDuePayment(
        due: MaintenanceDue,
        paymentMethod: PaymentMethod,
        receiptNumber: String,
        paymentNotes: String?
    ): Result<Unit> = runSafe {
        require(due.id.isNotBlank()) { "Due ID is missing." }
        require(due.statusEnum != DueStatus.PAID) { "This bill is already marked paid." }
        val cleanReceipt = receiptNumber.trim().take(40)
        require(cleanReceipt.isNotEmpty()) { "Receipt reference is required." }
        val now = System.currentTimeMillis()

        dao.recordDuePayment(
            dueId = due.id,
            status = DueStatus.PAID.name,
            paymentMethod = paymentMethod.name,
            receiptNumber = cleanReceipt,
            paidAtEpochMillis = now,
            notes = paymentNotes?.trim()?.take(300)?.takeIf { it.isNotEmpty() },
            updatedAtMillis = now
        )
    }

    suspend fun markDueOverdue(
        due: MaintenanceDue,
        additionalLateFee: Double
    ): Result<Unit> = runSafe {
        require(due.id.isNotBlank()) { "Due ID is missing." }
        require(due.statusEnum != DueStatus.PAID) { "Paid bills cannot be marked overdue." }
        require(additionalLateFee in 0.0..1_000_000.0) { "Late fee out of range." }

        val newLateFee = due.lateFee + additionalLateFee
        val newTotal = due.baseAmount + due.sinkingFund + due.utilityAmount + newLateFee
        dao.markDueOverdue(
            dueId = due.id,
            status = DueStatus.OVERDUE.name,
            lateFee = newLateFee,
            totalAmount = newTotal,
            updatedAtMillis = System.currentTimeMillis()
        )
    }

    suspend fun deleteMaintenanceDue(dueId: String): Result<Unit> = runSafe {
        require(dueId.isNotBlank())
        dao.deleteMaintenanceDue(dueId)
    }

    suspend fun createContractor(
        name: String,
        company: String,
        specialty: TradeCategory,
        phoneSuffix: String?,
        shiftStartHour: Long,
        shiftEndHour: Long,
        dailyCapacity: Long,
        hourlyRate: Double
    ): Result<Unit> = runSafe {
        val cleanName = name.trim().take(60)
        val cleanCompany = company.trim().take(60)
        require(cleanName.length >= 2) { "Contractor name must be at least 2 characters." }
        require(cleanCompany.length >= 2) { "Company name must be at least 2 characters." }
        require(shiftStartHour in 0L..23L && shiftEndHour in (shiftStartHour + 1)..24L) {
            "Invalid shift window."
        }
        require(dailyCapacity in 1L..12L) { "Daily capacity must be between 1 and 12 jobs." }
        require(hourlyRate in 0.0..50_000.0) { "Hourly rate out of range." }

        val now = System.currentTimeMillis()
        val docId = "con_${UUID.randomUUID().toString().replace("-", "").take(16)}"

        dao.upsertContractor(
            ContractorEntity(
                id = docId,
                name = cleanName,
                company = cleanCompany,
                specialty = specialty.name,
                phoneSuffix = phoneSuffix?.trim()?.take(16)?.takeIf { it.isNotEmpty() },
                shiftStartHour = shiftStartHour,
                shiftEndHour = shiftEndHour,
                dailyCapacity = dailyCapacity,
                hourlyRate = hourlyRate,
                available = true,
                completedJobs = 0L,
                createdAtMillis = now,
                updatedAtMillis = now
            )
        )
    }

    suspend fun toggleContractorAvailability(
        contractorId: String,
        available: Boolean
    ): Result<Unit> = runSafe {
        require(contractorId.isNotBlank())
        dao.updateContractorAvailability(contractorId, available, System.currentTimeMillis())
    }

    suspend fun deleteContractor(contractorId: String): Result<Unit> = runSafe {
        require(contractorId.isNotBlank())
        dao.deleteContractor(contractorId)
    }

    suspend fun createRepairTicket(
        unitLocation: String,
        title: String,
        description: String,
        category: TradeCategory,
        priority: TicketPriority,
        estimatedCost: Double,
        photoLabel: String? = null,
        photoUri: String? = null
    ): Result<RepairTicket> = runSafeValue {
        val cleanUnit = unitLocation.trim().take(40)
        val cleanTitle = title.trim().take(80)
        val cleanDesc = description.trim().take(500)
        require(cleanUnit.isNotEmpty()) { "Unit or area location is required." }
        require(cleanTitle.length >= 3) { "Repair title must be at least 3 characters." }
        require(cleanDesc.length >= 5) { "Description must be at least 5 characters." }
        require(estimatedCost in 0.0..1_000_000.0) { "Estimated cost out of range." }

        val now = System.currentTimeMillis()
        val docId = "tkt_${UUID.randomUUID().toString().replace("-", "").take(16)}"

        val entity = RepairTicketEntity(
            id = docId,
            unitLocation = cleanUnit,
            title = cleanTitle,
            description = cleanDesc,
            category = category.name,
            priority = priority.name,
            status = TicketStatus.LOGGED.name,
            estimatedCost = estimatedCost,
            actualCost = 0.0,
            workNotes = null,
            photoLabel = photoLabel?.trim()?.take(120)?.takeIf { it.isNotEmpty() },
            photoUri = photoUri?.trim()?.take(400)?.takeIf { it.isNotEmpty() },
            assignedContractorId = null,
            assignedContractorName = null,
            scheduledDateEpochDay = null,
            scheduledSlot = null,
            autoScheduled = false,
            createdAtMillis = now,
            updatedAtMillis = now
        )
        dao.upsertRepairTicket(entity)
        entity.toDomain()
    }

    suspend fun updateRepairProgress(
        ticket: RepairTicket,
        newStatus: TicketStatus,
        actualCost: Double,
        workNotes: String?
    ): Result<Unit> = runSafe {
        require(ticket.id.isNotBlank())
        require(ticket.statusEnum != TicketStatus.COMPLETED && ticket.statusEnum != TicketStatus.CANCELLED) {
            "Completed or cancelled tickets are locked."
        }
        require(actualCost in 0.0..1_000_000.0) { "Actual cost out of range." }

        dao.updateRepairProgress(
            ticketId = ticket.id,
            status = newStatus.name,
            actualCost = actualCost,
            workNotes = workNotes?.trim()?.take(500)?.takeIf { it.isNotEmpty() },
            updatedAtMillis = System.currentTimeMillis()
        )
    }

    suspend fun deleteRepairTicket(ticketId: String): Result<Unit> = runSafe {
        require(ticketId.isNotBlank())
        dao.deleteRepairTicket(ticketId)
    }

    suspend fun applyScheduleProposals(proposals: List<ScheduleProposal>): Result<Int> = runSafeValue {
        if (proposals.isEmpty()) return@runSafeValue 0
        val now = System.currentTimeMillis()
        val entries = proposals.map { proposal ->
            val entryId = "sch_${UUID.randomUUID().toString().replace("-", "").take(16)}"
            ScheduleEntryEntity(
                id = entryId,
                ticketId = proposal.ticket.id,
                ticketTitle = proposal.ticket.title.trim(),
                unitLocation = proposal.ticket.unitLocation.trim(),
                contractorId = proposal.contractor.id,
                contractorName = proposal.contractor.name.trim(),
                specialty = proposal.contractor.specialty,
                priority = proposal.ticket.priority,
                scheduledDateEpochDay = proposal.epochDay,
                timeSlot = proposal.slot.name,
                status = ScheduleStatus.SCHEDULED.name,
                autoScheduled = true,
                matchReason = proposal.matchReason.trim(),
                createdAtMillis = now,
                updatedAtMillis = now
            )
        }
        val ticketUpdates = proposals.map { proposal ->
            TicketScheduleUpdate(
                ticketId = proposal.ticket.id,
                contractorId = proposal.contractor.id,
                contractorName = proposal.contractor.name.take(60),
                epochDay = proposal.epochDay,
                slot = proposal.slot.name
            )
        }
        dao.applyScheduleBatch(entries, ticketUpdates, now)
        proposals.size
    }

    suspend fun advanceScheduleEntry(
        entry: ScheduleEntry,
        nextStatus: ScheduleStatus,
        actualCost: Double? = null,
        workNotes: String? = null
    ): Result<Unit> = runSafe {
        require(entry.id.isNotBlank())
        require(entry.statusEnum != ScheduleStatus.DONE && entry.statusEnum != ScheduleStatus.CANCELLED) {
            "Completed or cancelled dispatch entries are locked."
        }
        val mappedTicketStatus = when (nextStatus) {
            ScheduleStatus.SCHEDULED -> TicketStatus.SCHEDULED
            ScheduleStatus.EN_ROUTE, ScheduleStatus.IN_WORK -> TicketStatus.IN_PROGRESS
            ScheduleStatus.DONE -> TicketStatus.COMPLETED
            ScheduleStatus.CANCELLED -> TicketStatus.CANCELLED
        }
        dao.advanceScheduleTransaction(
            entryId = entry.id,
            ticketId = entry.ticketId,
            contractorId = entry.contractorId,
            nextScheduleStatus = nextStatus.name,
            mappedTicketStatus = mappedTicketStatus.name,
            actualCost = actualCost,
            workNotes = workNotes?.trim()?.take(500)?.takeIf { it.isNotEmpty() },
            incrementContractorCount = nextStatus == ScheduleStatus.DONE,
            nowMillis = System.currentTimeMillis()
        )
    }

    suspend fun rescheduleEntry(
        entry: ScheduleEntry,
        newEpochDay: Long,
        newSlot: ScheduleSlot
    ): Result<Unit> = runSafe {
        require(entry.id.isNotBlank())
        require(newEpochDay >= 0L)
        require(entry.statusEnum != ScheduleStatus.DONE && entry.statusEnum != ScheduleStatus.CANCELLED)

        dao.rescheduleTransaction(
            entryId = entry.id,
            ticketId = entry.ticketId,
            contractorId = entry.contractorId,
            contractorName = entry.contractorName.take(60),
            autoScheduled = entry.autoScheduled,
            newEpochDay = newEpochDay,
            newSlot = newSlot.name,
            matchReason = "Rescheduled to ${newSlot.windowText}".take(155),
            nowMillis = System.currentTimeMillis()
        )
    }

    private suspend inline fun runSafe(crossinline block: suspend () -> Unit): Result<Unit> =
        try {
            block()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    private suspend inline fun <T> runSafeValue(crossinline block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}
