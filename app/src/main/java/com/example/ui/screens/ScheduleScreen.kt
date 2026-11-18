package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Contractor
import com.example.data.model.ScheduleEntry
import com.example.data.model.ScheduleSlot
import com.example.data.model.ScheduleStatus
import com.example.data.model.TradeCategory
import com.example.data.scheduling.AutoScheduler
import com.example.ui.SocietyDashboardState
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.MetricTile
import com.example.ui.components.MonoTag
import com.example.ui.components.PriorityBadge
import com.example.ui.components.ScheduleStatusBadge
import com.example.ui.components.TradeBadge
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatEpochDay
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusPaidGreen

@Composable
fun ScheduleScreen(
    state: SocietyDashboardState,
    onRunAutoScheduler: () -> Unit,
    onAdvanceEntry: (ScheduleEntry, ScheduleStatus, Double?, String?) -> Unit,
    onRescheduleEntry: (ScheduleEntry, Long, ScheduleSlot) -> Unit,
    onOpenAddContractor: () -> Unit,
    onToggleContractorAvailable: (String, Boolean) -> Unit,
    onDeleteContractor: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var completeTarget by remember { mutableStateOf<ScheduleEntry?>(null) }
    var rescheduleTarget by remember { mutableStateOf<ScheduleEntry?>(null) }
    var deleteContractorTarget by remember { mutableStateOf<Contractor?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            SegmentedButton(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                modifier = Modifier.testTag("schedule_tab_dispatch")
            ) {
                Text("Dispatch Board (${state.schedules.size})")
            }
            SegmentedButton(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                modifier = Modifier.testTag("schedule_tab_contractors")
            ) {
                Text("Contractors (${state.contractors.size})")
            }
        }

        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricTile(
                            label = "Scheduled Visits",
                            value = state.metrics.scheduledJobsCount.toString(),
                            sublabel = "${state.metrics.unscheduledRepairsCount} pending auto-assign",
                            accentColor = MaterialTheme.colorScheme.primary,
                            icon = Icons.Rounded.Schedule,
                            modifier = Modifier.weight(1f)
                        )
                        MetricTile(
                            label = "Active Crew",
                            value = state.metrics.activeContractorsCount.toString(),
                            sublabel = "${state.metrics.completedRepairsCount} completed repairs",
                            accentColor = StatusPaidGreen,
                            icon = Icons.Rounded.Engineering,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                // Auto-Scheduler Engine Banner
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Automated Contractor Dispatch",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${state.metrics.unscheduledRepairsCount} awaiting ticket(s) • ${state.metrics.activeContractorsCount} active contractor(s)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (state.unscheduledDiagnostics.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                state.unscheduledDiagnostics.values.distinct().take(2).forEach { msg ->
                                    Text(
                                        text = "• $msg",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onRunAutoScheduler,
                                    enabled = state.metrics.unscheduledRepairsCount > 0,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("run_auto_scheduler_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Rounded.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Run Auto-Scheduler")
                                }
                                if (state.contractors.isEmpty()) {
                                    OutlinedButton(
                                        onClick = { selectedTab = 1 },
                                        modifier = Modifier.height(48.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Add Crew")
                                    }
                                }
                            }
                        }
                    }
                }

                if (state.schedules.isEmpty()) {
                    item {
                        EmptyStatePanel(
                            title = "No Scheduled Visits",
                            subtitle = "Register maintenance contractors and run the Auto-Scheduler to assign open repair tickets into conflict-free slots.",
                            actionLabel = if (state.contractors.isEmpty()) "Register Contractor" else "Run Auto-Scheduler",
                            onAction = {
                                if (state.contractors.isEmpty()) selectedTab = 1 else onRunAutoScheduler()
                            }
                        )
                    }
                } else {
                    items(state.schedules, key = { it.id }) { entry ->
                        ScheduleEntryCard(
                            entry = entry,
                            onAdvance = { next ->
                                if (next == ScheduleStatus.DONE) {
                                    completeTarget = entry
                                } else {
                                    onAdvanceEntry(entry, next, null, null)
                                }
                            },
                            onReschedule = { rescheduleTarget = entry }
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Maintenance Crew Directory", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "Specialty, shift window & daily capacity drive auto-scheduling.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = onOpenAddContractor,
                                modifier = Modifier.testTag("open_add_contractor_button")
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add")
                            }
                        }
                    }
                }

                if (state.contractors.isEmpty()) {
                    item {
                        EmptyStatePanel(
                            title = "No Maintenance Contractors Registered",
                            subtitle = "Add plumbing, electrical, elevator, HVAC, or civil specialists with shift hours and daily job limits.",
                            actionLabel = "Register First Contractor",
                            onAction = onOpenAddContractor
                        )
                    }
                } else {
                    items(state.contractors, key = { it.id }) { contractor ->
                        val activeToday = state.schedules.count {
                            it.contractorId == contractor.id &&
                                it.scheduledDateEpochDay == AutoScheduler.currentEpochDay() &&
                                it.statusEnum != ScheduleStatus.DONE &&
                                it.statusEnum != ScheduleStatus.CANCELLED
                        }
                        ContractorCard(
                            contractor = contractor,
                            todayLoad = activeToday,
                            onToggleAvailable = { avail -> onToggleContractorAvailable(contractor.id, avail) },
                            onDelete = { deleteContractorTarget = contractor }
                        )
                    }
                }
            }
        }
    }

    completeTarget?.let { target ->
        CompleteVisitDialog(
            entry = target,
            onDismiss = { completeTarget = null },
            onConfirm = { actualCost, notes ->
                onAdvanceEntry(target, ScheduleStatus.DONE, actualCost, notes)
                completeTarget = null
            }
        )
    }

    rescheduleTarget?.let { target ->
        RescheduleDialog(
            entry = target,
            onDismiss = { rescheduleTarget = null },
            onConfirm = { newDay, newSlot ->
                onRescheduleEntry(target, newDay, newSlot)
                rescheduleTarget = null
            }
        )
    }

    deleteContractorTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteContractorTarget = null },
            title = { Text("Remove Contractor?") },
            text = { Text("Remove ${target.name} (${target.company}) from the contractor directory?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteContractor(target.id)
                    deleteContractorTarget = null
                }) {
                    Text("Remove", color = StatusOverdueRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteContractorTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ScheduleEntryCard(
    entry: ScheduleEntry,
    onAdvance: (ScheduleStatus) -> Unit,
    onReschedule: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    MonoTag(text = entry.unitLocation)
                    TradeBadge(trade = entry.specialtyEnum)
                }
                ScheduleStatusBadge(status = entry.statusEnum)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.ticketTitle,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                PriorityBadge(priority = entry.priorityEnum)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Engineering,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = entry.contractorName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${formatEpochDay(entry.scheduledDateEpochDay)} • ${entry.slotEnum.windowText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = entry.matchReason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            if (entry.statusEnum != ScheduleStatus.DONE && entry.statusEnum != ScheduleStatus.CANCELLED) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (entry.statusEnum) {
                        ScheduleStatus.SCHEDULED -> {
                            Button(
                                onClick = { onAdvance(ScheduleStatus.EN_ROUTE) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.DirectionsCar, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("En Route")
                            }
                        }
                        ScheduleStatus.EN_ROUTE -> {
                            Button(
                                onClick = { onAdvance(ScheduleStatus.IN_WORK) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Work")
                            }
                        }
                        ScheduleStatus.IN_WORK -> {
                            Button(
                                onClick = { onAdvance(ScheduleStatus.DONE) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusPaidGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Complete & Log")
                            }
                        }
                        else -> Unit
                    }

                    OutlinedButton(
                        onClick = onReschedule,
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.EditCalendar, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Slot")
                    }
                }
            }
        }
    }
}

@Composable
private fun ContractorCard(
    contractor: Contractor,
    todayLoad: Int,
    onToggleAvailable: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contractor.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = contractor.company,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TradeBadge(trade = contractor.specialtyEnum)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Shift", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${contractor.shiftStartHour}:00–${contractor.shiftEndHour}:00",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Column {
                    Text("Today Load", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "$todayLoad / ${contractor.dailyCapacity} slots",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Rate / Hr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = formatCurrency(contractor.hourlyRate),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Switch(
                        checked = contractor.available,
                        onCheckedChange = onToggleAvailable
                    )
                    Text(
                        text = if (contractor.available) "Available (${contractor.completedJobs} done)" else "Paused",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (contractor.available) StatusPaidGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete contractor",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterContractorSheet(
    onDismiss: () -> Unit,
    onSubmit: (
        name: String,
        company: String,
        specialty: TradeCategory,
        phoneSuffix: String?,
        shiftStartHour: Long,
        shiftEndHour: Long,
        dailyCapacity: Long,
        hourlyRate: Double
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf(TradeCategory.PLUMBING) }
    var phoneSuffix by remember { mutableStateOf("") }
    var shiftStart by remember { mutableStateOf("8") }
    var shiftEnd by remember { mutableStateOf("20") }
    var capacity by remember { mutableStateOf("4") }
    var hourlyRate by remember { mutableStateOf("45") }
    var validationError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Register Maintenance Contractor", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "The Auto-Scheduler matches tickets to active contractors using trade, shift hours, and daily slot capacity.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Lead Technician Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contractor_name_input")
            )
            OutlinedTextField(
                value = company,
                onValueChange = { company = it },
                label = { Text("Contractor Firm / Team") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contractor_company_input")
            )

            Text("Trade Specialty", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TradeCategory.entries.forEach { trade ->
                    FilterChip(
                        selected = specialty == trade,
                        onClick = { specialty = trade },
                        label = { Text(trade.label) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = shiftStart,
                    onValueChange = { shiftStart = it },
                    label = { Text("Shift Start (0-23)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = shiftEnd,
                    onValueChange = { shiftEnd = it },
                    label = { Text("Shift End (1-24)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = { Text("Max Jobs / Day") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = hourlyRate,
                    onValueChange = { hourlyRate = it },
                    label = { Text("Hourly Rate") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = phoneSuffix,
                onValueChange = { phoneSuffix = it },
                label = { Text("Contact Extension / Last 4 Digits (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (validationError != null) {
                Text(
                    text = validationError ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusOverdueRed
                )
            }

            Button(
                onClick = {
                    val startH = shiftStart.toLongOrNull() ?: 8L
                    val endH = shiftEnd.toLongOrNull() ?: 20L
                    val cap = capacity.toLongOrNull() ?: 4L
                    val rate = hourlyRate.toDoubleOrNull() ?: 0.0
                    when {
                        name.trim().length < 2 -> validationError = "Contractor name must be at least 2 characters."
                        company.trim().length < 2 -> validationError = "Company name must be at least 2 characters."
                        startH !in 0L..23L || endH !in (startH + 1)..24L -> validationError = "Shift end must be after start (0-24)."
                        cap !in 1L..12L -> validationError = "Daily capacity must be 1 to 12."
                        rate < 0.0 -> validationError = "Hourly rate must be 0 or higher."
                        else -> onSubmit(
                            name.trim(),
                            company.trim(),
                            specialty,
                            phoneSuffix.trim().takeIf { it.isNotEmpty() },
                            startH,
                            endH,
                            cap,
                            rate
                        )
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_contractor_button")
            ) {
                Text("Save Contractor")
            }
        }
    }
}

@Composable
private fun CompleteVisitDialog(
    entry: ScheduleEntry,
    onDismiss: () -> Unit,
    onConfirm: (Double, String?) -> Unit
) {
    var actualCost by remember { mutableStateOf("750") }
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Complete Repair Visit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${entry.ticketTitle} (${entry.unitLocation})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = actualCost,
                    onValueChange = { actualCost = it },
                    label = { Text("Final Repair Cost") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Completion & Parts Log") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cost = (actualCost.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
                    onConfirm(cost, notes.trim().takeIf { it.isNotEmpty() })
                }
            ) {
                Text("Mark Completed")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun RescheduleDialog(
    entry: ScheduleEntry,
    onDismiss: () -> Unit,
    onConfirm: (Long, ScheduleSlot) -> Unit
) {
    val today = remember { AutoScheduler.currentEpochDay() }
    var selectedDayOffset by remember { mutableStateOf((entry.scheduledDateEpochDay - today).coerceIn(0L, 6L)) }
    var selectedSlot by remember { mutableStateOf(entry.slotEnum) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust Visit Window") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Day", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (0L..4L).forEach { offset ->
                        val day = today + offset
                        FilterChip(
                            selected = selectedDayOffset == offset,
                            onClick = { selectedDayOffset = offset },
                            label = { Text(if (offset == 0L) "Today" else formatEpochDay(day)) }
                        )
                    }
                }
                Text("Select Time Slot", style = MaterialTheme.typography.labelLarge)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ScheduleSlot.entries.forEach { slot ->
                        FilterChip(
                            selected = selectedSlot == slot,
                            onClick = { selectedSlot = slot },
                            label = { Text("${slot.label} (${slot.windowText})") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(today + selectedDayOffset, selectedSlot) }) {
                Text("Reschedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
