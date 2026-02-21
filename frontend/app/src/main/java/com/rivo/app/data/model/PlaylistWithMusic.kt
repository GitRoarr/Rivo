package com.rivo.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "playlist_music_cross_ref",
    primaryKeys = ["playlistId", "musicId"],
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Music::class,
            parentColumns = ["id"],
            childColumns = ["musicId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlaylistMusicCrossRef(
    val playlistId: Long,
    val musicId: String
)