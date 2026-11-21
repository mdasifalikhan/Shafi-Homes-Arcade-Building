package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SocietyDao {

    // --- Profile ---
    @Query("SELECT * FROM user_profile WHERE id = 'local_profile' LIMIT 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 'local_profile' LIMIT 1")
    suspend fun getProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)

    // --- Maintenance Dues ---
    @Query("SELECT * FROM maintenance_dues ORDER BY createdAtMillis DESC")
    fun observeMaintenanceDues(): Flow<List<MaintenanceDueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMaintenanceDue(due: MaintenanceDueEntity)

    @Query(
        "UPDATE maintenance_dues SET status = :status, paymentMethod = :paymentMethod, " +
            "receiptNumber = :receiptNumber, paidAtEpochMillis = :paidAtEpochMillis, " +
            "notes = COALESCE(:notes, notes), updatedAtMillis = :updatedAtMillis " +
            "WHERE id = :dueId"
    )
    suspend fun recordDuePayment(
        dueId: String,
        status: String,
        paymentMethod: String,
        receiptNumber: String,
        paidAtEpochMillis: Long,
        notes: String?,
        updatedAtMillis: Long
    )

    @Query(
        "UPDATE maintenance_dues SET status = :status, lateFee = :lateFee, " +
            "totalAmount = :totalAmount, updatedAtMillis = :updatedAtMillis WHERE id = :dueId"
    )
    suspend fun markDueOverdue(
        dueId: String,
        status: String,
        lateFee: Double,
        totalAmount: Double,
        updatedAtMillis: Long
    )

    @Query("DELETE FROM maintenance_dues WHERE id = :dueId")
    suspend fun deleteMaintenanceDue(dueId: String)

    // --- Contractors ---
    @Query("SELECT * FROM contractors ORDER BY name ASC")
    fun observeContractors(): Flow<List<ContractorEntity>>

    @Query("SELECT * FROM contractors WHERE id = :contractorId LIMIT 1")
    suspend fun getContractorById(contractorId: String): ContractorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContractor(contractor: ContractorEntity)

    @Query("UPDATE contractors SET available = :available, updatedAtMillis = :updatedAtMillis WHERE id = :contractorId")
    suspend fun updateContractorAvailability(contractorId: String, available: Boolean, updatedAtMillis: Long)

    @Query("UPDATE contractors SET completedJobs = completedJobs + 1, updatedAtMillis = :updatedAtMillis WHERE id = :contractorId")
    suspend fun incrementContractorCompletedJobs(contractorId: String, updatedAtMillis: Long)

    @Query("DELETE FROM contractors WHERE id = :contractorId")
    suspend fun deleteContractor(contractorId: String)

    // --- Repair Tickets ---
    @Query("SELECT * FROM repair_tickets ORDER BY createdAtMillis DESC")
    fun observeRepairTickets(): Flow<List<RepairTicketEntity>>

    @Query("SELECT * FROM repair_tickets WHERE id = :ticketId LIMIT 1")
    suspend fun getTicketById(ticketId: String): RepairTicketEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRepairTicket(ticket: RepairTicketEntity)

    @Query(
        "UPDATE repair_tickets SET status = :status, actualCost = :actualCost, " +
            "workNotes = COALESCE(:workNotes, workNotes), updatedAtMillis = :updatedAtMillis " +
            "WHERE id = :ticketId"
    )
    suspend fun updateRepairProgress(
        ticketId: String,
        status: String,
        actualCost: Double,
        workNotes: String?,
        updatedAtMillis: Long
    )

    @Query(
        "UPDATE repair_tickets SET status = :status, assignedContractorId = :contractorId, " +
            "assignedContractorName = :contractorName, scheduledDateEpochDay = :epochDay, " +
            "scheduledSlot = :slot, autoScheduled = :autoScheduled, updatedAtMillis = :updatedAtMillis " +
            "WHERE id = :ticketId"
    )
    suspend fun assignTicketSchedule(
        ticketId: String,
        status: String,
        contractorId: String,
        contractorName: String,
        epochDay: Long,
        slot: String,
        autoScheduled: Boolean,
        updatedAtMillis: Long
    )

    @Query("DELETE FROM repair_tickets WHERE id = :ticketId")
    suspend fun deleteRepairTicket(ticketId: String)

    // --- Schedule Entries ---
    @Query("SELECT * FROM schedule_entries ORDER BY scheduledDateEpochDay ASC, createdAtMillis ASC")
    fun observeScheduleEntries(): Flow<List<ScheduleEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleEntries(entries: List<ScheduleEntryEntity>)

    @Query("UPDATE schedule_entries SET status = :status, updatedAtMillis = :updatedAtMillis WHERE id = :entryId")
    suspend fun updateScheduleStatus(entryId: String, status: String, updatedAtMillis: Long)

    @Query(
        "UPDATE schedule_entries SET scheduledDateEpochDay = :epochDay, timeSlot = :slot, " +
            "matchReason = :matchReason, updatedAtMillis = :updatedAtMillis WHERE id = :entryId"
    )
    suspend fun updateScheduleSlot(
        entryId: String,
        epochDay: Long,
        slot: String,
        matchReason: String,
        updatedAtMillis: Long
    )

    @Transaction
    suspend fun applyScheduleBatch(
        entries: List<ScheduleEntryEntity>,
        ticketAssignments: List<TicketScheduleUpdate>,
        nowMillis: Long
    ) {
        insertScheduleEntries(entries)
        for (update in ticketAssignments) {
            assignTicketSchedule(
                ticketId = update.ticketId,
                status = "SCHEDULED",
                contractorId = update.contractorId,
                contractorName = update.contractorName,
                epochDay = update.epochDay,
                slot = update.slot,
                autoScheduled = true,
                updatedAtMillis = nowMillis
            )
        }
    }

    @Transaction
    suspend fun advanceScheduleTransaction(
        entryId: String,
        ticketId: String,
        contractorId: String,
        nextScheduleStatus: String,
        mappedTicketStatus: String,
        actualCost: Double?,
        workNotes: String?,
        incrementContractorCount: Boolean,
        nowMillis: Long
    ) {
        updateScheduleStatus(entryId, nextScheduleStatus, nowMillis)
        val ticket = getTicketById(ticketId)
        if (ticket != null && ticket.status != "COMPLETED" && ticket.status != "CANCELLED") {
            updateRepairProgress(
                ticketId = ticketId,
                status = mappedTicketStatus,
                actualCost = actualCost ?: ticket.actualCost,
                workNotes = workNotes,
                updatedAtMillis = nowMillis
            )
        }
        if (incrementContractorCount && getContractorById(contractorId) != null) {
            incrementContractorCompletedJobs(contractorId, nowMillis)
        }
    }

    @Transaction
    suspend fun rescheduleTransaction(
        entryId: String,
        ticketId: String,
        contractorId: String,
        contractorName: String,
        autoScheduled: Boolean,
        newEpochDay: Long,
        newSlot: String,
        matchReason: String,
        nowMillis: Long
    ) {
        updateScheduleSlot(entryId, newEpochDay, newSlot, matchReason, nowMillis)
        val ticket = getTicketById(ticketId)
        if (ticket != null && ticket.status != "COMPLETED" && ticket.status != "CANCELLED") {
            assignTicketSchedule(
                ticketId = ticketId,
                status = "SCHEDULED",
                contractorId = contractorId,
                contractorName = contractorName,
                epochDay = newEpochDay,
                slot = newSlot,
                autoScheduled = autoScheduled,
                updatedAtMillis = nowMillis
            )
        }
    }

    // --- Notice Board ---
    @Query("SELECT COUNT(*) FROM notices")
    suspend fun getNoticesCount(): Int

    @Query("SELECT * FROM notices ORDER BY isPinned DESC, createdAtMillis DESC")
    fun observeNotices(): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNotice(notice: NoticeEntity)

    @Query("DELETE FROM notices WHERE id = :id")
    suspend fun deleteNotice(id: String)

    // --- Visitor Passes ---
    @Query("SELECT * FROM visitor_passes ORDER BY createdAtMillis DESC")
    fun observeVisitorPasses(): Flow<List<VisitorPassEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVisitorPass(pass: VisitorPassEntity)

    @Query(
        "UPDATE visitor_passes SET status = :status, " +
            "checkInEpochMillis = COALESCE(:checkInEpochMillis, checkInEpochMillis), " +
            "checkOutEpochMillis = COALESCE(:checkOutEpochMillis, checkOutEpochMillis) " +
            "WHERE id = :passId"
    )
    suspend fun updateVisitorStatus(
        passId: String,
        status: String,
        checkInEpochMillis: Long?,
        checkOutEpochMillis: Long?
    )

    @Query("DELETE FROM visitor_passes WHERE id = :passId")
    suspend fun deleteVisitorPass(passId: String)

    // --- Amenity Bookings ---
    @Query("SELECT * FROM amenity_bookings ORDER BY createdAtMillis DESC")
    fun observeAmenityBookings(): Flow<List<AmenityBookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAmenityBooking(booking: AmenityBookingEntity)

    @Query("UPDATE amenity_bookings SET status = :status WHERE id = :bookingId")
    suspend fun updateAmenityBookingStatus(bookingId: String, status: String)

    @Query("DELETE FROM amenity_bookings WHERE id = :bookingId")
    suspend fun deleteAmenityBooking(bookingId: String)

    // --- Society Polls ---
    @Query("SELECT * FROM society_polls ORDER BY createdAtMillis DESC")
    fun observeSocietyPolls(): Flow<List<SocietyPollEntity>>

    @Query("SELECT * FROM society_polls WHERE id = :pollId LIMIT 1")
    suspend fun getPollById(pollId: String): SocietyPollEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSocietyPoll(poll: SocietyPollEntity)

    @Query("DELETE FROM society_polls WHERE id = :pollId")
    suspend fun deleteSocietyPoll(pollId: String)

    // --- Domestic Helpers ---
    @Query("SELECT * FROM domestic_helpers ORDER BY name ASC")
    fun observeDomesticHelpers(): Flow<List<DomesticHelperEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDomesticHelper(helper: DomesticHelperEntity)

    @Query("UPDATE domestic_helpers SET isInPremises = :inPremises, lastCheckInTime = :checkInTime WHERE id = :helperId")
    suspend fun updateHelperStatus(helperId: String, inPremises: Boolean, checkInTime: String?)

    @Query("DELETE FROM domestic_helpers WHERE id = :helperId")
    suspend fun deleteDomesticHelper(helperId: String)

    // --- Society Directory ---
    @Query("SELECT * FROM society_directory ORDER BY category ASC, unitNumber ASC, name ASC")
    fun observeDirectory(): Flow<List<DirectoryEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDirectoryEntry(entry: DirectoryEntryEntity)

    @Query("SELECT COUNT(*) FROM society_directory")
    suspend fun getDirectoryCount(): Int

    @Query("SELECT COUNT(*) FROM society_polls")
    suspend fun getPollsCount(): Int

    @Query("SELECT COUNT(*) FROM domestic_helpers")
    suspend fun getHelpersCount(): Int
}

data class TicketScheduleUpdate(
    val ticketId: String,
    val contractorId: String,
    val contractorName: String,
    val epochDay: Long,
    val slot: String
)
