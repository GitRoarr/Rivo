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
    val monthlyListeners: Int = 0,
    val playlistAdds: Int = 0,
    val watchlistSaves: Int = 0,
    val uniqueListeners: Int = 0,
    val newFollowers: Int = 0,
    val averageListenTime: Long = 0, // in seconds
    val topSongs: Map<String, Int> = emptyMap(), // song name to play count
    val listenerDemographics: Map<String, Float> = emptyMap(), // region to percentage
    val playCountByDay: Map<String, Int> = emptyMap(), // date string to play count
    val lastUpdated: Date = Date()
) {
    // Empty constructor for Room
    constructor() : this(artistId = "")
}