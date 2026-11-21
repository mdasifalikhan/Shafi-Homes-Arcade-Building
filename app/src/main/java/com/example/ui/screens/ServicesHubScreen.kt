package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.Festival
import androidx.compose.material.icons.rounded.HowToVote
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SocietyDashboardState

@Composable
fun ServicesHubScreen(
    state: SocietyDashboardState,
    onNavigateToAmenities: () -> Unit,
    onNavigateToEmergencySos: () -> Unit,
    onNavigateToDirectory: () -> Unit,
    onNavigateToPolls: () -> Unit,
    onNavigateToHelpers: () -> Unit,
    onNavigateToContractors: () -> Unit,
    onNavigateToVisitorPass: () -> Unit,
    onNavigateToSecurityTerminal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeBookings = state.amenityBookings.count { it.status == "CONFIRMED" }
    val helpersInPremises = state.helpers.count { it.isInPremises }
    val activePollsCount = state.polls.size
    val directoryCount = state.directory.size

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Emergency Alert Bar
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF4444)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToEmergencySos() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = "Emergency SOS",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Emergency & Security Hub",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B),
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFDC2626)
                            ) {
                                Text(
                                    text = "24/7 LIVE",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "1-Tap Gate Intercom, 108 Ambulance, 112 Police, 101 Fire & Lift Rescue",
                            color = Color(0xFF7F1D1D),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Open SOS",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }

        // Section Title: Resident Living Suites
        item {
            Column {
                Text(
                    text = "Resident Services & Amenities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Explore amenities, connect with neighbors, and vote in society polls",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Feature 1: Amenity Booking
        item {
            HubFeatureCard(
                title = "Amenity & Facility Booking",
                subtitle = "Reserve Community Hall, Rooftop BBQ Terrace, Fitness Gym & Guest Parking",
                badgeText = if (activeBookings > 0) "$activeBookings Active Bookings" else "Available to Book",
                icon = Icons.Rounded.Festival,
                iconBackgroundColor = Color(0xFF4F46E5),
                badgeBackgroundColor = Color(0xFFEEF2FF),
                badgeTextColor = Color(0xFF4338CA),
                onClick = onNavigateToAmenities
            )
        }

        // Feature 2: Society Directory
        item {
            HubFeatureCard(
                title = "Resident & Committee Directory",
                subtitle = "Searchable society phonebook with 1-tap Phone and WhatsApp calling",
                badgeText = "$directoryCount Members Listed",
                icon = Icons.Rounded.People,
                iconBackgroundColor = Color(0xFF0D9488),
                badgeBackgroundColor = Color(0xFFF0FDFA),
                badgeTextColor = Color(0xFF0F766E),
                onClick = onNavigateToDirectory
            )
        }

        // Feature 3: Society Polls & Voting
        item {
            HubFeatureCard(
                title = "Society Polls & Community Voting",
                subtitle = "Participate in society decisions, AGM resolutions, and community initiatives",
                badgeText = "$activePollsCount Active Polls",
                icon = Icons.Rounded.HowToVote,
                iconBackgroundColor = Color(0xFFD97706),
                badgeBackgroundColor = Color(0xFFFFFBEB),
                badgeTextColor = Color(0xFFB45309),
                onClick = onNavigateToPolls
            )
        }

        // Feature 4: Domestic Staff & Helper Tracker
        item {
            HubFeatureCard(
                title = "Domestic Staff & Helper Tracker",
                subtitle = "Real-time in-premises tracking for maids, cooks, drivers & cleaners",
                badgeText = "$helpersInPremises Inside Premises",
                icon = Icons.Rounded.CleaningServices,
                iconBackgroundColor = Color(0xFF059669),
                badgeBackgroundColor = Color(0xFFECFDF5),
                badgeTextColor = Color(0xFF047857),
                onClick = onNavigateToHelpers
            )
        }

        // Section Title: Operations & Infrastructure
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Text(
                    text = "Operations & Security",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Gate security, contractor schedules, and visitor management",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Feature 5: Contractor Dashboard & Schedule
        item {
            HubFeatureCard(
                title = "Contractor & Repair Schedule",
                subtitle = "Auto-scheduled plumbing, electrical, carpentry, and elevator visits",
                badgeText = "${state.contractors.size} Contractors Registered",
                icon = Icons.Rounded.Engineering,
                iconBackgroundColor = Color(0xFF2563EB),
                badgeBackgroundColor = Color(0xFFEFF6FF),
                badgeTextColor = Color(0xFF1D4ED8),
                onClick = onNavigateToContractors
            )
        }

        // Feature 6: Visitor Gate Pass
        item {
            HubFeatureCard(
                title = "Visitor Pass & Gate Security",
                subtitle = "Pre-approve delivery partners, cabs, and personal guests with secure PINs",
                badgeText = "${state.visitors.size} Active Passes",
                icon = Icons.Rounded.Shield,
                iconBackgroundColor = Color(0xFF7C3AED),
                badgeBackgroundColor = Color(0xFFF5F3FF),
                badgeTextColor = Color(0xFF6D28D9),
                onClick = onNavigateToVisitorPass
            )
        }

        // Feature 7: Guard Gate Terminal Kiosk
        item {
            HubFeatureCard(
                title = "Guard Gate Terminal (PIN Validator)",
                subtitle = "Terminal for security guard: 4-digit PIN verification, check-in & walk-ins",
                badgeText = "Gate Kiosk Mode",
                icon = Icons.Rounded.Security,
                iconBackgroundColor = Color(0xFF0F172A),
                badgeBackgroundColor = Color(0xFFF1F5F9),
                badgeTextColor = Color(0xFF334155),
                onClick = onNavigateToSecurityTerminal
            )
        }

        // Extra bottom spacing
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HubFeatureCard(
    title: String,
    subtitle: String,
    badgeText: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
    badgeBackgroundColor: Color,
    badgeTextColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeBackgroundColor
                ) {
                    Text(
                        text = badgeText,
                        color = badgeTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Open $title",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
