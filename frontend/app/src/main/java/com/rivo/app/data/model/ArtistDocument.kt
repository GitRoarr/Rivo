package com.rivo.app.data.model

data class ArtistDocument(
    val id: String,
    val artistId: String,
    val documentType: String,
    val documentUrl: String,
    val verificationRequestId: String
)
