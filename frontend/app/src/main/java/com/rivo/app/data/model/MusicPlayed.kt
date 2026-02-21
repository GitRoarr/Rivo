package com.rivo.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "music_played")
data class MusicPlayed(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val musicId: String
)
