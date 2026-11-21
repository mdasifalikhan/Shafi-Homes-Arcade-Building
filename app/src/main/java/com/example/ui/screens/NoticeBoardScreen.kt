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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.data.model.Notice
import com.example.data.model.NoticeCategory
import com.example.ui.SocietyDashboardState
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.MonoTag
import com.example.ui.components.formatEpochDateTime
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.StatusPendingAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeBoardScreen(
    state: SocietyDashboardState,
    onPostNotice: (title: String, content: String, category: NoticeCategory, author: String, isPinned: Boolean) -> Unit,
    onDeleteNotice: (String) -> Unit,
    onNavigateToAmenities: () -> Unit = {},
    onNavigateToEmergencySos: () -> Unit = {},
    onNavigateToDirectory: () -> Unit = {},
    onNavigateToPolls: () -> Unit = {},
    onNavigateToHelpers: () -> Unit = {},
    onNavigateToHub: () -> Unit = {},
    onNavigateToSecurityTerminal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<NoticeCategory?>(null) }
    var showNewNoticeSheet by remember { mutableStateOf(false) }

    val filteredNotices = remember(state.notices, selectedCategory) {
        if (selectedCategory == null) state.notices
        else state.notices.filter { it.categoryEnum == selectedCategory }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewNoticeSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Post Notice", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Quick Services Shortcuts Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionChip(
                    label = "🚨 Emergency SOS",
                    containerColor = Color(0xFFFEF2F2),
                    textColor = Color(0xFFDC2626),
                    borderColor = Color(0xFFFCA5A5),
                    onClick = onNavigateToEmergencySos
                )
                QuickActionChip(
                    label = "🏛️ Book Amenity",
                    containerColor = Color(0xFFEEF2FF),
                    textColor = Color(0xFF4338CA),
                    borderColor = Color(0xFFC7D2FE),
                    onClick = onNavigateToAmenities
                )
                QuickActionChip(
                    label = "👥 Directory",
                    containerColor = Color(0xFFF0FDFA),
                    textColor = Color(0xFF0F766E),
                    borderColor = Color(0xFF99F6E4),
                    onClick = onNavigateToDirectory
                )
                QuickActionChip(
                    label = "📊 Polls & Votes",
                    containerColor = Color(0xFFFFFBEB),
                    textColor = Color(0xFFB45309),
                    borderColor = Color(0xFFFDE68A),
                    onClick = onNavigateToPolls
                )
                QuickActionChip(
                    label = "🧹 Staff Tracker",
                    containerColor = Color(0xFFECFDF5),
                    textColor = Color(0xFF047857),
                    borderColor = Color(0xFFA7F3D0),
                    onClick = onNavigateToHelpers
                )
                QuickActionChip(
                    label = "🛡️ Gate Terminal",
                    containerColor = Color(0xFFF1F5F9),
                    textColor = Color(0xFF1E293B),
                    borderColor = Color(0xFFCBD5E1),
                    onClick = onNavigateToSecurityTerminal
                )
                QuickActionChip(
                    label = "✨ Services Hub",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    onClick = onNavigateToHub
                )
            }

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All (${state.notices.size})") }
                )
                NoticeCategory.entries.forEach { cat ->
                    val count = state.notices.count { it.categoryEnum == cat }
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text("${cat.label} ($count)") }
                    )
                }
            }

            if (filteredNotices.isEmpty()) {
                EmptyStatePanel(
                    title = "No Notices Posted",
                    subtitle = "The digital bulletin board is clear. Committee members can tap '+ Post Notice' to publish building updates.",
                    actionLabel = "Post Notice",
                    onAction = { showNewNoticeSheet = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 92.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNotices, key = { it.id }) { notice ->
                        NoticeCard(
                            notice = notice,
                            onDelete = { onDeleteNotice(notice.id) }
                        )
                    }
                }
            }
        }
    }

    if (showNewNoticeSheet) {
        NewNoticeSheet(
            onDismiss = { showNewNoticeSheet = false },
            onSubmit = { title, content, cat, author, pinned ->
                onPostNotice(title, content, cat, author, pinned)
                showNewNoticeSheet = false
            }
        )
    }
}

@Composable
private fun NoticeCard(
    notice: Notice,
    onDelete: () -> Unit
) {
    val isEmergency = notice.categoryEnum == NoticeCategory.EMERGENCY
    val cardBorder = when {
        isEmergency -> BorderStroke(1.5.dp, StatusOverdueRed.copy(alpha = 0.6f))
        notice.isPinned -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }

    val cardBg = when {
        isEmergency -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        notice.isPinned -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = cardBorder,
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    if (notice.isPinned) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PushPin,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "PINNED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    MonoTag(text = notice.categoryEnum.label)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete Notice",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = notice.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "By ${notice.author}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatEpochDateTime(notice.createdAtMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewNoticeSheet(
    onDismiss: () -> Unit,
    onSubmit: (String, String, NoticeCategory, String, Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(NoticeCategory.GENERAL) }
    var author by remember { mutableStateOf("Managing Committee") }
    var isPinned by remember { mutableStateOf(false) }
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
                    imageVector = Icons.Rounded.Campaign,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Publish Notice", style = MaterialTheme.typography.titleLarge)
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Notice Headline / Subject *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Category", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NoticeCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat.label) }
                    )
                }
            }

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Detailed Notice Content *") },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                label = { Text("Published By") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Pin to Top of Notice Board", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Keep this announcement highlighted at the top",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isPinned,
                    onCheckedChange = { isPinned = it }
                )
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
                    when {
                        title.trim().length < 3 -> validationError = "Headline must be at least 3 characters."
                        content.trim().length < 5 -> validationError = "Notice content must be at least 5 characters."
                        else -> {
                            onSubmit(title.trim(), content.trim(), category, author.trim(), isPinned)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Publish to Society", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    label: String,
    containerColor: Color,
    textColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

