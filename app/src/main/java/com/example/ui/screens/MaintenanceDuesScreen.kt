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
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Payments
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.DueStatus
import com.example.data.model.MaintenanceDue
import com.example.data.model.PaymentMethod
import com.example.data.scheduling.AutoScheduler
import com.example.ui.SocietyDashboardState
import com.example.ui.components.DueStatusBadge
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.MonoTag
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatEpochDay
import com.example.ui.components.launchUpiPayment
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusPendingAmber

@Composable
fun MaintenanceDuesScreen(
    state: SocietyDashboardState,
    onOpenNewDue: () -> Unit,
    onRecordPayment: (MaintenanceDue, PaymentMethod, String, String?) -> Unit,
    onMarkOverdue: (MaintenanceDue, Double) -> Unit,
    onDeleteDue: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedFilter by remember { mutableStateOf<DueStatus?>(null) }
    var paymentTarget by remember { mutableStateOf<MaintenanceDue?>(null) }
    var overdueTarget by remember { mutableStateOf<MaintenanceDue?>(null) }
    var deleteTarget by remember { mutableStateOf<MaintenanceDue?>(null) }
    var receiptTarget by remember { mutableStateOf<MaintenanceDue?>(null) }
    val context = LocalContext.current

    val filteredDues = remember(state.dues, selectedFilter) {
        if (selectedFilter == null) state.dues
        else state.dues.filter { it.statusEnum == selectedFilter }
    }

    Column(modifier = modifier.fillMaxSize()) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp)
                .testTag("payment_collection_tabs")
        ) {
            SegmentedButton(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                modifier = Modifier.testTag("payment_tab_dues")
            ) {
                Text("Active Dues (${state.dues.size})", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            SegmentedButton(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                modifier = Modifier.testTag("payment_tab_history")
            ) {
                Text("Payment History (${state.dues.size})", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        if (selectedTab == 1) {
            PaymentHistorySection(
                dues = state.dues,
                onPayDue = { paymentTarget = it },
                onCreateDue = onOpenNewDue,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Filter Chips Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                label = { Text("All (${state.dues.size})") }
            )
            DueStatus.entries.forEach { status ->
                val count = state.dues.count { it.statusEnum == status }
                FilterChip(
                    selected = selectedFilter == status,
                    onClick = { selectedFilter = status },
                    label = { Text("${status.label} ($count)") }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Financial Collection Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Collection Rate: ${state.metrics.collectionRatePercent}%",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Collected ${formatCurrency(state.metrics.collectedAmount)} of ${formatCurrency(state.metrics.totalBilledAmount)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = onOpenNewDue,
                                modifier = Modifier.testTag("dues_header_new_bill_button"),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bill Unit")
                            }
                        }
                        LinearProgressIndicator(
                            progress = { state.metrics.collectionRatePercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp)),
                            color = StatusPaidGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Collected",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatCurrency(state.metrics.collectedAmount),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusPaidGreen
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Pending",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatCurrency(state.metrics.pendingAmount),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusPendingAmber
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Overdue",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatCurrency(state.metrics.overdueAmount),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusOverdueRed
                                )
                            }
                        }
                    }
                }
            }

            if (filteredDues.isEmpty()) {
                item {
                    EmptyStatePanel(
                        title = if (state.dues.isEmpty()) "No Maintenance Bills Yet" else "No Bills Match This Filter",
                        subtitle = if (state.dues.isEmpty()) {
                            "Create monthly maintenance assessments for flats and record resident payments with digital receipts."
                        } else {
                            "Switch filters above or create a new unit maintenance assessment."
                        },
                        actionLabel = "Create Maintenance Bill",
                        onAction = onOpenNewDue
                    )
                }
            } else {
                items(filteredDues, key = { it.id }) { due ->
                    MaintenanceDueCard(
                        due = due,
                        onCollect = { paymentTarget = due },
                        onMarkOverdue = { overdueTarget = due },
                        onDelete = { deleteTarget = due },
                        onViewReceipt = { receiptTarget = due },
                        onPayUpi = { launchUpiPayment(context, due, state.profile?.societyName ?: "Shafi Homes Arcade") }
                    )
                }
            }
        }
        }
    }

    paymentTarget?.let { target ->
        RecordPaymentDialog(
            due = target,
            onDismiss = { paymentTarget = null },
            onConfirm = { method, receipt, notes ->
                onRecordPayment(target, method, receipt, notes)
                paymentTarget = null
            }
        )
    }

    overdueTarget?.let { target ->
        MarkOverdueDialog(
            due = target,
            onDismiss = { overdueTarget = null },
            onConfirm = { lateFee ->
                onMarkOverdue(target, lateFee)
                overdueTarget = null
            }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Maintenance Bill?") },
            text = {
                Text("Remove ${target.billingPeriod} maintenance bill for ${target.unitNumber} (${formatCurrency(target.totalAmount)})?")
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteDue(target.id)
                    deleteTarget = null
                }) {
                    Text("Delete", color = StatusOverdueRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    receiptTarget?.let { target ->
        ReceiptDialog(
            due = target,
            societyName = state.profile?.societyName ?: "Shafi Homes Arcade",
            onDismiss = { receiptTarget = null }
        )
    }
}

@Composable
private fun MaintenanceDueCard(
    due: MaintenanceDue,
    onCollect: () -> Unit,
    onMarkOverdue: () -> Unit,
    onDelete: () -> Unit,
    onViewReceipt: () -> Unit,
    onPayUpi: () -> Unit
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    MonoTag(text = due.unitNumber)
                    Text(
                        text = due.billingPeriod,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                DueStatusBadge(status = due.statusEnum)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = due.residentName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Due ${formatEpochDay(due.dueDateEpochDay)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = formatCurrency(due.totalAmount),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = when (due.statusEnum) {
                        DueStatus.PAID -> StatusPaidGreen
                        DueStatus.OVERDUE -> StatusOverdueRed
                        DueStatus.PENDING -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(10.dp))

            // Fee Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FeeMiniColumn("Maintenance", due.baseAmount)
                FeeMiniColumn("Sinking", due.sinkingFund)
                FeeMiniColumn("Utilities", due.utilityAmount)
                if (due.lateFee > 0.0) {
                    FeeMiniColumn("Late Fee", due.lateFee, isAlert = true)
                }
            }

            if (!due.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = due.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (due.statusEnum == DueStatus.PAID) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onViewReceipt,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                            contentDescription = null,
                            tint = StatusPaidGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "View & Share Receipt",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete bill",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onPayUpi,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccountBalanceWallet,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pay via UPI", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onCollect,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Payments,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Record")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (due.statusEnum == DueStatus.PENDING) {
                            TextButton(
                                onClick = onMarkOverdue,
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("Mark Overdue", color = StatusPendingAmber, style = MaterialTheme.typography.labelMedium)
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete bill",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeeMiniColumn(label: String, amount: Double, isAlert: Boolean = false) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.labelLarge,
            color = if (isAlert) StatusOverdueRed else MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMaintenanceDueSheet(
    defaultUnit: String,
    defaultResident: String,
    defaultMonthlyFee: Double,
    onDismiss: () -> Unit,
    onSubmit: (
        unitNumber: String,
        residentName: String,
        billingPeriod: String,
        baseAmount: Double,
        sinkingFund: Double,
        utilityAmount: Double,
        dueDateEpochDay: Long,
        notes: String?
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var unitNumber by remember { mutableStateOf(defaultUnit) }
    var residentName by remember { mutableStateOf(defaultResident) }
    var billingPeriod by remember { mutableStateOf("Nov 2026") }
    var baseAmount by remember { mutableStateOf(defaultMonthlyFee.toInt().toString()) }
    var sinkingFund by remember { mutableStateOf("500") }
    var utilityAmount by remember { mutableStateOf("450") }
    var dueInDays by remember { mutableStateOf("10") }
    var notes by remember { mutableStateOf("") }
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
            Text(
                text = "New Maintenance Bill",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Issue a monthly society assessment with sinking fund & utility breakdown.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = unitNumber,
                    onValueChange = { unitNumber = it },
                    label = { Text("Unit / Flat") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bill_unit_input")
                )
                OutlinedTextField(
                    value = billingPeriod,
                    onValueChange = { billingPeriod = it },
                    label = { Text("Billing Period") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = residentName,
                onValueChange = { residentName = it },
                label = { Text("Resident Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = baseAmount,
                    onValueChange = { baseAmount = it },
                    label = { Text("Base Fee") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = sinkingFund,
                    onValueChange = { sinkingFund = it },
                    label = { Text("Sinking") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = utilityAmount,
                    onValueChange = { utilityAmount = it },
                    label = { Text("Utilities") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = dueInDays,
                onValueChange = { dueInDays = it },
                label = { Text("Due in (days from today)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Billing Notes (optional)") },
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            val previewTotal = (baseAmount.toDoubleOrNull() ?: 0.0) +
                (sinkingFund.toDoubleOrNull() ?: 0.0) +
                (utilityAmount.toDoubleOrNull() ?: 0.0)

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Assessment", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = formatCurrency(previewTotal),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (validationError != null) {
                Text(
                    text = validationError ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusOverdueRed
                )
            }

            Button(
                onClick = {
                    val base = baseAmount.toDoubleOrNull()
                    val sink = sinkingFund.toDoubleOrNull() ?: 0.0
                    val util = utilityAmount.toDoubleOrNull() ?: 0.0
                    val days = dueInDays.toLongOrNull() ?: 10L
                    when {
                        unitNumber.trim().isEmpty() -> validationError = "Enter a flat or unit number."
                        residentName.trim().length < 2 -> validationError = "Enter resident name (at least 2 chars)."
                        billingPeriod.trim().length < 3 -> validationError = "Enter billing period (e.g. Nov 2026)."
                        base == null || base < 0.0 -> validationError = "Enter a valid base maintenance amount."
                        else -> {
                            val epochDay = AutoScheduler.currentEpochDay() + days.coerceIn(0L, 120L)
                            onSubmit(
                                unitNumber.trim(),
                                residentName.trim(),
                                billingPeriod.trim(),
                                base,
                                sink.coerceAtLeast(0.0),
                                util.coerceAtLeast(0.0),
                                epochDay,
                                notes.trim().takeIf { it.isNotEmpty() }
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_new_bill_button")
            ) {
                Text("Issue Maintenance Bill")
            }
        }
    }
}

@Composable
private fun RecordPaymentDialog(
    due: MaintenanceDue,
    onDismiss: () -> Unit,
    onConfirm: (PaymentMethod, String, String?) -> Unit
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.UPI) }
    var receiptNumber by remember {
        mutableStateOf("RCP-${due.unitNumber.filter { it.isLetterOrDigit() }.take(6)}-${(1000..9999).random()}")
    }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Collect ${formatCurrency(due.totalAmount)}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${due.unitNumber} • ${due.billingPeriod} (${due.residentName})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("Payment Mode", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMethod.entries.forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = { Text(method.label) }
                        )
                    }
                }
                OutlinedTextField(
                    value = receiptNumber,
                    onValueChange = { receiptNumber = it },
                    label = { Text("Receipt Reference") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Remarks (optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (receiptNumber.trim().isNotEmpty()) {
                        onConfirm(selectedMethod, receiptNumber.trim(), notes.trim().takeIf { it.isNotEmpty() })
                    }
                }
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Confirm Paid")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun MarkOverdueDialog(
    due: MaintenanceDue,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var lateFeeInput by remember { mutableStateOf("250") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mark ${due.unitNumber} Overdue") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Optionally add a late payment penalty to ${due.billingPeriod} dues.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = lateFeeInput,
                    onValueChange = { lateFeeInput = it },
                    label = { Text("Additional Late Fee") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fee = (lateFeeInput.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
                    onConfirm(fee)
                }
            ) {
                Text("Mark Overdue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
