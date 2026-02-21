package com.rivo.app.data.repository

import com.rivo.app.data.local.ArtistStatsDao
import com.rivo.app.data.model.AnalyticsPeriod
import com.rivo.app.data.model.ArtistAnalytics
import com.rivo.app.data.model.Music
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AnalyticsRepository @Inject constructor(
    private val artistStatsDao: ArtistStatsDao,
    private val musicRepository: MusicRepository
) {

    suspend fun getArtistAnalytics(
        artistId: String,
        period: AnalyticsPeriod
    ): ArtistAnalytics? = withContext(Dispatchers.IO) {
        artistStatsDao.getArtistAnalytics(artistId)
    }

    fun getAllArtistAnalytics(): Flow<List<ArtistAnalytics>> = flow {
        emit(artistStatsDao.getAllArtistAnalytics())
    }.flowOn(Dispatchers.IO)

    suspend fun getTopSongs(
        artistId: String,
        period: AnalyticsPeriod
    ): List<Pair<String, Int>> = withContext(Dispatchers.IO) {
        val analytics = artistStatsDao.getArtistAnalytics(artistId)
        analytics
            ?.topSongs
            ?.entries
            ?.sortedByDescending { it.value }
            ?.map { it.key to it.value }
            ?: emptyList()
    }

    suspend fun getListenerDemographics(
        artistId: String
    ): Map<String, Float> = withContext(Dispatchers.IO) {
        artistStatsDao.getArtistAnalytics(artistId)
            ?.listenerDemographics
            ?: emptyMap()
    }


    suspend fun getPlayCountByDay(
        artistId: String,
        period: AnalyticsPeriod
    ): Map<String, Int> = withContext(Dispatchers.IO) {
        artistStatsDao.getArtistAnalytics(artistId)
            ?.playCountByDay
            ?: emptyMap()
    }

    suspend fun recordPlay(
        musicId: String,
        artistId: String
    ) = withContext(Dispatchers.IO) {
        musicRepository.incrementPlayCount(musicId)

        val analytics = artistStatsDao.getArtistAnalytics(artistId)
        if (analytics != null) {
            val updated = analytics.copy(
                totalPlays = analytics.totalPlays + 1,
                lastUpdated = Date()
            )
            artistStatsDao.updateArtistAnalytics(updated)
        } else {
            artistStatsDao.insertArtistAnalytics(
                ArtistAnalytics(
                    artistId = artistId,
                    totalPlays = 1,
                    lastUpdated = Date()
                )
            )
        }
    }

    suspend fun recordPlaylistAdd(
        artistId: String
    ) = withContext(Dispatchers.IO) {
        val analytics = artistStatsDao.getArtistAnalytics(artistId)
        if (analytics != null) {
            val updated = analytics.copy(
                playlistAdds = analytics.playlistAdds + 1,
                lastUpdated = Date()
            )
            artistStatsDao.updateArtistAnalytics(updated)
        } else {
            artistStatsDao.insertArtistAnalytics(
                ArtistAnalytics(
                    artistId = artistId,
                    playlistAdds = 1,
                    lastUpdated = Date()
                )
            )
        }
    }


    suspend fun recordWatchlistSave(
        artistId: String
    ) = withContext(Dispatchers.IO) {
        val analytics = artistStatsDao.getArtistAnalytics(artistId)
        if (analytics != null) {
            val updated = analytics.copy(
                watchlistSaves = analytics.watchlistSaves + 1,
                lastUpdated = Date()
            )
            artistStatsDao.updateArtistAnalytics(updated)
        } else {
            artistStatsDao.insertArtistAnalytics(
                ArtistAnalytics(
                    artistId = artistId,
                    watchlistSaves = 1,
                    lastUpdated = Date()
                )
            )
        }
    }

    suspend fun getAppAnalytics(): Map<String, Any> = withContext(Dispatchers.IO) {
        mapOf(
            "totalUsers" to 0,
            "totalArtists" to 0,
            "totalTracks" to 0,
            "totalPlays" to 0
        )
    }

    suspend fun getAnalyticsForPeriod(
        artistId: String,
        period: AnalyticsPeriod
    ): ArtistAnalytics? = withContext(Dispatchers.IO) {
        artistStatsDao.getArtistAnalytics(artistId)
    }
}
