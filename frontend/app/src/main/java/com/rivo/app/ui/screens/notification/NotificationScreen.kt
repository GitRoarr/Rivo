package com.rivo.app.ui.screens.notification

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rivo.app.data.model.Notification
import com.rivo.app.data.model.NotificationType
import com.rivo.app.ui.theme.*
import com.rivo.app.ui.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBackClick: () -> Unit,
    onNotificationClick: (Notification) -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ), label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Ambient header glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            RivoPurple.copy(alpha = 0.12f * glowAlpha),
                            DarkBackground
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Premium Top Bar
            NotificationTopBar(
                onBackClick = onBackClick,
                unreadCount = unreadCount,
                onMarkAllRead = { viewModel.markAllAsRead() },
                onClearAll = { viewModel.clearAllNotifications() }
            )

            // Notification Stats
            if (notifications.isNotEmpty()) {
                NotificationStatsBar(
                    total = notifications.size,
                    unread = unreadCount
                )
            }

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = RivoPink,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                } else if (notifications.isEmpty()) {
                    EmptyNotificationsView()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Group by date
                        val grouped = notifications.groupBy { getDateGroup(it.timestamp) }

                        grouped.forEach { (dateLabel, items) ->
                            item {
                                Text(
                                    text = dateLabel,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = LightGray,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                )
                            }

                            items(
                                items = items,
                                key = { it.id }
                            ) { notification ->
                                PremiumNotificationCard(
                                    notification = notification,
                                    onClick = {
                                        viewModel.markAsRead(notification.id)
                                        onNotificationClick(notification)
                                    },
                                    onDelete = { viewModel.deleteNotification(notification.id) }
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationTopBar(
    onBackClick: () -> Unit,
    unreadCount: Int,
    onMarkAllRead: () -> Unit,
    onClearAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 44.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(White.copy(alpha = 0.06f))
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = White, modifier = Modifier.size(22.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = White,
                    letterSpacing = (-0.5).sp
                )
            )
            if (unreadCount > 0) {
                Text(
                    text = "$unreadCount unread",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = RivoPink,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        // Mark all read
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(White.copy(alpha = 0.06f))
                .clickable(onClick = onMarkAllRead),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.DoneAll, contentDescription = "Mark All Read", tint = SuccessGreen, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Clear all
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(White.copy(alpha = 0.06f))
                .clickable(onClick = onClearAll),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = ErrorRed.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun NotificationStatsBar(total: Int, unread: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(RivoPurple)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("$total total", style = MaterialTheme.typography.bodySmall.copy(color = LightGray))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(RivoPink)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("$unread unread", style = MaterialTheme.typography.bodySmall.copy(color = LightGray))
        }
    }
}

@Composable
private fun PremiumNotificationCard(
    notification: Notification,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val typeColor = getNotificationColor(notification.type)
    val typeIcon = getNotificationIcon(notification.type)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = if (!notification.isRead)
            White.copy(alpha = 0.06f)
        else
            White.copy(alpha = 0.02f),
        shape = RoundedCornerShape(20.dp),
        border = if (!notification.isRead)
            BorderStroke(1.dp, typeColor.copy(alpha = 0.2f))
        else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon with gradient background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(typeColor.copy(alpha = 0.25f), typeColor.copy(alpha = 0.08f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = White,
                            fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (!notification.isRead) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(typeColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall.copy(color = LightGray),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = DarkGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatNotificationTime(notification.timestamp),
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkGray)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.04f))
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Delete",
                    tint = DarkGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyNotificationsView() {
    val infiniteTransition = rememberInfiniteTransition(label = "empty")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ), label = "float"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer { translationY = floatOffset }
                .shadow(24.dp, CircleShape, ambientColor = RivoPurple.copy(alpha = 0.3f))
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            White.copy(alpha = 0.06f),
                            White.copy(alpha = 0.02f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.NotificationsNone,
                contentDescription = null,
                tint = RivoPurple.copy(alpha = 0.6f),
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "All Caught Up!",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black,
                color = White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You'll be notified about new followers,\nmusic releases, and more.",
            style = MaterialTheme.typography.bodyMedium.copy(color = LightGray),
            modifier = Modifier.padding(horizontal = 48.dp),
            lineHeight = 22.sp
        )
    }
}

private fun getNotificationIcon(type: NotificationType): ImageVector = when (type) {
    NotificationType.NEW_FOLLOWER -> Icons.Filled.PersonAdd
    NotificationType.NEW_MUSIC -> Icons.Filled.MusicNote
    NotificationType.VERIFICATION -> Icons.Filled.Verified
    NotificationType.SYSTEM -> Icons.Filled.Info
    NotificationType.LIKE -> Icons.Filled.Favorite
    NotificationType.COMMENT -> Icons.Filled.ChatBubble
}

private fun getNotificationColor(type: NotificationType): Color = when (type) {
    NotificationType.NEW_FOLLOWER -> Color(0xFF4F9CF7)
    NotificationType.NEW_MUSIC -> Color(0xFFA855F7)
    NotificationType.VERIFICATION -> Color(0xFFFBBF24)
    NotificationType.SYSTEM -> Color(0xFF06B6D4)
    NotificationType.LIKE -> Color(0xFFEC4899)
    NotificationType.COMMENT -> Color(0xFF8B5CF6)
}

private fun getDateGroup(timestamp: Date): String {
    val now = Calendar.getInstance()
    val notifTime = Calendar.getInstance().apply { time = timestamp }

    return when {
        now.get(Calendar.DATE) == notifTime.get(Calendar.DATE) &&
        now.get(Calendar.MONTH) == notifTime.get(Calendar.MONTH) &&
        now.get(Calendar.YEAR) == notifTime.get(Calendar.YEAR) -> "TODAY"

        now.get(Calendar.DATE) - notifTime.get(Calendar.DATE) == 1 &&
        now.get(Calendar.MONTH) == notifTime.get(Calendar.MONTH) &&
        now.get(Calendar.YEAR) == notifTime.get(Calendar.YEAR) -> "YESTERDAY"

        now.get(Calendar.WEEK_OF_YEAR) == notifTime.get(Calendar.WEEK_OF_YEAR) &&
        now.get(Calendar.YEAR) == notifTime.get(Calendar.YEAR) -> "THIS WEEK"

        else -> "EARLIER"
    }
}

private fun formatNotificationTime(timestamp: Date): String {
    val now = Calendar.getInstance()
    val notifTime = Calendar.getInstance().apply { time = timestamp }
    val diff = now.timeInMillis - notifTime.timeInMillis
    val minutes = diff / (1000 * 60)
    val hours = minutes / 60

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        hours < 48 -> "Yesterday"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(timestamp)
    }
}
