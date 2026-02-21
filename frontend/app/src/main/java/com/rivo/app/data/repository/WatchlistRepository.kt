package com.rivo.app.data.repository

import android.util.Log
import com.rivo.app.data.local.WatchlistDao
import com.rivo.app.data.model.Watchlist
import com.rivo.app.data.model.WatchlistMusicCrossRef
import com.rivo.app.data.local.WatchlistWithMusic
import com.rivo.app.data.remote.ApiService
import com.rivo.app.data.remote.WatchlistRequest
import com.rivo.app.data.remote.AddSongRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WatchlistRepository @Inject constructor(
    private val watchlistDao: WatchlistDao,
    private val userRepository: UserRepository,
    private val apiService: ApiService
) {
    fun getWatchlistsByUser(userId: String): Flow<List<Watchlist>> {
        return watchlistDao.getWatchlistsByUser(userId)
    }

    fun getWatchlistWithMusic(watchlistId: Long): Flow<WatchlistWithMusic> {
        return watchlistDao.getWatchlistWithMusic(watchlistId)
    }

    suspend fun createWatchlist(watchlist: Watchlist): Boolean {
        return try {
            val userId = watchlist.createdBy
            val user = userRepository.getUserById(userId)

            if (user == null) {
                Log.e("WatchlistRepository", "No user found with ID: $userId")
                return false
            }

            // Try API first
            try {
                val response = apiService.createWatchlist(
                    WatchlistRequest(
                        id = watchlist.id,
                        name = watchlist.name,
                        description = watchlist.description
                    )
                )

                if (response.isSuccessful) {
                    // Save to local DB as well
                    watchlistDao.insert(watchlist)
                    return true
                }
            } catch (e: Exception) {
                Log.e("WatchlistRepository", "API createWatchlist failed: ${e.message}", e)
            }

            // Fall back to local
            watchlistDao.insert(watchlist)
            true
        } catch (e: Exception) {
            Log.e("WatchlistRepository", "Error creating watchlist: ${e.message}", e)
            false
        }
    }

    suspend fun deleteWatchlist(watchlist: Watchlist) {
        try {
            // Try API first
            try {
                val response = apiService.deleteWatchlist(watchlist.id)

                if (response.isSuccessful) {
                    // Delete from local DB as well
                    watchlistDao.deleteWatchlist(watchlist)
                    return
                }
            } catch (e: Exception) {
                Log.e("WatchlistRepository", "API deleteWatchlist failed: ${e.message}", e)
            }

            // Fall back to local
            watchlistDao.deleteWatchlist(watchlist)
        } catch (e: Exception) {
            Log.e("WatchlistRepository", "Error deleting watchlist: ${e.message}", e)
            throw e
        }
    }

    suspend fun addMusicToWatchlist(watchlistId: Long, musicId: String) {
        try {
            val response = apiService.addSongToWatchlist(
                watchlistId,
                AddSongRequest(musicId)
            )

            if (response.isSuccessful) {
                val crossRef = WatchlistMusicCrossRef(
                    watchlistId = watchlistId,
                    musicId = musicId
                )
                watchlistDao.addMusicToWatchlist(crossRef)
                return
            }
        } catch (e: Exception) {
            Log.e("WatchlistRepository", "API addMusicToWatchlist failed: ${e.message}", e)
        }

        val crossRef = WatchlistMusicCrossRef(
            watchlistId = watchlistId,
            musicId = musicId
        )
        watchlistDao.addMusicToWatchlist(crossRef)
    }

    suspend fun removeMusicFromWatchlist(watchlistId: Long, musicId: String) {
        try {
            val response = apiService.removeSongFromWatchlist(watchlistId, musicId)

            if (response.isSuccessful) {
                val crossRef = WatchlistMusicCrossRef(
                    watchlistId = watchlistId,
                    musicId = musicId
                )
                watchlistDao.removeMusicFromWatchlist(crossRef)
                return
            }
        } catch (e: Exception) {
            Log.e("WatchlistRepository", "API removeMusicFromWatchlist failed: ${e.message}", e)
        }

        val crossRef = WatchlistMusicCrossRef(
            watchlistId = watchlistId,
            musicId = musicId
        )
        watchlistDao.removeMusicFromWatchlist(crossRef)
    }

    fun getAllWatchlistsWithMusicForUser(userId: String): Flow<List<WatchlistWithMusic>> {
        return watchlistDao.getAllWatchlistsWithMusicForUser(userId)
    }

    suspend fun isMusicInUserWatchlist(userId: String, musicId: String): Boolean {
        try {
            val response = apiService.checkSongInWatchlists(musicId)

            if (response.isSuccessful) {
                val result = response.body()
                if (result != null) {
                    return result.inWatchlist
                }
            }
        } catch (e: Exception) {
            Log.e("WatchlistRepository", "API isMusicInUserWatchlist failed: ${e.message}", e)
        }
        val watchlists = watchlistDao.getWatchlistsByUserSync(userId)
        if (watchlists.isEmpty()) return false

        for (watchlist in watchlists) {
            val count = watchlistDao.countMusicInWatchlist(watchlist.id, musicId)
            if (count > 0) return true
        }
        return false
    }
}
