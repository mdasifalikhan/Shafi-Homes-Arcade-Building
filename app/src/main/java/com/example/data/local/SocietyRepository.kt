package com.example.data.local

import android.content.Context
import com.example.data.model.Amenity
import com.example.data.model.AmenityBooking
import com.example.data.model.Contractor
import com.example.data.model.DirectoryEntry
import com.example.data.model.DomesticHelper
import com.example.data.model.DueStatus
import com.example.data.model.EmergencyContact
import com.example.data.model.MaintenanceDue
import com.example.data.model.Notice
import com.example.data.model.NoticeCategory
import com.example.data.model.PaymentMethod
import com.example.data.model.PollOption
import com.example.data.model.RepairTicket
import com.example.data.model.ScheduleEntry
import com.example.data.model.ScheduleSlot
import com.example.data.model.ScheduleStatus
import com.example.data.model.SocietyPoll
import com.example.data.model.SocietyRole
import com.example.data.model.TicketPriority
import com.example.data.model.TicketStatus
import com.example.data.model.TradeCategory
import com.example.data.model.UserProfile
import com.example.data.model.VisitorPass
import com.example.data.model.VisitorPurpose
import com.example.data.model.VisitorStatus
import com.example.data.scheduling.ScheduleProposal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

    fun observeNotices(): Flow<List<Notice>> =
        dao.observeNotices().map { list -> list.map { it.toDomain() } }

    fun observeVisitorPasses(): Flow<List<VisitorPass>> =
        dao.observeVisitorPasses().map { list -> list.map { it.toDomain() } }

    fun observeAmenityBookings(): Flow<List<AmenityBooking>> =
        dao.observeAmenityBookings().map { list -> list.map { it.toDomain() } }

    fun observeSocietyPolls(): Flow<List<SocietyPoll>> =
        dao.observeSocietyPolls().map { list -> list.map { it.toDomain() } }

    fun observeDomesticHelpers(): Flow<List<DomesticHelper>> =
        dao.observeDomesticHelpers().map { list -> list.map { it.toDomain() } }

    fun observeDirectory(): Flow<List<DirectoryEntry>> =
        dao.observeDirectory().map { list -> list.map { it.toDomain() } }

    fun getAmenitiesList(): List<Amenity> = listOf(
        Amenity(
            id = "amenity_party_hall",
            name = "Community & Party Hall",
            iconName = "Festival",
            description = "Spacious air-conditioned banquet hall with sound system and kitchen prep area. Ideal for family celebrations, birthdays, and society gatherings.",
            capacity = 100,
            operationalHours = "09:00 AM - 11:00 PM",
            bookingPrice = "Rs. 1,500 / event",
            rules = "Music must be turned down after 10:00 PM. No fireworks inside."
        ),
        Amenity(
            id = "amenity_rooftop",
            name = "Rooftop Garden & BBQ Terrace",
            iconName = "Deck",
            description = "Scenic rooftop terrace with city skyline views, gazebos, barbecue grills, and open seating.",
            capacity = 40,
            operationalHours = "05:00 PM - 10:30 PM",
            bookingPrice = "Free for Residents",
            rules = "Clean the BBQ pit after use. Keep terrace door securely locked."
        ),
        Amenity(
            id = "amenity_gym",
            name = "Fitness Gym & Yoga Studio",
            iconName = "FitnessCenter",
            description = "Modern fitness center equipped with treadmills, cross-trainers, multi-gym stations, and yoga mats.",
            capacity = 15,
            operationalHours = "06:00 AM - 10:00 PM",
            bookingPrice = "Free for Residents",
            rules = "Wipe machines after use. Carry gym towel and wear sports shoes."
        ),
        Amenity(
            id = "amenity_guest_parking",
            name = "Reserved Guest Parking Bay",
            iconName = "LocalParking",
            description = "Dedicated basement parking bay for visiting guests, family members, and overnight parking.",
            capacity = 6,
            operationalHours = "24 Hours",
            bookingPrice = "Free (Max 48 hrs)",
            rules = "Display Gate Pass on dashboard. Park only in demarcated guest bays."
        )
    )

    fun getEmergencyContactsList(): List<EmergencyContact> = listOf(
        EmergencyContact(
            id = "em_security",
            title = "Main Gate Security Desk",
            subtitle = "Guard Intercom & Gate Barrier Control",
            phoneNumber = "+91 98200 11223",
            category = "SECURITY",
            is24x7 = true
        ),
        EmergencyContact(
            id = "em_ambulance",
            title = "Ambulance / Medical Emergency",
            subtitle = "National Emergency Ambulance Hotline",
            phoneNumber = "108",
            category = "MEDICAL",
            is24x7 = true
        ),
        EmergencyContact(
            id = "em_police",
            title = "Police Emergency Control",
            subtitle = "Local Police Station Helpline",
            phoneNumber = "112",
            category = "POLICE",
            is24x7 = true
        ),
        EmergencyContact(
            id = "em_fire",
            title = "Fire Brigade Emergency",
            subtitle = "City Fire & Rescue Services",
            phoneNumber = "101",
            category = "FIRE",
            is24x7 = true
        ),
        EmergencyContact(
            id = "em_lift",
            title = "Lift Breakdown Emergency (Otis)",
            subtitle = "24x7 Elevator Passenger Rescue Service",
            phoneNumber = "+91 98200 55443",
            category = "UTILITY",
            is24x7 = true
        ),
        EmergencyContact(
            id = "em_secretary",
            title = "Society Secretary (Mr. Tariq)",
            subtitle = "Building Managing Committee",
            phoneNumber = "+91 98765 43210",
            category = "SOCIETY",
            is24x7 = false
        )
    )

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

    suspend fun createNotice(
        title: String,
        content: String,
        category: NoticeCategory,
        author: String = "Managing Committee",
        isPinned: Boolean = false
    ): Result<Unit> = runSafe {
        val cleanTitle = title.trim().take(120)
        val cleanContent = content.trim().take(2000)
        val cleanAuthor = author.trim().take(60).ifEmpty { "Managing Committee" }
        require(cleanTitle.length >= 3) { "Title must be at least 3 characters." }
        require(cleanContent.length >= 5) { "Notice content must be at least 5 characters." }
        dao.upsertNotice(
            NoticeEntity(
                id = UUID.randomUUID().toString(),
                title = cleanTitle,
                content = cleanContent,
                category = category.name,
                author = cleanAuthor,
                isPinned = isPinned,
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteNotice(id: String): Result<Unit> = runSafe {
        dao.deleteNotice(id)
    }

    suspend fun createVisitorPass(
        visitorName: String,
        phone: String?,
        purpose: VisitorPurpose,
        unitNumber: String,
        vehicleNumber: String? = null
    ): Result<VisitorPass> = runSafeValue {
        val cleanName = visitorName.trim().take(60)
        val cleanUnit = unitNumber.trim().take(24)
        require(cleanName.length >= 2) { "Visitor name must be at least 2 characters." }
        require(cleanUnit.isNotEmpty()) { "Unit number is required." }
        val randomPasscode = (1000..9999).random().toString()
        val now = System.currentTimeMillis()
        val entity = VisitorPassEntity(
            id = UUID.randomUUID().toString(),
            visitorName = cleanName,
            phone = phone?.trim()?.take(20)?.takeIf { it.isNotEmpty() },
            purpose = purpose.name,
            unitNumber = cleanUnit,
            passcode = randomPasscode,
            vehicleNumber = vehicleNumber?.trim()?.uppercase()?.take(20)?.takeIf { it.isNotEmpty() },
            status = VisitorStatus.EXPECTED.name,
            createdAtMillis = now,
            checkInEpochMillis = null,
            checkOutEpochMillis = null
        )
        dao.upsertVisitorPass(entity)
        entity.toDomain()
    }

    suspend fun updateVisitorStatus(
        passId: String,
        nextStatus: VisitorStatus
    ): Result<Unit> = runSafe {
        val now = System.currentTimeMillis()
        val checkIn = if (nextStatus == VisitorStatus.CHECKED_IN) now else null
        val checkOut = if (nextStatus == VisitorStatus.CHECKED_OUT) now else null
        dao.updateVisitorStatus(
            passId = passId,
            status = nextStatus.name,
            checkInEpochMillis = checkIn,
            checkOutEpochMillis = checkOut
        )
    }

    suspend fun deleteVisitorPass(passId: String): Result<Unit> = runSafe {
        dao.deleteVisitorPass(passId)
    }

    suspend fun bookAmenity(
        amenityId: String,
        amenityName: String,
        residentName: String,
        unitLabel: String,
        dateEpochDay: Long,
        timeSlot: String,
        purpose: String
    ): Result<AmenityBooking> = runSafeValue {
        val now = System.currentTimeMillis()
        val entity = AmenityBookingEntity(
            id = UUID.randomUUID().toString(),
            amenityId = amenityId,
            amenityName = amenityName,
            residentName = residentName.trim().take(60),
            unitLabel = unitLabel.trim().take(24),
            dateEpochDay = dateEpochDay,
            timeSlot = timeSlot,
            status = "CONFIRMED",
            purpose = purpose.trim().take(120).ifEmpty { "Resident Booking" },
            createdAtMillis = now
        )
        dao.upsertAmenityBooking(entity)
        entity.toDomain()
    }

    suspend fun cancelAmenityBooking(bookingId: String): Result<Unit> = runSafe {
        dao.updateAmenityBookingStatus(bookingId, "CANCELLED")
    }

    suspend fun castPollVote(pollId: String, optionIndex: Int): Result<Unit> = runSafe {
        val pollEntity = dao.getPollById(pollId) ?: return@runSafe
        val currentDomain = pollEntity.toDomain()
        if (currentDomain.userVotedOptionIndex != null || currentDomain.isClosed) {
            return@runSafe
        }
        val updatedOptions = currentDomain.options.map { opt ->
            if (opt.index == optionIndex) {
                opt.copy(votesCount = opt.votesCount + 1)
            } else {
                opt
            }
        }
        val updatedRaw = SocietyPollEntity.formatOptions(updatedOptions)
        val updatedEntity = pollEntity.copy(
            optionsRaw = updatedRaw,
            totalVotes = pollEntity.totalVotes + 1,
            userVotedOptionIndex = optionIndex
        )
        dao.upsertSocietyPoll(updatedEntity)
    }

    suspend fun createPoll(
        question: String,
        description: String,
        options: List<String>
    ): Result<Unit> = runSafe {
        val now = System.currentTimeMillis()
        val pollOptions = options.filter { it.isNotBlank() }.mapIndexed { index, text ->
            PollOption(index = index, text = text.trim(), votesCount = 0)
        }
        val raw = SocietyPollEntity.formatOptions(pollOptions)
        dao.upsertSocietyPoll(
            SocietyPollEntity(
                id = UUID.randomUUID().toString(),
                question = question.trim().take(150),
                description = description.trim().take(300),
                optionsRaw = raw,
                totalVotes = 0,
                userVotedOptionIndex = null,
                createdAtMillis = now,
                isClosed = false
            )
        )
    }

    suspend fun toggleHelperPremisesStatus(helperId: String, isInPremises: Boolean): Result<Unit> = runSafe {
        val checkIn = if (isInPremises) {
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        } else null
        dao.updateHelperStatus(helperId, isInPremises, checkIn)
    }

    suspend fun addDomesticHelper(
        name: String,
        serviceType: String,
        phone: String,
        assignedUnits: String
    ): Result<DomesticHelper> = runSafeValue {
        val entity = DomesticHelperEntity(
            id = UUID.randomUUID().toString(),
            name = name.trim().take(60),
            serviceType = serviceType,
            phone = phone.trim().take(20),
            assignedUnits = assignedUnits.trim().take(60),
            isInPremises = false,
            lastCheckInTime = null
        )
        dao.upsertDomesticHelper(entity)
        entity.toDomain()
    }

    suspend fun addDirectoryEntry(
        name: String,
        unitNumber: String,
        role: String,
        category: String,
        phone: String,
        email: String?
    ): Result<DirectoryEntry> = runSafeValue {
        val entity = DirectoryEntryEntity(
            id = UUID.randomUUID().toString(),
            name = name.trim().take(60),
            unitNumber = unitNumber.trim().take(24),
            role = role.trim().take(40),
            category = category,
            phoneNumber = phone.trim().take(20),
            email = email?.trim()?.take(60)
        )
        dao.upsertDirectoryEntry(entity)
        entity.toDomain()
    }

    suspend fun seedDefaultsIfEmpty(): Result<Unit> = runSafe {
        val profile = dao.getProfileOnce()
        if (profile == null) {
            dao.upsertProfile(
                UserProfileEntity(
                    id = "local_profile",
                    societyName = "Shafi Homes Arcade",
                    residentName = "Mohammad Asif",
                    unitLabel = "Tower A - 302",
                    defaultMonthlyFee = 3500.0,
                    activeRole = SocietyRole.RESIDENT.name,
                    createdAtMillis = System.currentTimeMillis(),
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
        }

        val noticeCount = dao.getNoticesCount()
        if (noticeCount == 0) {
            val now = System.currentTimeMillis()
            dao.upsertNotice(
                NoticeEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Welcome to Shafi Homes Arcade",
                    content = "Welcome to our smart society management portal. Residents can now pre-approve visitors, view & pay monthly maintenance with UPI, and track building repairs seamlessly.",
                    category = NoticeCategory.GENERAL.name,
                    author = "Managing Committee",
                    isPinned = true,
                    createdAtMillis = now - 3600_000L * 4
                )
            )
            dao.upsertNotice(
                NoticeEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Overhead Water Tank Cleaning Schedule",
                    content = "Please note that annual overhead tank disinfection and pressure cleaning will occur this Saturday between 10:00 AM and 02:00 PM. Water supply will be paused temporarily.",
                    category = NoticeCategory.MAINTENANCE.name,
                    author = "Facility Manager",
                    isPinned = false,
                    createdAtMillis = now - 3600_000L * 2
                )
            )
        }

        val directoryCount = dao.getDirectoryCount()
        if (directoryCount == 0) {
            val directory = listOf(
                DirectoryEntryEntity(UUID.randomUUID().toString(), "Syed Mohammad Asif", "Tower A - 302", "President", "COMMITTEE", "+91 98200 44332", "asifalikhan@programmer.net"),
                DirectoryEntryEntity(UUID.randomUUID().toString(), "Tariq Mahmood", "Tower A - 401", "Secretary", "COMMITTEE", "+91 98765 43210", "secretary@shafihomes.com"),
                DirectoryEntryEntity(UUID.randomUUID().toString(), "Farhan Qureshi", "Tower B - 204", "Treasurer", "COMMITTEE", "+91 98111 22334", "treasurer@shafihomes.com"),
                DirectoryEntryEntity(UUID.randomUUID().toString(), "Abdul Basit", "Security Gate", "Head Security Guard", "STAFF", "+91 98200 11223", null),
                DirectoryEntryEntity(UUID.randomUUID().toString(), "Suresh Sharma", "Utility Desk", "Society Electrician", "STAFF", "+91 98333 44556", null),
                DirectoryEntryEntity(UUID.randomUUID().toString(), "Ramesh Kumar", "Utility Desk", "Society Plumber", "STAFF", "+91 98444 55667", null),
                DirectoryEntryEntity(UUID.randomUUID().toString(), "Zubair Ahmed", "Tower A - 101", "Resident", "RESIDENT", "+91 98555 66778", null),
                DirectoryEntryEntity(UUID.randomUUID().toString(), "Imran Khan", "Tower A - 102", "Resident", "RESIDENT", "+91 98666 77889", null),
                DirectoryEntryEntity(UUID.randomUUID().toString(), "Dr. Rashid Ali", "Tower B - 301", "Resident", "RESIDENT", "+91 98777 88990", null)
            )
            for (entry in directory) {
                dao.upsertDirectoryEntry(entry)
            }
        }

        val pollsCount = dao.getPollsCount()
        if (pollsCount == 0) {
            val now = System.currentTimeMillis()
            dao.upsertSocietyPoll(
                SocietyPollEntity(
                    id = UUID.randomUUID().toString(),
                    question = "Should we install a 15kW Rooftop Solar System?",
                    description = "Estimated to cut society common area lighting and lift electricity costs by 65%. Subsidy applicable under PM Surya Ghar Yojana.",
                    optionsRaw = "Yes, approve immediately:::14|||Need more vendor quotes:::4|||No, postpone for now:::1",
                    totalVotes = 19,
                    userVotedOptionIndex = null,
                    createdAtMillis = now - 86400_000L * 2,
                    isClosed = false
                )
            )
            dao.upsertSocietyPoll(
                SocietyPollEntity(
                    id = UUID.randomUUID().toString(),
                    question = "Proposed Exterior Building Painting Color Theme",
                    description = "Society facade repainting is planned for next quarter. Choose your preferred weather-proof exterior color combination.",
                    optionsRaw = "Royal Cream & Sandstone Yellow:::11|||Slate Grey & Charcoal Accent:::9|||Warm Terracotta & Off-White:::5",
                    totalVotes = 25,
                    userVotedOptionIndex = null,
                    createdAtMillis = now - 86400_000L * 5,
                    isClosed = false
                )
            )
        }

        val helpersCount = dao.getHelpersCount()
        if (helpersCount == 0) {
            val helpers = listOf(
                DomesticHelperEntity(UUID.randomUUID().toString(), "Sunita Bai", "MAID", "+91 98991 11223", "Tower A: 101, 202, 302", true, "08:15 AM"),
                DomesticHelperEntity(UUID.randomUUID().toString(), "Ramesh Verma", "DRIVER", "+91 98992 22334", "Tower B: 204", true, "08:45 AM"),
                DomesticHelperEntity(UUID.randomUUID().toString(), "Anita Devi", "COOK", "+91 98993 33445", "Tower A: 401, 402", false, null),
                DomesticHelperEntity(UUID.randomUUID().toString(), "Imran Ali", "CAR_CLEANER", "+91 98994 44556", "Basement Parking: Slots 1-18", true, "06:30 AM")
            )
            for (h in helpers) {
                dao.upsertDomesticHelper(h)
            }
        }
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
