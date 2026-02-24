package com.rivo.app.data.model

data class Playlist(
    val id: Long = 0L,
    val name: String? = "",
    val description: String = "",
    val coverArtUrl: String? = null,
    val createdBy: String? = "",
    val isPublic: Boolean = true,
    val songs: List<String> = emptyList()
) {
    constructor() : this(
        name = "",
        createdBy = ""
    )
}