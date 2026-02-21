package com.rivo.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artist_documents")
data class ArtistDocument(
    @PrimaryKey val id: String,
    val artistId: String,
    val documentType: String,
    val documentUrl: String,
    val verificationRequestId: String
)
