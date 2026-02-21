package com.rivo.app.data.repository

import com.rivo.app.data.local.ArtistStatsDao
import com.rivo.app.data.model.ArtistAnalytics
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtistStatsRepository @Inject constructor(
    private val artistStatsDao: ArtistStatsDao
) {

    fun getArtistAnalytics(artistId: String): Flow<ArtistAnalytics?> {
        return artistStatsDao.getArtistAnalyticsFlow(artistId)
    }

    suspend fun getArtistAnalyticsById(artistId: String): ArtistAnalytics? {
        return artistStatsDao.getArtistAnalytics(artistId)
    }

    suspend fun addArtistAnalytics(artistAnalytics: ArtistAnalytics) {
        artistStatsDao.insertArtistAnalytics(artistAnalytics)
    }

    suspend fun incrementPlayCount(artistId: String) {
        updateAnalyticsField(artistId) { analytics ->
            analytics.copy(
                totalPlays = analytics.totalPlays + 1,
                lastUpdated = Date()
            )
        }
    }

    suspend fun incrementPlaylistAdds(artistId: String) {
        updateAnalyticsField(artistId) { analytics ->
            analytics.copy(
                playlistAdds = analytics.playlistAdds + 1,
                lastUpdated = Date()
            )
        }
    }

    suspend fun incrementWatchlistSaves(artistId: String) {
        updateAnalyticsField(artistId) { analytics ->
            analytics.copy(
                watchlistSaves = analytics.watchlistSaves + 1,
                lastUpdated = Date()
            )
        }
    }

    suspend fun updateMonthlyListeners(artistId: String, count: Int) {
        updateAnalyticsField(artistId) { analytics ->
            analytics.copy(
                monthlyListeners = count,
                lastUpdated = Date()
            )
        }
    }
    suspend fun incrementArtistPlayCount(artistId: String) {
        incrementPlayCount(artistId)
    }

    private suspend fun updateAnalyticsField(
        artistId: String,
        update: (ArtistAnalytics) -> ArtistAnalytics
    ) {
        val analytics = artistStatsDao.getArtistAnalytics(artistId)
        if (analytics != null) {
            artistStatsDao.updateArtistAnalytics(update(analytics))
        } else {
            artistStatsDao.insertArtistAnalytics(
                ArtistAnalytics(
                    artistId = artistId,
                    totalPlays = 0,
                    monthlyListeners = 0,
                    playlistAdds = 0,
                    watchlistSaves = 0,
                    lastUpdated = Date()
                )
            )
        }
    }
}
