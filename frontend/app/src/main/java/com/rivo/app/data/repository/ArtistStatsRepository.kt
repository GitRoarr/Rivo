package com.rivo.app.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import com.rivo.app.data.model.ArtistAnalytics
import com.rivo.app.data.remote.ApiService
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

@Singleton
class ArtistStatsRepository @Inject constructor(
    private val apiService: ApiService
) {
    private val _artistAnalytics = MutableStateFlow<Map<String, ArtistAnalytics>>(emptyMap())

    fun getArtistAnalytics(artistId: String): Flow<ArtistAnalytics?> {
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            refreshArtistStats(artistId)
        }
        return _artistAnalytics.map { it[artistId] }
    }

    suspend fun refreshArtistStats(artistId: String? = null): Result<ArtistAnalytics> {
        return try {
            val response = apiService.getArtistStats()
            if (response.isSuccessful && response.body() != null) {
                val stats = response.body()!!
                val analytics = ArtistAnalytics(
                    artistId = artistId ?: "", 
                    totalPlays = stats.totalPlays,
                    totalSongs = stats.totalSongs,
                    pendingCount = stats.pendingCount,
                    unreadNotifications = stats.unreadNotifications,
                    followersCount = stats.followersCount,
                    followingCount = stats.followingCount,
                    monthlyListeners = stats.monthlyListeners,
                    newFollowers = stats.followersCount,
                    lastUpdated = Date()
                )
                if (artistId != null) {
                    val currentMap = _artistAnalytics.value.toMutableMap()
                    currentMap[artistId] = analytics
                    _artistAnalytics.value = currentMap
                }
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
        return _artistAnalytics.value[artistId]
    }

    suspend fun incrementArtistPlayCount(artistId: String) {
        // Increment handled on backend
    }

    suspend fun incrementPlaylistAdds(artistId: String) {
        // Increment handled on backend
    }

    suspend fun incrementWatchlistSaves(artistId: String) {
        // Increment handled on backend
    }

    suspend fun updateMonthlyListeners(artistId: String, count: Int) {
        // Backend handles demographic/reach metrics
    }

    suspend fun incrementPlayCount(artistId: String) {
        incrementArtistPlayCount(artistId)
    }

    suspend fun addArtistAnalytics(analytics: ArtistAnalytics) {
        val currentMap = _artistAnalytics.value.toMutableMap()
        currentMap[analytics.artistId] = analytics
        _artistAnalytics.value = currentMap
    }
}
