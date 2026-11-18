package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.TicketPriority
import com.example.data.model.TradeCategory
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.StatusScheduledBlue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewMaintenanceRequestScreen(
    defaultUnit: String,
    onCancel: () -> Unit,
    onSubmit: (
        unitLocation: String,
        title: String,
        description: String,
        category: TradeCategory,
        priority: TicketPriority,
        estimatedCost: Double,
        photoLabel: String?,
        photoUri: String?,
        autoScheduleNow: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var unitLocation by rememberSaveable(defaultUnit) { mutableStateOf(defaultUnit) }
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(TradeCategory.PLUMBING) }
    var priority by rememberSaveable { mutableStateOf(TicketPriority.MEDIUM) }
    var estimatedCost by rememberSaveable { mutableStateOf("600") }
    var selectedPhotoUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var autoScheduleNow by rememberSaveable { mutableStateOf(true) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val selectedPhotoUri = remember(selectedPhotoUriString) {
        selectedPhotoUriString?.let { Uri.parse(it) }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUriString = uri.toString()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "New Maintenance Request",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Submit flat or common-area repair details with priority level and optional photo evidence.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedTextField(
            value = unitLocation,
            onValueChange = { unitLocation = it },
            label = { Text("Unit / Common Area Location") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("new_request_location_input")
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Issue Summary Title") },
            placeholder = { Text("e.g. Riser pipe valve leak") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("new_request_title_input")
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Issue Description") },
            placeholder = { Text("Describe the issue, exact location, and visible damage...") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("new_request_description_input")
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Trade Category",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TradeCategory.entries.forEach { trade ->
                    FilterChip(
                        selected = category == trade,
                        onClick = { category = trade },
                        label = { Text(trade.label) },
                        modifier = Modifier.testTag("trade_chip_${trade.name.lowercase()}")
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Priority Level",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TicketPriority.entries.forEach { level ->
                    val accent = priorityAccentColor(level)
                    FilterChip(
                        selected = priority == level,
                        onClick = { priority = level },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Flag,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(level.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accent.copy(alpha = 0.14f),
                            selectedLabelColor = accent
                        ),
                        modifier = Modifier.testTag("priority_chip_${level.name.lowercase()}")
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Image Attachment",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            val outlineColor = MaterialTheme.colorScheme.outline
            if (selectedPhotoUri == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .drawBehind {
                            val strokeWidth = 1.5.dp.toPx()
                            val cornerRadius = 16.dp.toPx()
                            drawRoundRect(
                                color = outlineColor,
                                style = Stroke(
                                    width = strokeWidth,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
                                ),
                                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                            )
                        }
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(16.dp)
                        .testTag("image_attachment_placeholder"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.AddPhotoAlternate,
                                    contentDescription = "Attach maintenance photo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Text(
                            text = "Tap to attach issue photo",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Image attachment placeholder • PNG or JPG from gallery",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("image_attachment_preview")
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            AsyncImage(
                                model = selectedPhotoUri,
                                contentDescription = "Attached maintenance issue photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = { selectedPhotoUriString = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .testTag("remove_attached_photo_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Remove attached photo",
                                    tint = Color.White
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedPhotoUri.lastPathSegment?.take(40) ?: "Photo attached",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.height(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PhotoCamera,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Replace")
                            }
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = estimatedCost,
            onValueChange = { estimatedCost = it },
            label = { Text("Estimated Repair Cost") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("new_request_estimate_input")
        )

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-Schedule Contractor",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Assign earliest matching specialist slot on submit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = autoScheduleNow,
                    onCheckedChange = { autoScheduleNow = it },
                    modifier = Modifier.testTag("new_request_autoschedule_switch")
                )
            }
        }

        if (validationError != null) {
            Text(
                text = validationError ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = StatusOverdueRed,
                modifier = Modifier.testTag("new_request_validation_error")
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("cancel_new_request_button")
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    val est = estimatedCost.toDoubleOrNull() ?: 0.0
                    when {
                        unitLocation.trim().isEmpty() ->
                            validationError = "Location is required."
                        title.trim().length < 3 ->
                            validationError = "Title must be at least 3 characters."
                        description.trim().length < 5 ->
                            validationError = "Issue description must be at least 5 characters."
                        est < 0.0 ->
                            validationError = "Estimated cost must be 0 or greater."
                        else -> {
                            validationError = null
                            onSubmit(
                                unitLocation.trim(),
                                title.trim(),
                                description.trim(),
                                category,
                                priority,
                                est,
                                selectedPhotoUri?.lastPathSegment?.take(100),
                                selectedPhotoUri?.toString()?.take(380),
                                autoScheduleNow
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(2f)
                    .height(50.dp)
                    .testTag("submit_new_request_button")
            ) {
                Text(if (autoScheduleNow) "Submit & Auto-Schedule" else "Submit Request")
            }
        }
    }
}

private fun priorityAccentColor(priority: TicketPriority): Color = when (priority) {
    TicketPriority.EMERGENCY -> StatusOverdueRed
    TicketPriority.HIGH -> StatusPendingAmber
    TicketPriority.MEDIUM -> StatusScheduledBlue
    TicketPriority.LOW -> StatusPaidGreen
}
