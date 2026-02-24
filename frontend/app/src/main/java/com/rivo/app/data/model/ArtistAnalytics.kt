package com.rivo.app.data.model

import java.util.Date

data class ArtistAnalytics(
    val artistId: String,
    val totalPlays: Int = 0,
    val totalSongs: Int = 0,
    val pendingCount: Int = 0,
    val unreadNotifications: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val monthlyListeners: Int = 0,
    val playlistAdds: Int = 0,
    val watchlistSaves: Int = 0,
    val uniqueListeners: Int = 0,
    val newFollowers: Int = 0,
    val lastUpdated: Date = Date()
)