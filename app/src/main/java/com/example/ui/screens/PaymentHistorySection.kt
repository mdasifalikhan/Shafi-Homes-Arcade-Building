package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.DueStatus
import com.example.data.model.MaintenanceDue
import com.example.ui.components.DueStatusBadge
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.MonoTag
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatEpochDate
import com.example.ui.components.formatEpochDateTime
import com.example.ui.components.formatEpochDay
import com.example.ui.theme.StatusOverdueBg
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.StatusPendingBg

@Composable
fun PaymentHistorySection(
    dues: List<MaintenanceDue>,
    onPayDue: (MaintenanceDue) -> Unit,
    onCreateDue: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStatusName by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedStatus = selectedStatusName?.let { raw ->
        DueStatus.entries.firstOrNull { it.name == raw }
    }
    var detailTarget by remember { mutableStateOf<MaintenanceDue?>(null) }

    val sortedHistory = remember(dues) {
        dues.sortedWith(
            compareByDescending<MaintenanceDue> { it.paidAtEpochMillis ?: it.createdAtMillis }
                .thenByDescending { it.dueDateEpochDay }
        )
    }

    val filteredHistory = remember(sortedHistory, selectedStatus) {
        if (selectedStatus == null) sortedHistory
        else sortedHistory.filter { it.statusEnum == selectedStatus }
    }

    val paidCount = remember(sortedHistory) {
        sortedHistory.count { it.statusEnum == DueStatus.PAID }
    }
    val pendingCount = remember(sortedHistory) {
        sortedHistory.count { it.statusEnum == DueStatus.PENDING }
    }
    val overdueCount = remember(sortedHistory) {
        sortedHistory.count { it.statusEnum == DueStatus.OVERDUE }
    }
    val paidTotal = remember(sortedHistory) {
        sortedHistory.filter { it.statusEnum == DueStatus.PAID }.sumOf { it.totalAmount }
    }
    val outstandingTotal = remember(sortedHistory) {
        sortedHistory.filter { it.statusEnum != DueStatus.PAID }.sumOf { it.totalAmount }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("history_filter_bar"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.FilterList,
                contentDescription = "Filter payment history by transaction status",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            FilterChip(
                selected = selectedStatus == null,
                onClick = { selectedStatusName = null },
                label = { Text("All (${sortedHistory.size})") },
                modifier = Modifier.testTag("history_filter_chip_all")
            )
            FilterChip(
                selected = selectedStatus == DueStatus.PAID,
                onClick = {
                    selectedStatusName = if (selectedStatus == DueStatus.PAID) null else DueStatus.PAID.name
                },
                label = { Text("Paid ($paidCount)") },
                modifier = Modifier.testTag("history_filter_chip_paid")
            )
            FilterChip(
                selected = selectedStatus == DueStatus.PENDING,
                onClick = {
                    selectedStatusName = if (selectedStatus == DueStatus.PENDING) null else DueStatus.PENDING.name
                },
                label = { Text("Pending ($pendingCount)") },
                modifier = Modifier.testTag("history_filter_chip_pending")
            )
            FilterChip(
                selected = selectedStatus == DueStatus.OVERDUE,
                onClick = {
                    selectedStatusName = if (selectedStatus == DueStatus.OVERDUE) null else DueStatus.OVERDUE.name
                },
                label = { Text("Overdue ($overdueCount)") },
                modifier = Modifier.testTag("history_filter_chip_overdue")
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("payment_history_list"),
            contentPadding = PaddingValues(16.dp, 6.dp, 16.dp, 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PaymentHistorySummaryCard(
                    totalTransactions = sortedHistory.size,
                    paidCount = paidCount,
                    paidTotal = paidTotal,
                    outstandingTotal = outstandingTotal
                )
            }

            if (filteredHistory.isEmpty()) {
                item {
                    EmptyStatePanel(
                        title = if (sortedHistory.isEmpty()) {
                            "No Payment History Yet"
                        } else {
                            "No ${selectedStatus?.label ?: "Matching"} Transactions"
                        },
                        subtitle = if (sortedHistory.isEmpty()) {
                            "Past maintenance dues, payment receipts, and outstanding ledger entries will appear here once billed."
                        } else {
                            "Switch transaction status filters above or show all past maintenance dues."
                        },
                        actionLabel = if (sortedHistory.isEmpty()) {
                            "Create Maintenance Bill"
                        } else {
                            "Show All History"
                        },
                        onAction = {
                            if (sortedHistory.isEmpty()) {
                                onCreateDue()
                            } else {
                                selectedStatusName = null
                            }
                        }
                    )
                }
            } else {
                items(filteredHistory, key = { it.id }) { due ->
                    PaymentHistoryItemCard(
                        due = due,
                        onInspect = { detailTarget = due },
                        onPayNow = { onPayDue(due) }
                    )
                }
            }
        }
    }

    detailTarget?.let { target ->
        PaymentTransactionDetailDialog(
            due = target,
            onDismiss = { detailTarget = null },
            onPayNow = {
                detailTarget = null
                onPayDue(target)
            }
        )
    }
}

@Composable
private fun PaymentHistorySummaryCard(
    totalTransactions: Int,
    paidCount: Int,
    paidTotal: Double,
    outstandingTotal: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("payment_history_summary_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Payment History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Past maintenance dues and transaction status",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Settled",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$paidCount of $totalTransactions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Paid",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(paidTotal),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = StatusPaidGreen
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Unpaid Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(outstandingTotal),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (outstandingTotal > 0.0) StatusOverdueRed else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentHistoryItemCard(
    due: MaintenanceDue,
    onInspect: () -> Unit,
    onPayNow: () -> Unit
) {
    val (statusIcon, iconBg, iconTint, amountColor) = when (due.statusEnum) {
        DueStatus.PAID -> HistoryVisualStyle(
            icon = Icons.Rounded.CheckCircle,
            bg = StatusPaidBg,
            tint = StatusPaidGreen,
            amountColor = StatusPaidGreen
        )
        DueStatus.OVERDUE -> HistoryVisualStyle(
            icon = Icons.Rounded.ErrorOutline,
            bg = StatusOverdueBg,
            tint = StatusOverdueRed,
            amountColor = StatusOverdueRed
        )
        DueStatus.PENDING -> HistoryVisualStyle(
            icon = Icons.Rounded.Schedule,
            bg = StatusPendingBg,
            tint = StatusPendingAmber,
            amountColor = MaterialTheme.colorScheme.onSurface
        )
    }

    val transactionSubtitle = when (due.statusEnum) {
        DueStatus.PAID -> {
            val method = due.paymentMethodEnum?.label ?: "Verified Payment"
            val dateText = due.paidAtEpochMillis?.let { formatEpochDate(it) }
                ?: formatEpochDay(due.dueDateEpochDay)
            "Paid via $method • $dateText"
        }
        DueStatus.OVERDUE -> {
            val feeNote = if (due.lateFee > 0.0) " • Late fee ${formatCurrency(due.lateFee)}" else ""
            "Due ${formatEpochDay(due.dueDateEpochDay)}$feeNote"
        }
        DueStatus.PENDING -> "Awaiting payment • Due ${formatEpochDay(due.dueDateEpochDay)}"
    }

    Card(
        onClick = onInspect,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_item_${due.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = "${due.statusEnum.label} transaction",
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MonoTag(text = due.unitNumber)
                        Text(
                            text = due.billingPeriod,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = due.residentName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(due.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    DueStatusBadge(status = due.statusEnum)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transactionSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (due.statusEnum == DueStatus.PAID && !due.receiptNumber.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                                contentDescription = null,
                                tint = StatusPaidGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            MonoTag(text = due.receiptNumber)
                        }
                    }
                }

                if (due.statusEnum == DueStatus.PAID) {
                    OutlinedButton(
                        onClick = onInspect,
                        modifier = Modifier.height(48.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Receipt")
                    }
                } else {
                    Button(
                        onClick = onPayNow,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("history_pay_${due.id}"),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pay")
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentTransactionDetailDialog(
    due: MaintenanceDue,
    onDismiss: () -> Unit,
    onPayNow: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${due.billingPeriod} • ${due.unitNumber}")
                DueStatusBadge(status = due.statusEnum)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Resident: ${due.residentName}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (due.statusEnum == DueStatus.PAID) {
                    Surface(
                        color = StatusPaidBg,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Transaction Completed",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = StatusPaidGreen
                            )
                            Text(
                                text = "Method: ${due.paymentMethodEnum?.label ?: "Verified"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusPaidGreen
                            )
                            if (!due.receiptNumber.isNullOrBlank()) {
                                Text(
                                    text = "Receipt: ${due.receiptNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusPaidGreen
                                )
                            }
                            due.paidAtEpochMillis?.let { paidAt ->
                                Text(
                                    text = "Paid On: ${formatEpochDateTime(paidAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusPaidGreen
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Due Date: ${formatEpochDay(due.dueDateEpochDay)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                DetailAmountRow("Base Maintenance", due.baseAmount)
                DetailAmountRow("Sinking Fund", due.sinkingFund)
                DetailAmountRow("Utilities", due.utilityAmount)
                if (due.lateFee > 0.0) {
                    DetailAmountRow("Late Fee", due.lateFee, isAlert = true)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Assessment",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatCurrency(due.totalAmount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (!due.notes.isNullOrBlank()) {
                    Text(
                        text = "Notes: ${due.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            if (due.statusEnum != DueStatus.PAID) {
                Button(onClick = onPayNow) {
                    Text("Record Payment")
                }
            } else {
                Button(onClick = onDismiss) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            if (due.statusEnum != DueStatus.PAID) {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
private fun DetailAmountRow(label: String, amount: Double, isAlert: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isAlert) StatusOverdueRed else MaterialTheme.colorScheme.onSurface
        )
    }
}

private data class HistoryVisualStyle(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val bg: androidx.compose.ui.graphics.Color,
    val tint: androidx.compose.ui.graphics.Color,
    val amountColor: androidx.compose.ui.graphics.Color
)
