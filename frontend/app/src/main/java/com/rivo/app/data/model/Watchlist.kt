package com.rivo.app.data.model

data class Watchlist(
    val id: Long = 0,

    val name: String? = null,

    val description: String? = null,

    val createdBy: String? = null,

    val songs: List<String> = emptyList()
) {
    constructor() : this(
        name = "",
        description = "",
        createdBy = ""
    )
}