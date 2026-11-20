package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SocietyRepository
import com.example.data.model.Contractor
import com.example.data.model.DueStatus
import com.example.data.model.MaintenanceDue
import com.example.data.model.Notice
import com.example.data.model.NoticeCategory
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
import com.example.data.model.VisitorPass
import com.example.data.model.VisitorPurpose
import com.example.data.model.VisitorStatus
import com.example.data.scheduling.AutoScheduler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SocietyMetrics(
    val collectedAmount: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val overdueAmount: Double = 0.0,
    val totalBilledAmount: Double = 0.0,
    val collectionRatePercent: Int = 0,
    val openRepairsCount: Int = 0,
    val unscheduledRepairsCount: Int = 0,
    val urgentRepairsCount: Int = 0,
    val completedRepairsCount: Int = 0,
    val activeContractorsCount: Int = 0,
    val scheduledJobsCount: Int = 0
)

data class SocietyDashboardState(
    val profile: UserProfile? = null,
    val dues: List<MaintenanceDue> = emptyList(),
    val contractors: List<Contractor> = emptyList(),
    val tickets: List<RepairTicket> = emptyList(),
    val schedules: List<ScheduleEntry> = emptyList(),
    val notices: List<Notice> = emptyList(),
    val visitors: List<VisitorPass> = emptyList(),
    val metrics: SocietyMetrics = SocietyMetrics(),
    val unscheduledDiagnostics: Map<String, String> = emptyMap()
)

class SocietyViewModel(
    private val repository: SocietyRepository
) : ViewModel() {

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _unscheduledReasons = MutableStateFlow<Map<String, String>>(emptyMap())

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private fun <T> Flow<T>.toUiState(label: String): StateFlow<UiState<T>> =
        this.map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w(TAG, "Error observing $label", error)
                emit(UiState.Error(error.message ?: "Failed to load $label"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    private val profileFlow = repository.observeUserProfile().toUiState("society profile")
    private val duesFlow = repository.observeMaintenanceDues().toUiState("maintenance dues")
    private val contractorsFlow = repository.observeContractors().toUiState("contractors")
    private val ticketsFlow = repository.observeRepairTickets().toUiState("repair tickets")
    private val schedulesFlow = repository.observeScheduleEntries().toUiState("schedule entries")
    private val noticesFlow = repository.observeNotices().toUiState("notices")
    private val visitorsFlow = repository.observeVisitorPasses().toUiState("visitor passes")

    init {
        viewModelScope.launch {
            repository.seedDefaultsIfEmpty()
        }
    }

    val uiState: StateFlow<UiState<SocietyDashboardState>> = combine(
        profileFlow,
        duesFlow,
        contractorsFlow,
        ticketsFlow,
        schedulesFlow,
        noticesFlow,
        visitorsFlow,
        _unscheduledReasons
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val profileState = values[0] as UiState<UserProfile?>
        @Suppress("UNCHECKED_CAST")
        val duesState = values[1] as UiState<List<MaintenanceDue>>
        @Suppress("UNCHECKED_CAST")
        val contractorsState = values[2] as UiState<List<Contractor>>
        @Suppress("UNCHECKED_CAST")
        val ticketsState = values[3] as UiState<List<RepairTicket>>
        @Suppress("UNCHECKED_CAST")
        val schedulesState = values[4] as UiState<List<ScheduleEntry>>
        @Suppress("UNCHECKED_CAST")
        val noticesState = values[5] as UiState<List<Notice>>
        @Suppress("UNCHECKED_CAST")
        val visitorsState = values[6] as UiState<List<VisitorPass>>
        @Suppress("UNCHECKED_CAST")
        val diagnostics = values[7] as Map<String, String>

        val firstError = listOf(profileState, duesState, contractorsState, ticketsState, schedulesState, noticesState, visitorsState)
            .filterIsInstance<UiState.Error>()
            .firstOrNull()

        if (firstError != null) {
            return@combine firstError
        }

        val anyLoading = listOf(profileState, duesState, contractorsState, ticketsState, schedulesState, noticesState, visitorsState)
            .any { it is UiState.Loading }

        if (anyLoading) {
            return@combine UiState.Loading
        }

        val profile = (profileState as UiState.Success).data
        val dues = (duesState as UiState.Success).data
            .sortedByDescending { it.createdAtMillis }
        val contractors = (contractorsState as UiState.Success).data
            .sortedBy { it.name }
        val tickets = (ticketsState as UiState.Success).data
            .sortedWith(
                compareByDescending<RepairTicket> { it.priorityEnum.rank }
                    .thenByDescending { it.createdAtMillis }
            )
        val schedules = (schedulesState as UiState.Success).data
            .sortedWith(
                compareBy<ScheduleEntry> { it.scheduledDateEpochDay }
                    .thenBy { it.slotEnum.startHour }
                    .thenByDescending { it.priorityEnum.rank }
            )
        val notices = (noticesState as UiState.Success).data
            .sortedWith(
                compareByDescending<Notice> { it.isPinned }
                    .thenByDescending { it.createdAtMillis }
            )
        val visitors = (visitorsState as UiState.Success).data
            .sortedByDescending { it.createdAtMillis }

        val collected = dues.filter { it.statusEnum == DueStatus.PAID }.sumOf { it.totalAmount }
        val pending = dues.filter { it.statusEnum == DueStatus.PENDING }.sumOf { it.totalAmount }
        val overdue = dues.filter { it.statusEnum == DueStatus.OVERDUE }.sumOf { it.totalAmount }
        val totalBilled = collected + pending + overdue
        val rate = if (totalBilled > 0.0) ((collected / totalBilled) * 100.0).toInt().coerceIn(0, 100) else 0

        val openTickets = tickets.filter {
            it.statusEnum != TicketStatus.COMPLETED && it.statusEnum != TicketStatus.CANCELLED
        }
        val unscheduledCount = tickets.count { it.statusEnum == TicketStatus.LOGGED }
        val urgentCount = openTickets.count {
            it.priorityEnum == TicketPriority.EMERGENCY || it.priorityEnum == TicketPriority.HIGH
        }
        val completedCount = tickets.count { it.statusEnum == TicketStatus.COMPLETED }
        val activeContractors = contractors.count { it.available }
        val activeJobs = schedules.count {
            it.statusEnum != ScheduleStatus.DONE && it.statusEnum != ScheduleStatus.CANCELLED
        }

        UiState.Success(
            SocietyDashboardState(
                profile = profile,
                dues = dues,
                contractors = contractors,
                tickets = tickets,
                schedules = schedules,
                notices = notices,
                visitors = visitors,
                metrics = SocietyMetrics(
                    collectedAmount = collected,
                    pendingAmount = pending,
                    overdueAmount = overdue,
                    totalBilledAmount = totalBilled,
                    collectionRatePercent = rate,
                    openRepairsCount = openTickets.size,
                    unscheduledRepairsCount = unscheduledCount,
                    urgentRepairsCount = urgentCount,
                    completedRepairsCount = completedCount,
                    activeContractorsCount = activeContractors,
                    scheduledJobsCount = activeJobs
                ),
                unscheduledDiagnostics = diagnostics
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading
    )

    fun saveProfile(
        societyName: String,
        residentName: String,
        unitLabel: String,
        defaultMonthlyFee: Double,
        activeRole: SocietyRole
    ) {
        launchMutation("Profile saved") {
            repository.saveUserProfile(
                societyName = societyName,
                residentName = residentName,
                unitLabel = unitLabel,
                defaultMonthlyFee = defaultMonthlyFee,
                activeRole = activeRole
            )
        }
    }

    fun switchRole(role: SocietyRole) {
        launchMutation("Switched to ${role.label} view") {
            repository.updateActiveRole(role)
        }
    }

    fun createDue(
        unitNumber: String,
        residentName: String,
        billingPeriod: String,
        baseAmount: Double,
        sinkingFund: Double,
        utilityAmount: Double,
        dueDateEpochDay: Long,
        notes: String?
    ) {
        launchMutation("Maintenance bill added") {
            repository.createMaintenanceDue(
                unitNumber = unitNumber,
                residentName = residentName,
                billingPeriod = billingPeriod,
                baseAmount = baseAmount,
                sinkingFund = sinkingFund,
                utilityAmount = utilityAmount,
                dueDateEpochDay = dueDateEpochDay,
                notes = notes
            )
        }
    }

    fun recordPayment(
        due: MaintenanceDue,
        method: PaymentMethod,
        receiptNumber: String,
        notes: String?
    ) {
        launchMutation("Payment recorded (${receiptNumber.trim()})") {
            repository.recordDuePayment(due, method, receiptNumber, notes)
        }
    }

    fun markOverdue(due: MaintenanceDue, lateFee: Double) {
        launchMutation("Marked overdue") {
            repository.markDueOverdue(due, lateFee)
        }
    }

    fun deleteDue(dueId: String) {
        launchMutation("Maintenance record removed") {
            repository.deleteMaintenanceDue(dueId)
        }
    }

    fun registerContractor(
        name: String,
        company: String,
        specialty: TradeCategory,
        phoneSuffix: String?,
        shiftStartHour: Long,
        shiftEndHour: Long,
        dailyCapacity: Long,
        hourlyRate: Double
    ) {
        launchMutation("Contractor registered") {
            repository.createContractor(
                name = name,
                company = company,
                specialty = specialty,
                phoneSuffix = phoneSuffix,
                shiftStartHour = shiftStartHour,
                shiftEndHour = shiftEndHour,
                dailyCapacity = dailyCapacity,
                hourlyRate = hourlyRate
            )
        }
    }

    fun toggleContractorAvailability(contractorId: String, available: Boolean) {
        launchMutation(if (available) "Contractor marked available" else "Contractor paused") {
            repository.toggleContractorAvailability(contractorId, available)
        }
    }

    fun deleteContractor(contractorId: String) {
        launchMutation("Contractor removed") {
            repository.deleteContractor(contractorId)
        }
    }

    fun logRepairTicket(
        unitLocation: String,
        title: String,
        description: String,
        category: TradeCategory,
        priority: TicketPriority,
        estimatedCost: Double,
        photoLabel: String?,
        photoUri: String?,
        autoScheduleNow: Boolean
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                val created = repository.createRepairTicket(
                    unitLocation = unitLocation,
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    estimatedCost = estimatedCost,
                    photoLabel = photoLabel,
                    photoUri = photoUri
                )
                created.fold(
                    onSuccess = { newTicket ->
                        if (autoScheduleNow) {
                            val current = (uiState.value as? UiState.Success)?.data
                            if (current != null) {
                                val plan = AutoScheduler.buildSchedulePlan(
                                    tickets = listOf(newTicket),
                                    contractors = current.contractors,
                                    existingEntries = current.schedules
                                )
                                if (plan.proposals.isNotEmpty()) {
                                    repository.applyScheduleProposals(plan.proposals)
                                        .onSuccess {
                                            val p = plan.proposals.first()
                                            _events.tryEmit(
                                                "Logged & auto-scheduled to ${p.contractor.name} (${p.slot.windowText})"
                                            )
                                        }
                                        .onFailure { err ->
                                            _events.tryEmit("Ticket logged, auto-schedule failed: ${err.localizedMessage}")
                                        }
                                } else {
                                    val reason = plan.unscheduledReasons[newTicket.id]
                                        ?: "No matching contractor slot open yet"
                                    _unscheduledReasons.value = _unscheduledReasons.value + (newTicket.id to reason)
                                    _events.tryEmit("Repair logged. Auto-schedule note: $reason")
                                }
                            } else {
                                _events.tryEmit("Repair ticket logged")
                            }
                        } else {
                            _events.tryEmit("Repair ticket logged")
                        }
                    },
                    onFailure = { err ->
                        _events.tryEmit(err.localizedMessage ?: "Failed to log repair")
                    }
                )
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun updateRepairProgress(
        ticket: RepairTicket,
        newStatus: TicketStatus,
        actualCost: Double,
        workNotes: String?
    ) {
        launchMutation("Repair log updated") {
            repository.updateRepairProgress(ticket, newStatus, actualCost, workNotes)
        }
    }

    fun deleteRepairTicket(ticketId: String) {
        launchMutation("Repair ticket deleted") {
            repository.deleteRepairTicket(ticketId)
        }
    }

    fun runAutoScheduler(singleTicket: RepairTicket? = null) {
        val current = (uiState.value as? UiState.Success)?.data ?: return
        val targetTickets = if (singleTicket != null) listOf(singleTicket) else current.tickets
        val plan = AutoScheduler.buildSchedulePlan(
            tickets = targetTickets,
            contractors = current.contractors,
            existingEntries = current.schedules
        )
        _unscheduledReasons.value = plan.unscheduledReasons

        if (plan.proposals.isEmpty()) {
            val firstReason = plan.unscheduledReasons.values.firstOrNull()
                ?: "All logged repair tickets are already scheduled."
            _events.tryEmit(firstReason)
            return
        }

        viewModelScope.launch {
            _isBusy.value = true
            try {
                repository.applyScheduleProposals(plan.proposals)
                    .onSuccess { count ->
                        val skipped = plan.unscheduledReasons.size
                        val summary = if (skipped > 0) {
                            "Auto-scheduled $count ticket(s); $skipped waiting for contractor capacity"
                        } else {
                            "Auto-scheduled $count repair ticket(s) across available contractors"
                        }
                        _events.tryEmit(summary)
                    }
                    .onFailure { err ->
                        _events.tryEmit(err.localizedMessage ?: "Auto-scheduling failed")
                    }
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun advanceScheduleStatus(
        entry: ScheduleEntry,
        nextStatus: ScheduleStatus,
        actualCost: Double? = null,
        workNotes: String? = null
    ) {
        launchMutation("Visit updated to ${nextStatus.label}") {
            repository.advanceScheduleEntry(entry, nextStatus, actualCost, workNotes)
        }
    }

    fun rescheduleVisit(entry: ScheduleEntry, newEpochDay: Long, newSlot: ScheduleSlot) {
        launchMutation("Visit rescheduled to ${newSlot.windowText}") {
            repository.rescheduleEntry(entry, newEpochDay, newSlot)
        }
    }

    fun createNotice(
        title: String,
        content: String,
        category: NoticeCategory,
        author: String = "Managing Committee",
        isPinned: Boolean = false
    ) {
        launchMutation("Notice published to board") {
            repository.createNotice(title, content, category, author, isPinned)
        }
    }

    fun deleteNotice(id: String) {
        launchMutation("Notice deleted") {
            repository.deleteNotice(id)
        }
    }

    fun createVisitorPass(
        visitorName: String,
        phone: String?,
        purpose: VisitorPurpose,
        unitNumber: String,
        vehicleNumber: String? = null
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                repository.createVisitorPass(visitorName, phone, purpose, unitNumber, vehicleNumber)
                    .onSuccess { pass ->
                        _events.tryEmit("Pass created for ${pass.visitorName} • Code: ${pass.passcode}")
                    }
                    .onFailure { err ->
                        _events.tryEmit(err.localizedMessage ?: "Failed to create pass")
                    }
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun updateVisitorStatus(passId: String, nextStatus: VisitorStatus) {
        launchMutation("Visitor marked as ${nextStatus.label}") {
            repository.updateVisitorStatus(passId, nextStatus)
        }
    }

    fun deleteVisitorPass(passId: String) {
        launchMutation("Visitor pass removed") {
            repository.deleteVisitorPass(passId)
        }
    }

    private fun launchMutation(successMessage: String, block: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                block()
                    .onSuccess { _events.tryEmit(successMessage) }
                    .onFailure { err ->
                        _events.tryEmit(err.localizedMessage ?: "Action failed")
                    }
            } finally {
                _isBusy.value = false
            }
        }
    }

    private companion object {
        const val TAG = "SocietyVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
