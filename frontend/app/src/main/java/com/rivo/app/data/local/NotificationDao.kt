package com.rivo.app.data.local

import androidx.room.*
import com.rivo.app.data.model.Notification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<Notification>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    suspend fun countUnread(): Int

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}
