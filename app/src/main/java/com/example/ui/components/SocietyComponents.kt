package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.Elevator
import androidx.compose.material.icons.rounded.Plumbing
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.DueStatus
import com.example.data.model.ScheduleStatus
import com.example.data.model.TicketPriority
import com.example.data.model.TicketStatus
import com.example.data.model.TradeCategory
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StatusOverdueBg
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.StatusPendingBg
import com.example.ui.theme.StatusScheduledBg
import com.example.ui.theme.StatusScheduledBlue
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    formatter.maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
    return formatter.format(amount)
}

fun formatEpochDay(epochDay: Long): String {
    val millis = TimeUnit.DAYS.toMillis(epochDay)
    val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    return sdf.format(Date(millis))
}

fun formatEpochDate(epochMillis: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}

fun formatEpochDateTime(epochMillis: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}

fun tradeIcon(trade: TradeCategory): ImageVector = when (trade) {
    TradeCategory.PLUMBING -> Icons.Rounded.Plumbing
    TradeCategory.ELECTRICAL -> Icons.Rounded.Bolt
    TradeCategory.ELEVATOR -> Icons.Rounded.Elevator
    TradeCategory.HVAC -> Icons.Rounded.AcUnit
    TradeCategory.CIVIL -> Icons.Rounded.Construction
    TradeCategory.SECURITY -> Icons.Rounded.Shield
}

@Composable
fun StatusPill(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DueStatusBadge(status: DueStatus) {
    val (bg, fg) = when (status) {
        DueStatus.PAID -> StatusPaidBg to StatusPaidGreen
        DueStatus.PENDING -> StatusPendingBg to StatusPendingAmber
        DueStatus.OVERDUE -> StatusOverdueBg to StatusOverdueRed
    }
    StatusPill(text = status.label, containerColor = bg, contentColor = fg)
}

@Composable
fun PriorityBadge(priority: TicketPriority) {
    val (bg, fg) = when (priority) {
        TicketPriority.EMERGENCY -> StatusOverdueBg to StatusOverdueRed
        TicketPriority.HIGH -> Color(0xFFFFE5D6) to Color(0xFFB54418)
        TicketPriority.MEDIUM -> StatusPendingBg to StatusPendingAmber
        TicketPriority.LOW -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    StatusPill(text = priority.label, containerColor = bg, contentColor = fg)
}

@Composable
fun TicketStatusBadge(status: TicketStatus, modifier: Modifier = Modifier) {
    val (label, bg, fg, icon) = when (status) {
        TicketStatus.LOGGED, TicketStatus.SCHEDULED ->
            Quad("Pending", StatusPendingBg, StatusPendingAmber, Icons.Rounded.Schedule)
        TicketStatus.IN_PROGRESS ->
            Quad("In Progress", StatusScheduledBg, StatusScheduledBlue, Icons.Rounded.Autorenew)
        TicketStatus.COMPLETED ->
            Quad("Completed", StatusPaidBg, StatusPaidGreen, Icons.Rounded.CheckCircle)
        TicketStatus.CANCELLED ->
            Quad(
                "Cancelled",
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.onSurfaceVariant,
                Icons.Rounded.Cancel
            )
    }
    Surface(
        modifier = modifier,
        color = bg,
        contentColor = fg,
        shape = RoundedCornerShape(999.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Icon(
                imageVector = icon,
                contentDescription = "$label status",
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RequestProgressBar(status: TicketStatus, modifier: Modifier = Modifier) {
    val activeSteps = when (status) {
        TicketStatus.LOGGED, TicketStatus.SCHEDULED -> 1
        TicketStatus.IN_PROGRESS -> 2
        TicketStatus.COMPLETED -> 3
        TicketStatus.CANCELLED -> 0
    }
    val activeColor = when (status) {
        TicketStatus.LOGGED, TicketStatus.SCHEDULED -> StatusPendingAmber
        TicketStatus.IN_PROGRESS -> StatusScheduledBlue
        TicketStatus.COMPLETED -> StatusPaidGreen
        TicketStatus.CANCELLED -> MaterialTheme.colorScheme.outline
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (step in 1..3) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (step <= activeSteps) activeColor
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun ScheduleStatusBadge(status: ScheduleStatus) {
    val (bg, fg) = when (status) {
        ScheduleStatus.SCHEDULED -> StatusScheduledBg to StatusScheduledBlue
        ScheduleStatus.EN_ROUTE -> StatusPendingBg to StatusPendingAmber
        ScheduleStatus.IN_WORK -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        ScheduleStatus.DONE -> StatusPaidBg to StatusPaidGreen
        ScheduleStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    StatusPill(text = status.label, containerColor = bg, contentColor = fg)
}

@Composable
fun TradeBadge(trade: TradeCategory, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = tradeIcon(trade),
                contentDescription = trade.label,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = trade.label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
        }
    }
}

@Composable
fun MonoTag(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            fontFamily = JetBrainsMonoFamily,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun MetricTile(
    label: String,
    value: String,
    sublabel: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sublabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyStatePanel(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    showIllustration: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showIllustration) {
                Image(
                    painter = painterResource(id = R.drawable.img_empty_repairs),
                    contentDescription = stringResource(id = R.string.cd_empty_illustration),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(148.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}
