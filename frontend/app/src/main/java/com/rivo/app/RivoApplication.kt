package com.rivo.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RivoApplication : Application() {
    
    companion object {
        const val MUSIC_NOTIFICATION_CHANNEL_ID = "music_channel"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Music Playback"
            val descriptionText = "Notifications for music playback controls"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(MUSIC_NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
