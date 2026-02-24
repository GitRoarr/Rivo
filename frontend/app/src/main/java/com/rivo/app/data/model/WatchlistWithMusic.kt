package com.rivo.app.data.model

data class WatchlistWithMusic(
    val watchlist: Watchlist,
    val musicList: List<Music> = emptyList()
)
