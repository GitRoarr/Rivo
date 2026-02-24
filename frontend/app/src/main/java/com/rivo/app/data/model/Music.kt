package com.rivo.app.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

enum class MusicApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED
}

data class Music(
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
    val uploadDate: Date? = Date(),
    
    @SerializedName("isApproved")
    val isApproved: Boolean = false,
    
    val description: String? = null,
    val genre: String? = null,
    
    val userId: String? = null
) {
    val approvalStatus: MusicApprovalStatus
        get() = if (isApproved) MusicApprovalStatus.APPROVED else MusicApprovalStatus.PENDING
}
