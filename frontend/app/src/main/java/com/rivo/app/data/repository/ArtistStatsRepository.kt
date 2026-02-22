package com.rivo.app.data.repository

import android.util.Log
import com.rivo.app.data.local.ArtistStatsDao
import com.rivo.app.data.model.ArtistAnalytics
import com.rivo.app.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtistStatsRepository @Inject constructor(
    private val artistStatsDao: ArtistStatsDao,
    private val apiService: ApiService
) {

    fun getArtistAnalytics(artistId: String): Flow<ArtistAnalytics?> {
        return artistStatsDao.getArtistAnalyticsFlow(artistId)
    }

    suspend fun refreshArtistStats(): Result<ArtistAnalytics> {
        return try {
            val response = apiService.getArtistStats()
            if (response.isSuccessful && response.body() != null) {
                val stats = response.body()!!
                // Map frontend model - we use artistId as primary key
                // For now, we don't have artistId in the response, so we'll need it or assume current
                val analytics = ArtistAnalytics(
                    artistId = "", // We'll need to set this from caller or rethink model
                    totalPlays = stats.totalPlays,
                    newFollowers = stats.followersCount,
                    totalSongs = stats.totalSongs,
                    pendingCount = stats.pendingCount,
                    unreadNotifications = stats.unreadNotifications,
                    lastUpdated = Date()
                )
                // Need a better way to handle artistId here
                Result.success(analytics)
            } else {
                Result.failure(Exception("Failed to fetch artist stats: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ArtistStatsRepo", "Error refreshing stats", e)
            Result.failure(e)
        }
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

    suspend fun incrementArtistPlayCount(artistId: String) {
        incrementPlayCount(artistId)
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
                    lastUpdated = Date()
                )
            )
        }
    }
}
