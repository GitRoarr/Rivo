package com.rivo.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.rivo.app.data.local.Converters

enum class MusicApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Entity(
    tableName = "music",
    indices = [
        androidx.room.Index("artistId"),
        androidx.room.Index("playlistId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["artistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
@TypeConverters(Converters::class)
data class Music(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String,

    val playlistId: Long? = null,
    val album: String? = null,
    val duration: Long = 0,
    val path: String? = null,
    val artworkUri: String? = null,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val uploadDate: Long = System.currentTimeMillis(),
    val approvalStatus: MusicApprovalStatus = MusicApprovalStatus.APPROVED,
    val description: String? = null,
    val genre: String? = null,
    @ColumnInfo(name = "userId") val userId: String // Add this line

)
