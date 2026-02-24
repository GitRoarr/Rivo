package com.rivo.app.data.model

import java.util.Date

enum class NotificationType {
    NEW_FOLLOWER,
    NEW_MUSIC,
    VERIFICATION,
    SYSTEM,
    LIKE,
    COMMENT
}

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.SYSTEM,
    val timestamp: Date = Date(),
    val isRead: Boolean = false,
    val relatedContentId: String? = null,
    val relatedContentType: String? = null,
    val imageUrl: String? = null
)
