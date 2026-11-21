package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DomesticHelper
import com.example.data.model.VisitorPass
import com.example.data.model.VisitorPurpose
import com.example.data.model.VisitorStatus
import com.example.ui.SocietyDashboardState
import com.example.util.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityTerminalScreen(
    state: SocietyDashboardState,
    onUpdateVisitorStatus: (passId: String, nextStatus: VisitorStatus) -> Unit,
    onCreateWalkInPass: (name: String, phone: String?, purpose: VisitorPurpose, unit: String, vehicle: String?) -> Unit,
    onToggleHelperStatus: (helperId: String, nextInPremises: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pinInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showWalkInSheet by remember { mutableStateOf(false) }

    // Find pass matching 4-digit PIN
    val matchedPass = remember(pinInput, state.visitors) {
        if (pinInput.length == 4) {
            state.visitors.firstOrNull { it.passcode == pinInput }
        } else {
            null
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            Button(
                onClick = { showWalkInSheet = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Walk-In Entry", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Guard Terminal Status Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Main Gate Security Terminal",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF22C55E)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Gate #1 Entrance • Shafi Homes Arcade",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 4-Digit Passcode Input & Keypad Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "GUEST 4-DIGIT PIN VALIDATOR",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Visual PIN Boxes
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until 4) {
                                val digit = pinInput.getOrNull(i)?.toString() ?: ""
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (digit.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .border(
                                            width = if (digit.isNotEmpty()) 2.dp else 1.dp,
                                            color = if (digit.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = digit,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (digit.isNotEmpty()) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Numeric Keypad Grid
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val rows = listOf(
                                listOf("1", "2", "3"),
                                listOf("4", "5", "6"),
                                listOf("7", "8", "9"),
                                listOf("CLEAR", "0", "DEL")
                            )

                            for (row in rows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    for (key in row) {
                                        val isSpecial = key == "CLEAR" || key == "DEL"
                                        Button(
                                            onClick = {
                                                when (key) {
                                                    "CLEAR" -> pinInput = ""
                                                    "DEL" -> if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                                                    else -> if (pinInput.length < 4) pinInput += key
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSpecial) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                                contentColor = if (isSpecial) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                            ),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Text(
                                                text = key,
                                                fontSize = if (isSpecial) 12.sp else 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Validation Result Card
                        AnimatedVisibility(
                            visible = pinInput.length == 4,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))

                            if (matchedPass != null) {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                    border = BorderStroke(1.5.dp, Color(0xFF22C55E)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Rounded.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color(0xFF16A34A)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "VALID PASS VERIFIED",
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF16A34A),
                                                    fontSize = 13.sp
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = when (matchedPass.statusEnum) {
                                                    VisitorStatus.EXPECTED -> Color(0xFFFEF3C7)
                                                    VisitorStatus.CHECKED_IN -> Color(0xFFDCFCE7)
                                                    VisitorStatus.CHECKED_OUT -> Color(0xFFF1F5F9)
                                                }
                                            ) {
                                                Text(
                                                    text = matchedPass.statusEnum.label,
                                                    color = when (matchedPass.statusEnum) {
                                                        VisitorStatus.EXPECTED -> Color(0xFF92400E)
                                                        VisitorStatus.CHECKED_IN -> Color(0xFF166534)
                                                        VisitorStatus.CHECKED_OUT -> Color(0xFF475569)
                                                    },
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = matchedPass.visitorName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Flat Destination: ${matchedPass.unitNumber}",
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 14.sp
                                        )

                                        Text(
                                            text = "Purpose: ${matchedPass.purposeEnum.label}${if (!matchedPass.vehicleNumber.isNullOrBlank()) " • Vehicle: ${matchedPass.vehicleNumber}" else ""}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Action buttons
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (matchedPass.statusEnum != VisitorStatus.CHECKED_IN) {
                                                Button(
                                                    onClick = {
                                                        onUpdateVisitorStatus(matchedPass.id, VisitorStatus.CHECKED_IN)
                                                        NotificationHelper.showVisitorArrivalNotification(
                                                            context = context,
                                                            visitorName = matchedPass.visitorName,
                                                            purpose = matchedPass.purposeEnum.label,
                                                            unitNumber = matchedPass.unitNumber,
                                                            passcode = matchedPass.passcode
                                                        )
                                                        pinInput = ""
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Icon(Icons.AutoMirrored.Rounded.Login, contentDescription = null)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Grant Entry", fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Button(
                                                    onClick = {
                                                        onUpdateVisitorStatus(matchedPass.id, VisitorStatus.CHECKED_OUT)
                                                        pinInput = ""
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Mark Departed", fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            if (!matchedPass.phone.isNullOrBlank()) {
                                                OutlinedButton(
                                                    onClick = {
                                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                                            data = Uri.parse("tel:${matchedPass.phone}")
                                                        }
                                                        context.startActivity(intent)
                                                    },
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Icon(Icons.Rounded.Call, contentDescription = "Call Visitor")
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                    border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Warning,
                                            contentDescription = null,
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "PIN '$pinInput' Not Found",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF991B1B)
                                        )
                                        Text(
                                            text = "No pre-approved pass matches this PIN. Verify with resident or log a walk-in entry.",
                                            fontSize = 12.sp,
                                            color = Color(0xFF7F1D1D),
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        OutlinedButton(
                                            onClick = { showWalkInSheet = true },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Log as Walk-In Guest")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Live Gate Roster Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            val count = state.visitors.count { it.statusEnum == VisitorStatus.EXPECTED }
                            Text("Expected ($count)")
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            val count = state.visitors.count { it.statusEnum == VisitorStatus.CHECKED_IN }
                            Text("Inside ($count)")
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            val count = state.helpers.count { it.isInPremises }
                            Text("Staff Inside ($count)")
                        }
                    )
                }
            }

            // Roster List based on tab
            when (selectedTab) {
                0 -> {
                    val expectedList = state.visitors.filter { it.statusEnum == VisitorStatus.EXPECTED }
                    if (expectedList.isEmpty()) {
                        item {
                            EmptyRosterState("No expected visitors pending arrival.")
                        }
                    } else {
                        items(expectedList, key = { it.id }) { pass ->
                            GateVisitorCard(
                                pass = pass,
                                onCheckIn = {
                                    onUpdateVisitorStatus(pass.id, VisitorStatus.CHECKED_IN)
                                    NotificationHelper.showVisitorArrivalNotification(
                                        context = context,
                                        visitorName = pass.visitorName,
                                        purpose = pass.purposeEnum.label,
                                        unitNumber = pass.unitNumber,
                                        passcode = pass.passcode
                                    )
                                },
                                onCheckOut = {
                                    onUpdateVisitorStatus(pass.id, VisitorStatus.CHECKED_OUT)
                                }
                            )
                        }
                    }
                }
                1 -> {
                    val insideList = state.visitors.filter { it.statusEnum == VisitorStatus.CHECKED_IN }
                    if (insideList.isEmpty()) {
                        item {
                            EmptyRosterState("No visitors currently inside building.")
                        }
                    } else {
                        items(insideList, key = { it.id }) { pass ->
                            GateVisitorCard(
                                pass = pass,
                                onCheckIn = {},
                                onCheckOut = {
                                    onUpdateVisitorStatus(pass.id, VisitorStatus.CHECKED_OUT)
                                }
                            )
                        }
                    }
                }
                2 -> {
                    val insideStaff = state.helpers.filter { it.isInPremises }
                    if (insideStaff.isEmpty()) {
                        item {
                            EmptyRosterState("No domestic staff currently checked in.")
                        }
                    } else {
                        items(insideStaff, key = { it.id }) { helper ->
                            GateStaffCard(
                                helper = helper,
                                onToggle = { onToggleHelperStatus(helper.id, false) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showWalkInSheet) {
        WalkInPassSheet(
            onDismiss = { showWalkInSheet = false },
            onSubmit = { name, phone, purpose, unit, vehicle ->
                onCreateWalkInPass(name, phone, purpose, unit, vehicle)
                showWalkInSheet = false
            }
        )
    }
}

@Composable
private fun GateVisitorCard(
    pass: VisitorPass,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (pass.purposeEnum) {
                            VisitorPurpose.DELIVERY -> Color(0xFFFEF3C7)
                            VisitorPurpose.CAB -> Color(0xFFEFF6FF)
                            VisitorPurpose.SERVICE -> Color(0xFFF3E8FF)
                            VisitorPurpose.GUEST -> Color(0xFFDCFCE7)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (pass.purposeEnum) {
                        VisitorPurpose.DELIVERY -> Icons.Rounded.LocalShipping
                        VisitorPurpose.CAB -> Icons.Rounded.DirectionsCar
                        VisitorPurpose.SERVICE -> Icons.Rounded.Security
                        VisitorPurpose.GUEST -> Icons.Rounded.Person
                    },
                    contentDescription = null,
                    tint = when (pass.purposeEnum) {
                        VisitorPurpose.DELIVERY -> Color(0xFFD97706)
                        VisitorPurpose.CAB -> Color(0xFF2563EB)
                        VisitorPurpose.SERVICE -> Color(0xFF7C3AED)
                        VisitorPurpose.GUEST -> Color(0xFF16A34A)
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pass.visitorName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0F172A)
                    ) {
                        Text(
                            text = "PIN: ${pass.passcode}",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Flat ${pass.unitNumber} • ${pass.purposeEnum.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!pass.vehicleNumber.isNullOrBlank()) {
                    Text(
                        text = "Vehicle: ${pass.vehicleNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (pass.statusEnum == VisitorStatus.EXPECTED) {
                Button(
                    onClick = onCheckIn,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Check In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else if (pass.statusEnum == VisitorStatus.CHECKED_IN) {
                OutlinedButton(
                    onClick = onCheckOut,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Depart", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun GateStaffCard(
    helper: DomesticHelper,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCFCE7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.DirectionsWalk,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = helper.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${helper.serviceType} • Flats: ${helper.assignedUnits}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = onToggle,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Check Out", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmptyRosterState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalkInPassSheet(
    onDismiss: () -> Unit,
    onSubmit: (name: String, phone: String?, purpose: VisitorPurpose, unit: String, vehicle: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var unitNumber by remember { mutableStateOf("Tower A - 101") }
    var vehicle by remember { mutableStateOf("") }
    var selectedPurpose by remember { mutableStateOf(VisitorPurpose.DELIVERY) }
    var error by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Log Walk-In Visitor at Gate",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Visitor / Delivery Partner Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = unitNumber,
                onValueChange = { unitNumber = it },
                label = { Text("Destination Flat Number *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Purpose of Entry", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VisitorPurpose.entries.forEach { p ->
                    FilterChip(
                        selected = selectedPurpose == p,
                        onClick = { selectedPurpose = p },
                        label = { Text(p.name, fontSize = 11.sp) }
                    )
                }
            }

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            OutlinedTextField(
                value = vehicle,
                onValueChange = { vehicle = it },
                label = { Text("Vehicle Number (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (error != null) {
                Text(error ?: "", color = Color(0xFFDC2626), fontSize = 12.sp)
            }

            Button(
                onClick = {
                    if (name.trim().isBlank() || unitNumber.trim().isBlank()) {
                        error = "Please enter visitor name and destination flat."
                    } else {
                        onSubmit(name.trim(), phone.trim().ifBlank { null }, selectedPurpose, unitNumber.trim(), vehicle.trim().ifBlank { null })
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Admit & Check In Visitor", fontWeight = FontWeight.Bold)
            }
        }
    }
}
