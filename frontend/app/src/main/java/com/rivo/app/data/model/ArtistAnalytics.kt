package com.rivo.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.rivo.app.data.local.Converters
import java.util.Date

@Entity(
    tableName = "artist_analytics",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["artistId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(Converters::class)
data class ArtistAnalytics(
    @PrimaryKey
    val artistId: String,
    val totalPlays: Int = 0,
    val totalSongs: Int = 0,
    val pendingCount: Int = 0,
    val unreadNotifications: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val monthlyListeners: Int = 0,
    val playlistAdds: Int = 0,
    val watchlistSaves: Int = 0,
    val uniqueListeners: Int = 0,
    val newFollowers: Int = 0,
    val lastUpdated: Date = Date()
) {
    // Empty constructor for Room
    constructor() : this(artistId = "")
}