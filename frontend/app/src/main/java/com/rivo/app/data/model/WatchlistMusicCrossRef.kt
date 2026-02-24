package com.rivo.app.data.model

data class WatchlistMusicCrossRef(
    val id: Long = 0,
    val watchlistId: Long, // Foreign key referencing Watchlist's id
    val musicId: String   // Foreign key referencing Music's id
)