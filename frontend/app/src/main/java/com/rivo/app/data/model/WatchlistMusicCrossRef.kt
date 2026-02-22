package com.rivo.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "watchlist_music_cross_ref",
    indices = [
        Index("watchlistId"),
        Index("musicId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = Watchlist::class,
            parentColumns = ["id"],
            childColumns = ["watchlistId"],
            onDelete = ForeignKey.Companion.CASCADE
        ),
        ForeignKey(
            entity = Music::class,
            parentColumns = ["id"],
            childColumns = ["musicId"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ]
)
data class WatchlistMusicCrossRef(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val watchlistId: Long, // Foreign key referencing Watchlist's id
    val musicId: String   // Foreign key referencing Music's id
)