package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.RepairTicket
import com.example.data.model.TicketStatus
import com.example.ui.SocietyDashboardState
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.MonoTag
import com.example.ui.components.PriorityBadge
import com.example.ui.components.RequestProgressBar
import com.example.ui.components.TicketStatusBadge
import com.example.ui.components.TradeBadge
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatEpochDay
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.StatusPendingBg
import com.example.ui.theme.StatusScheduledBg
import com.example.ui.theme.StatusScheduledBlue

private enum class RequestStatusFilter(val label: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed")
}

private fun TicketStatus.toRequestStatusFilter(): RequestStatusFilter? = when (this) {
    TicketStatus.LOGGED, TicketStatus.SCHEDULED -> RequestStatusFilter.PENDING
    TicketStatus.IN_PROGRESS -> RequestStatusFilter.IN_PROGRESS
    TicketStatus.COMPLETED -> RequestStatusFilter.COMPLETED
    TicketStatus.CANCELLED -> null
}

@Composable
fun RepairLogsScreen(
    state: SocietyDashboardState,
    onOpenLogRepair: () -> Unit,
    onAutoScheduleTicket: (RepairTicket) -> Unit,
    onUpdateProgress: (RepairTicket, TicketStatus, Double, String?) -> Unit,
    onDeleteTicket: (String) -> Unit,
    modifier: Modifier = Modifier,
    onAutoScheduleAll: () -> Unit = {}
) {
    var selectedFilter by rememberSaveable { mutableStateOf<RequestStatusFilter?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var progressTarget by remember { mutableStateOf<RepairTicket?>(null) }
    var deleteTarget by remember { mutableStateOf<RepairTicket?>(null) }
    val focusManager = LocalFocusManager.current

    val pendingCount = remember(state.tickets) {
        state.tickets.count { it.statusEnum.toRequestStatusFilter() == RequestStatusFilter.PENDING }
    }
    val inProgressCount = remember(state.tickets) {
        state.tickets.count { it.statusEnum.toRequestStatusFilter() == RequestStatusFilter.IN_PROGRESS }
    }
    val completedCount = remember(state.tickets) {
        state.tickets.count { it.statusEnum.toRequestStatusFilter() == RequestStatusFilter.COMPLETED }
    }

    val filteredTickets = remember(state.tickets, selectedFilter, searchQuery) {
        val statusFiltered = if (selectedFilter == null) state.tickets
        else state.tickets.filter { it.statusEnum.toRequestStatusFilter() == selectedFilter }

        val query = searchQuery.trim()
        if (query.isEmpty()) {
            statusFiltered
        } else {
            statusFiltered.filter { ticket ->
                ticket.description.contains(query, ignoreCase = true) ||
                    ticket.title.contains(query, ignoreCase = true) ||
                    ticket.unitLocation.contains(query, ignoreCase = true)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search issues by description...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search maintenance requests",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.testTag("requests_search_clear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .testTag("requests_search_bar")
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("requests_filter_bar"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.FilterList,
                contentDescription = "Filter maintenance requests",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            FilterChip(
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                label = { Text("All (${state.tickets.size})") },
                modifier = Modifier.testTag("requests_filter_chip_all")
            )
            FilterChip(
                selected = selectedFilter == RequestStatusFilter.PENDING,
                onClick = {
                    selectedFilter = if (selectedFilter == RequestStatusFilter.PENDING) null
                    else RequestStatusFilter.PENDING
                },
                label = { Text("Pending ($pendingCount)") },
                modifier = Modifier.testTag("requests_filter_chip_pending")
            )
            FilterChip(
                selected = selectedFilter == RequestStatusFilter.IN_PROGRESS,
                onClick = {
                    selectedFilter = if (selectedFilter == RequestStatusFilter.IN_PROGRESS) null
                    else RequestStatusFilter.IN_PROGRESS
                },
                label = { Text("In Progress ($inProgressCount)") },
                modifier = Modifier.testTag("requests_filter_chip_in_progress")
            )
            FilterChip(
                selected = selectedFilter == RequestStatusFilter.COMPLETED,
                onClick = {
                    selectedFilter = if (selectedFilter == RequestStatusFilter.COMPLETED) null
                    else RequestStatusFilter.COMPLETED
                },
                label = { Text("Completed ($completedCount)") },
                modifier = Modifier.testTag("requests_filter_chip_completed")
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("repairs_lazy_column"),
            contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(136.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_society_hero),
                        contentDescription = stringResource(id = R.string.cd_society_hero),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF0E2B32).copy(alpha = 0.92f),
                                        Color(0xFF0E2B32).copy(alpha = 0.45f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = state.profile?.unitLabel ?: "Tower A - 101",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Text(
                                    text = "${state.metrics.openRepairsCount} Open",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Maintenance Requests",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${state.metrics.unscheduledRepairsCount} awaiting dispatch • ${state.metrics.completedRepairsCount} completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onOpenLogRepair,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("repairs_header_log_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Log Issue")
                        }
                        OutlinedButton(
                            onClick = onAutoScheduleAll,
                            enabled = state.metrics.unscheduledRepairsCount > 0,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("requests_auto_schedule_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Auto-Assign", maxLines = 1)
                        }
                    }
                }
            }

            item {
                RequestStatusSectionHeader(
                    totalCount = state.tickets.size,
                    pendingCount = pendingCount,
                    inProgressCount = inProgressCount,
                    completedCount = completedCount,
                    selectedFilter = selectedFilter,
                    onSelectFilter = { clicked ->
                        selectedFilter = if (selectedFilter == clicked) null else clicked
                    },
                    onShowAll = { selectedFilter = null }
                )
            }

            if (filteredTickets.isEmpty()) {
                item {
                    val activeFilter = selectedFilter
                    val hasSearch = searchQuery.isNotBlank()
                    EmptyStatePanel(
                        title = when {
                            state.tickets.isEmpty() -> "No Repair Work Logged"
                            hasSearch -> "No Matching Requests"
                            else -> "No ${activeFilter?.label ?: "Matching"} Requests"
                        },
                        subtitle = when {
                            state.tickets.isEmpty() ->
                                "Log plumbing, electrical, elevator, or civil repair tickets with photo evidence and cost tracking."
                            hasSearch ->
                                "No maintenance requests match \"${searchQuery.trim()}\". Try another description keyword or clear your search."
                            else ->
                                "There are currently no requests in the ${activeFilter?.label ?: "selected"} state. Switch or clear the filter to view all requests."
                        },
                        actionLabel = when {
                            state.tickets.isEmpty() -> "Log First Repair"
                            hasSearch -> "Clear Search & Filters"
                            else -> "Show All Requests"
                        },
                        onAction = {
                            if (state.tickets.isEmpty()) {
                                onOpenLogRepair()
                            } else {
                                searchQuery = ""
                                selectedFilter = null
                            }
                        }
                    )
                }
            } else {
                items(filteredTickets, key = { it.id }) { ticket ->
                    RepairTicketCard(
                        ticket = ticket,
                        diagnostic = state.unscheduledDiagnostics[ticket.id],
                        onAutoSchedule = { onAutoScheduleTicket(ticket) },
                        onUpdateWorkLog = { progressTarget = ticket },
                        onDelete = { deleteTarget = ticket }
                    )
                }
            }
        }
    }

    progressTarget?.let { target ->
        UpdateRepairProgressDialog(
            ticket = target,
            onDismiss = { progressTarget = null },
            onConfirm = { status, actualCost, notes ->
                onUpdateProgress(target, status, actualCost, notes)
                progressTarget = null
            }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Repair Log?") },
            text = { Text("Delete repair ticket \"${target.title}\" for ${target.unitLocation}?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteTicket(target.id)
                    deleteTarget = null
                }) {
                    Text("Delete", color = StatusOverdueRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun RepairTicketCard(
    ticket: RepairTicket,
    diagnostic: String?,
    onAutoSchedule: () -> Unit,
    onUpdateWorkLog: () -> Unit,
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    MonoTag(text = ticket.unitLocation)
                    TradeBadge(trade = ticket.categoryEnum)
                }
                TicketStatusBadge(status = ticket.statusEnum)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ticket.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                PriorityBadge(priority = ticket.priorityEnum)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = ticket.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
            RequestProgressBar(status = ticket.statusEnum)

            if (!ticket.photoUri.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                AsyncImage(
                    model = Uri.parse(ticket.photoUri),
                    contentDescription = ticket.photoLabel ?: "Repair evidence photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else if (!ticket.photoLabel.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = ticket.photoLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Est. Cost",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(ticket.estimatedCost),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Actual Logged Cost",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(ticket.actualCost),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (ticket.actualCost > 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!ticket.assignedContractorName.isNullOrBlank() && ticket.scheduledDateEpochDay != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Engineering,
                            contentDescription = null,
                            tint = StatusScheduledBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${ticket.assignedContractorName} • ${formatEpochDay(ticket.scheduledDateEpochDay)} (${ticket.slotEnum?.windowText ?: "Slot"})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (!ticket.workNotes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Work Log: ${ticket.workNotes}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (!diagnostic.isNullOrBlank() && ticket.statusEnum == TicketStatus.LOGGED) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = diagnostic,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (ticket.statusEnum == TicketStatus.LOGGED) {
                    Button(
                        onClick = onAutoSchedule,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("auto_schedule_ticket_${ticket.id}")
                    ) {
                        Icon(Icons.Rounded.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Auto-Schedule")
                    }
                }
                if (ticket.statusEnum != TicketStatus.COMPLETED && ticket.statusEnum != TicketStatus.CANCELLED) {
                    OutlinedButton(
                        onClick = onUpdateWorkLog,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("update_repair_${ticket.id}")
                    ) {
                        Icon(Icons.Rounded.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Work", maxLines = 1)
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete repair ticket",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestStatusSectionHeader(
    totalCount: Int,
    pendingCount: Int,
    inProgressCount: Int,
    completedCount: Int,
    selectedFilter: RequestStatusFilter?,
    onSelectFilter: (RequestStatusFilter) -> Unit,
    onShowAll: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("request_status_section"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Request Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Track requests across Pending, In Progress, and Completed states",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = onShowAll,
                    label = { Text("All ($totalCount)") },
                    modifier = Modifier.testTag("request_status_filter_all")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RequestStatusStateTile(
                    label = "Pending",
                    count = pendingCount,
                    icon = Icons.Rounded.Schedule,
                    accentColor = StatusPendingAmber,
                    bgColor = StatusPendingBg,
                    selected = selectedFilter == RequestStatusFilter.PENDING,
                    onClick = { onSelectFilter(RequestStatusFilter.PENDING) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("request_status_filter_pending")
                )
                RequestStatusStateTile(
                    label = "In Progress",
                    count = inProgressCount,
                    icon = Icons.Rounded.Autorenew,
                    accentColor = StatusScheduledBlue,
                    bgColor = StatusScheduledBg,
                    selected = selectedFilter == RequestStatusFilter.IN_PROGRESS,
                    onClick = { onSelectFilter(RequestStatusFilter.IN_PROGRESS) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("request_status_filter_in_progress")
                )
                RequestStatusStateTile(
                    label = "Completed",
                    count = completedCount,
                    icon = Icons.Rounded.CheckCircle,
                    accentColor = StatusPaidGreen,
                    bgColor = StatusPaidBg,
                    selected = selectedFilter == RequestStatusFilter.COMPLETED,
                    onClick = { onSelectFilter(RequestStatusFilter.COMPLETED) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("request_status_filter_completed")
                )
            }
        }
    }
}

@Composable
private fun RequestStatusStateTile(
    label: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    bgColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (selected) bgColor else bgColor.copy(alpha = 0.45f),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) accentColor else accentColor.copy(alpha = 0.28f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = "$label indicator",
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun UpdateRepairProgressDialog(
    ticket: RepairTicket,
    onDismiss: () -> Unit,
    onConfirm: (TicketStatus, Double, String?) -> Unit
) {
    var selectedStatus by remember {
        mutableStateOf(
            if (ticket.statusEnum == TicketStatus.COMPLETED) TicketStatus.COMPLETED
            else TicketStatus.IN_PROGRESS
        )
    }
    var actualCost by remember {
        mutableStateOf(
            if (ticket.actualCost > 0.0) ticket.actualCost.toInt().toString()
            else ticket.estimatedCost.toInt().toString()
        )
    }
    var workNotes by remember { mutableStateOf(ticket.workNotes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Work Log") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${ticket.unitLocation} • ${ticket.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        TicketStatus.IN_PROGRESS,
                        TicketStatus.COMPLETED,
                        TicketStatus.CANCELLED
                    ).forEach { st ->
                        FilterChip(
                            selected = selectedStatus == st,
                            onClick = { selectedStatus = st },
                            label = { Text(st.label) }
                        )
                    }
                }
                OutlinedTextField(
                    value = actualCost,
                    onValueChange = { actualCost = it },
                    label = { Text("Actual Labor & Materials Cost") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = workNotes,
                    onValueChange = { workNotes = it },
                    label = { Text("Repair Work Notes") },
                    placeholder = { Text("Parts replaced, inspection findings...") },
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
                    onConfirm(selectedStatus, cost, workNotes.trim().takeIf { it.isNotEmpty() })
                }
            ) {
                Text("Save Log")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
