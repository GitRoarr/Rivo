package com.rivo.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.google.gson.annotations.SerializedName
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
    ]
)
@TypeConverters(Converters::class)
data class Music(
    @PrimaryKey
    @SerializedName("_id", alternate = ["id"])
    val id: String = "",
    
    val title: String? = null,
    
    @SerializedName("artistName")
    val artist: String? = null,
    
    @SerializedName("artist")
    val artistId: String? = null,

    val playlistId: Long? = null,
    val album: String? = null,
    val duration: Long = 0,
    
    @SerializedName("url", alternate = ["path"])
    val path: String? = null,
    
    @SerializedName("coverImageUrl", alternate = ["artworkUri"])
    val artworkUri: String? = null,
    
    val isFavorite: Boolean = false,
    
    @SerializedName("plays", alternate = ["playCount"])
    val playCount: Int = 0,
    
    @SerializedName("createdAt")
    val uploadDate: java.util.Date? = java.util.Date(),
    
    @SerializedName("isApproved")
    val isApproved: Boolean = false,
    
    val description: String? = null,
    val genre: String? = null,
    
    @ColumnInfo(name = "userId") val userId: String? = null,

    var approvalStatus: MusicApprovalStatus = MusicApprovalStatus.PENDING
) {
    init {
        // Automatically sync approvalStatus with isApproved when coming from network
        if (isApproved && approvalStatus == MusicApprovalStatus.PENDING) {
            approvalStatus = MusicApprovalStatus.APPROVED
        }
    }
}
