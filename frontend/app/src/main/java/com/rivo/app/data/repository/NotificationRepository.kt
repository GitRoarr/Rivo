package com.rivo.app.data.repository

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rivo.app.MainActivity
import com.rivo.app.R
import com.rivo.app.data.local.NotificationDao
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.Notification
import com.rivo.app.data.model.NotificationType
import com.rivo.app.data.model.User
import com.rivo.app.data.remote.ApiService
import com.rivo.app.data.remote.CreateNotificationRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: NotificationDao,
    private val apiService: ApiService
) {
    companion object {
        private const val TAG = "NotificationRepo"
        private const val CHANNEL_ID = "rivo_channel"
        private const val MUSIC_NOTIFICATION_ID = 1
        private const val NEW_FOLLOWER_NOTIFICATION_ID = 2
        private const val VERIFICATION_NOTIFICATION_ID = 3
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Rivo Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for Rivo"
            }
            context
                .getSystemService(Context.NOTIFICATION_SERVICE)
                .let { it as NotificationManager }
                .createNotificationChannel(channel)
        }
    }

    /*** Android "push" notifications ***/
    private fun pendingMainIntent() =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK },
            PendingIntent.FLAG_IMMUTABLE
        )

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNowPlayingNotification(music: Music, isPlaying: Boolean) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_play)
            .setContentTitle(music.title)
            .setContentText("By ${music.artist}")
            .setContentIntent(pendingMainIntent())
            .setOngoing(isPlaying)

        NotificationManagerCompat.from(context)
            .notify(MUSIC_NOTIFICATION_ID, builder.build())
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNewFollowerNotification(artist: User, follower: User) {
        val text = "${follower.fullName} started following you"
        NotificationManagerCompat.from(context)
            .notify(
                NEW_FOLLOWER_NOTIFICATION_ID,
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_profile)
                    .setContentTitle("New Follower")
                    .setContentText(text)
                    .setContentIntent(pendingMainIntent())
                    .setAutoCancel(true)
                    .build()
            )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showVerificationNotification(artist: User, isVerified: Boolean) {
        val title = if (isVerified) "Verification Approved" else "Verification Rejected"
        val msg = if (isVerified)
            "Congratulations! Your artist account has been verified."
        else
            "Your verification request has been rejected."

        NotificationManagerCompat.from(context)
            .notify(
                VERIFICATION_NOTIFICATION_ID,
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_profile)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setContentIntent(pendingMainIntent())
                    .setAutoCancel(true)
                    .build()
            )
    }

    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }

    /*** In-app notification data operations ***/

    fun getNotifications(): Flow<List<Notification>> =
        dao.getAllNotifications()

    suspend fun getUnreadCount(): Int =
        dao.countUnread()

    suspend fun markAsRead(id: String) {
        dao.markAsRead(id)
        try { apiService.markNotificationAsRead(id) } catch (_: Exception) {}
    }

    suspend fun markAllAsRead() {
        dao.markAllAsRead()
        try { apiService.markAllNotificationsAsRead() } catch (_: Exception) {}
    }

    suspend fun deleteNotification(id: String) {
        dao.deleteById(id)
        try { apiService.deleteNotification(id) } catch (_: Exception) {}
    }

    suspend fun clearAllNotifications() {
        dao.clearAll()
        try { apiService.clearAllNotifications() } catch (_: Exception) {}
    }

    /**
     * Sync notifications from backend to local DB
     */
    suspend fun syncNotifications() {
        try {
            val response = apiService.getNotifications()
            if (response.isSuccessful) {
                val remoteNotifications = response.body() ?: return
                // Clear old and store fresh
                dao.clearAll()
                remoteNotifications.forEach { remote ->
                    val notification = Notification(
                        id = remote.id,
                        userId = remote.user,
                        title = remote.title,
                        message = remote.message,
                        type = mapNotificationType(remote.type),
                        timestamp = parseDate(remote.createdAt),
                        isRead = remote.isRead,
                        relatedContentId = remote.relatedId
                    )
                    dao.insert(notification)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing notifications: ${e.message}")
        }
    }

    /**
     * Create notification both locally and via backend API
     */
    suspend fun createNotification(
        userId: String,
        type: NotificationType,
        title: String,
        message: String,
        relatedId: String? = null
    ) {
        // Store locally
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = title,
            message = message,
            type = type,
            timestamp = Date(),
            isRead = false,
            relatedContentId = relatedId
        )
        dao.insert(notification)

        // Attempt to send to backend
        try {
            apiService.createNotification(
                CreateNotificationRequest(
                    userId = userId,
                    type = mapTypeToString(type),
                    title = title,
                    message = message,
                    relatedId = relatedId
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating remote notification: ${e.message}")
        }
    }

    private fun mapNotificationType(type: String): NotificationType = when (type) {
        "FOLLOW", "NEW_FOLLOWER" -> NotificationType.NEW_FOLLOWER
        "NEW_MUSIC", "UPLOAD" -> NotificationType.NEW_MUSIC
        "VERIFICATION", "APPROVAL", "REJECTION" -> NotificationType.VERIFICATION
        "LIKE" -> NotificationType.LIKE
        "COMMENT" -> NotificationType.COMMENT
        else -> NotificationType.SYSTEM
    }

    private fun mapTypeToString(type: NotificationType): String = when (type) {
        NotificationType.NEW_FOLLOWER -> "FOLLOW"
        NotificationType.NEW_MUSIC -> "NEW_MUSIC"
        NotificationType.VERIFICATION -> "VERIFICATION"
        NotificationType.SYSTEM -> "INFO"
        NotificationType.LIKE -> "INFO"
        NotificationType.COMMENT -> "INFO"
    }

    private fun parseDate(dateStr: String?): Date {
        if (dateStr == null) return Date()
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(dateStr) ?: Date()
        } catch (e: Exception) {
            try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(dateStr) ?: Date()
            } catch (_: Exception) {
                Date()
            }
        }
    }
}
