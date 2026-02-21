package com.rivo.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlists",
    indices = [androidx.room.Index("createdBy")],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["createdBy"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val description: String = "",
    val coverArtUrl: String? = null,
    val createdBy: String,
    val isPublic: Boolean = true
) {
    constructor() : this(
        name = "",
        createdBy = ""
    )
}