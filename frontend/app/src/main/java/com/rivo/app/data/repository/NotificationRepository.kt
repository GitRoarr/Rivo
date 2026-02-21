package com.rivo.app.data.repository

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rivo.app.MainActivity
import com.rivo.app.R
import com.rivo.app.data.local.NotificationDao
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.Notification
import com.rivo.app.data.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: NotificationDao
) {
    companion object {
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

    /*** Android “push” notifications ***/
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


    fun getNotifications(): Flow<List<Notification>> =
        dao.getAllNotifications()

    suspend fun getUnreadCount(): Int =
        dao.countUnread()

    suspend fun markAsRead(id: String) {
        dao.markAsRead(id)
    }

    suspend fun markAllAsRead() {
        dao.markAllAsRead()
    }

    suspend fun deleteNotification(id: String) {
        dao.deleteById(id)
    }

    suspend fun clearAllNotifications() {
        dao.clearAll()
    }
}
