package com.rivo.app.ui.screens.notification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rivo.app.data.model.Notification
import com.rivo.app.data.model.NotificationType
import com.rivo.app.ui.theme.Primary
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
    val isLoading by viewModel.isLoading.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.markAllAsRead() }) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Mark All as Read",
                            tint = Color.White
                        )
                    }
                    
                    IconButton(onClick = { viewModel.clearAllNotifications() }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear All",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            // Loading indicator
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            
            // Notifications list
            if (!isLoading) {
                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "No notifications yet",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "We'll notify you when something happens",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = notifications,
                            key = { it.id }
                        ) { notification ->
                            NotificationItem(
                                notification = notification,
                                onClick = { 
                                    viewModel.markAsRead(notification.id)
                                    onNotificationClick(notification)
                                },
                                onDeleteClick = { viewModel.deleteNotification(notification.id) }
                            )
                            
                            Divider(color = Color.DarkGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (!notification.isRead) Color(0xFF1A1A1A) else Color.Transparent
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Notification icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(getNotificationColor(notification.type).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getNotificationIcon(notification.type),
                contentDescription = null,
                tint = getNotificationColor(notification.type)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Notification content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = notification.title,
                color = Color.White,
                fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = notification.message,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = formatNotificationTime(notification.timestamp),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        
        // Delete button
        IconButton(
            onClick = { showDeleteConfirm = true }
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.Gray
            )
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Notification", color = Color.White) },
            text = { Text("Are you sure you want to delete this notification?", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClick()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false }
                ) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }
}

@Composable
private fun getNotificationIcon(type: NotificationType) = when (type) {
    NotificationType.NEW_FOLLOWER -> Icons.Default.Person
    NotificationType.NEW_MUSIC -> Icons.Default.MusicNote
    NotificationType.VERIFICATION -> Icons.Default.Verified
    NotificationType.SYSTEM -> Icons.Default.Info
    NotificationType.LIKE -> Icons.Default.Favorite
    NotificationType.COMMENT -> Icons.Default.Comment
}

@Composable
private fun getNotificationColor(type: NotificationType) = when (type) {
    NotificationType.NEW_FOLLOWER -> Color(0xFF2196F3) // Blue
    NotificationType.NEW_MUSIC -> Primary
    NotificationType.VERIFICATION -> Color(0xFFFFEB3B) // Yellow
    NotificationType.SYSTEM -> Color(0xFFFF5722) // Orange
    NotificationType.LIKE -> Color(0xFFE91E63) // Pink
    NotificationType.COMMENT -> Color(0xFF9C27B0) // Purple
}

private fun formatNotificationTime(timestamp: Date): String {
    val now = Calendar.getInstance()
    val notificationTime = Calendar.getInstance().apply { time = timestamp }
    
    return when {
        // Today
        now.get(Calendar.DATE) == notificationTime.get(Calendar.DATE) &&
        now.get(Calendar.MONTH) == notificationTime.get(Calendar.MONTH) &&
        now.get(Calendar.YEAR) == notificationTime.get(Calendar.YEAR) -> {
            "Today, ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(timestamp)}"
        }
        // Yesterday
        now.get(Calendar.DATE) - notificationTime.get(Calendar.DATE) == 1 &&
        now.get(Calendar.MONTH) == notificationTime.get(Calendar.MONTH) &&
        now.get(Calendar.YEAR) == notificationTime.get(Calendar.YEAR) -> {
            "Yesterday, ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(timestamp)}"
        }
        // This week
        now.get(Calendar.WEEK_OF_YEAR) == notificationTime.get(Calendar.WEEK_OF_YEAR) &&
        now.get(Calendar.YEAR) == notificationTime.get(Calendar.YEAR) -> {
            SimpleDateFormat("EEEE, h:mm a", Locale.getDefault()).format(timestamp)
        }
        // This year
        now.get(Calendar.YEAR) == notificationTime.get(Calendar.YEAR) -> {
            SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(timestamp)
        }
        // Older
        else -> {
            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(timestamp)
        }
    }
}
