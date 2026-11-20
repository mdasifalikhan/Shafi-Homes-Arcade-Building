package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VisitorPass
import com.example.data.model.VisitorPurpose
import com.example.data.model.VisitorStatus
import com.example.ui.SocietyDashboardState
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.MonoTag
import com.example.ui.components.formatEpochDateTime
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusPendingAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorPassScreen(
    state: SocietyDashboardState,
    onCreatePass: (visitorName: String, phone: String?, purpose: VisitorPurpose, unitNumber: String, vehicleNumber: String?) -> Unit,
    onUpdateStatus: (passId: String, nextStatus: VisitorStatus) -> Unit,
    onDeletePass: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedStatus by remember { mutableStateOf<VisitorStatus?>(null) }
    var showNewPassSheet by remember { mutableStateOf(false) }

    val filteredPasses = remember(state.visitors, selectedStatus) {
        if (selectedStatus == null) state.visitors
        else state.visitors.filter { it.statusEnum == selectedStatus }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewPassSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pre-Approve Entry", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatus == null,
                    onClick = { selectedStatus = null },
                    label = { Text("All (${state.visitors.size})") }
                )
                VisitorStatus.entries.forEach { status ->
                    val count = state.visitors.count { it.statusEnum == status }
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        label = { Text("${status.label} ($count)") }
                    )
                }
            }

            if (filteredPasses.isEmpty()) {
                EmptyStatePanel(
                    title = "No Visitor Passes",
                    subtitle = "No visitors or deliveries currently logged. Tap 'Pre-Approve Entry' to issue a secure 4-digit gate passcode for guests, deliveries, or service staff.",
                    actionLabel = "Pre-Approve Guest",
                    onAction = { showNewPassSheet = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 92.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPasses, key = { it.id }) { pass ->
                        VisitorPassCard(
                            pass = pass,
                            onShare = { shareVisitorPass(context, pass, state.profile?.societyName ?: "Shafi Homes Arcade") },
                            onCheckIn = { onUpdateStatus(pass.id, VisitorStatus.CHECKED_IN) },
                            onCheckOut = { onUpdateStatus(pass.id, VisitorStatus.CHECKED_OUT) },
                            onDelete = { onDeletePass(pass.id) }
                        )
                    }
                }
            }
        }
    }

    if (showNewPassSheet) {
        NewVisitorPassSheet(
            defaultUnit = state.profile?.unitLabel ?: "Tower A - 302",
            onDismiss = { showNewPassSheet = false },
            onSubmit = { name, phone, purpose, unit, vehicle ->
                onCreatePass(name, phone, purpose, unit, vehicle)
                showNewPassSheet = false
            }
        )
    }
}

@Composable
private fun VisitorPassCard(
    pass: VisitorPass,
    onShare: () -> Unit,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (pass.statusEnum) {
        VisitorStatus.EXPECTED -> StatusPendingAmber
        VisitorStatus.CHECKED_IN -> StatusPaidGreen
        VisitorStatus.CHECKED_OUT -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = pass.statusEnum.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    MonoTag(text = pass.purposeEnum.label)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete Pass",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pass.visitorName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Visiting: ${pass.unitNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!pass.phone.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = pass.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (!pass.vehicleNumber.isNullOrBlank()) {
                        Text(
                            text = "Vehicle: ${pass.vehicleNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Security Passcode Badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "GATE CODE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = pass.passcode,
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onShare,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Code")
                }

                when (pass.statusEnum) {
                    VisitorStatus.EXPECTED -> {
                        Button(
                            onClick = onCheckIn,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark Entry")
                        }
                    }
                    VisitorStatus.CHECKED_IN -> {
                        OutlinedButton(
                            onClick = onCheckOut,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark Exit")
                        }
                    }
                    VisitorStatus.CHECKED_OUT -> {
                        Text(
                            text = "Exited at ${pass.checkOutEpochMillis?.let { formatEpochDateTime(it) } ?: "Gate"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewVisitorPassSheet(
    defaultUnit: String,
    onDismiss: () -> Unit,
    onSubmit: (String, String?, VisitorPurpose, String, String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var visitorName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf(VisitorPurpose.GUEST) }
    var unitNumber by remember { mutableStateOf(defaultUnit) }
    var vehicleNumber by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Pre-Approve Visitor / Delivery", style = MaterialTheme.typography.titleLarge)
            }

            OutlinedTextField(
                value = visitorName,
                onValueChange = { visitorName = it },
                label = { Text("Visitor / Driver / Company Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Entry Purpose", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VisitorPurpose.entries.forEach { p ->
                    FilterChip(
                        selected = purpose == p,
                        onClick = { purpose = p },
                        label = { Text(p.label) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = unitNumber,
                    onValueChange = { unitNumber = it },
                    label = { Text("Destination Flat *") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = vehicleNumber,
                onValueChange = { vehicleNumber = it },
                label = { Text("Vehicle Number (optional, e.g. KA01AB1234)") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
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
                    when {
                        visitorName.trim().length < 2 -> validationError = "Enter visitor name (at least 2 chars)."
                        unitNumber.trim().isEmpty() -> validationError = "Specify the destination flat/unit."
                        else -> {
                            onSubmit(
                                visitorName.trim(),
                                phone.trim().takeIf { it.isNotEmpty() },
                                purpose,
                                unitNumber.trim(),
                                vehicleNumber.trim().takeIf { it.isNotEmpty() }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Generate Entry Passcode", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun shareVisitorPass(context: Context, pass: VisitorPass, societyName: String) {
    val message = """
        🏢 *$societyName - VISITOR GATE PASS*
        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        Visitor: ${pass.visitorName}
        Purpose: ${pass.purposeEnum.label}
        Destination: Flat ${pass.unitNumber}
        
        🔑 *GATE SECURITY CODE: ${pass.passcode}*
        
        Show this 4-digit code to the security guard at the gate for fast verified entry.
        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Gate Code via WhatsApp / SMS"))
}
