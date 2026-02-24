package com.rivo.app.data.repository

import android.util.Log
import com.rivo.app.data.model.Watchlist
import com.rivo.app.data.model.WatchlistWithMusic
import com.rivo.app.data.remote.ApiService
import com.rivo.app.data.remote.WatchlistRequest
import com.rivo.app.data.remote.AddSongRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Singleton
class WatchlistRepository @Inject constructor(
    private val apiService: ApiService
) {
    private val _watchlists = MutableStateFlow<List<Watchlist>>(emptyList())
    val watchlists: StateFlow<List<Watchlist>> = _watchlists.asStateFlow()

    suspend fun syncUserWatchlistsFromRemote() {
        try {
            val response = apiService.getUserWatchlists()
            if (response.isSuccessful) {
                _watchlists.value = response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("WatchlistRepository", "Error syncing watchlists from remote: ${e.message}")
        }
    }

    fun getWatchlistsByUser(userId: String): Flow<List<Watchlist>> {
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            syncUserWatchlistsFromRemote()
        }
        return watchlists
    }

    fun getWatchlistWithMusic(watchlistId: Long): Flow<WatchlistWithMusic?> {
        val flow = MutableStateFlow<WatchlistWithMusic?>(null)
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getWatchlistById(watchlistId)
                if (response.isSuccessful) {
                    val watchlist = response.body()
                    if (watchlist != null) {
                        val musicList = watchlist.songs.mapNotNull { songId ->
                            try {
                                val musicResponse = apiService.getMusicById(songId)
                                if (musicResponse.isSuccessful) musicResponse.body() else null
                            } catch (e: Exception) { null }
                        }
                        flow.value = WatchlistWithMusic(watchlist = watchlist, musicList = musicList)
                    }
                }
            } catch (e: Exception) {
                Log.e("WatchlistRepository", "Error fetching watchlist with music: ${e.message}")
            }
        }
        return flow
    }

    suspend fun createWatchlist(watchlist: Watchlist): Boolean {
        return try {
            val response = apiService.createWatchlist(
                WatchlistRequest(
                    id = watchlist.id,
                    name = watchlist.name ?: "",
                    description = watchlist.description ?: ""
                )
            )

            if (response.isSuccessful) {
                syncUserWatchlistsFromRemote()
                return true
            }
            false
        } catch (e: Exception) {
            Log.e("WatchlistRepository", "Error creating watchlist: ${e.message}")
            false
        }
    }

    suspend fun deleteWatchlist(watchlist: Watchlist) {
        try {
            val response = apiService.deleteWatchlist(watchlist.id)
            if (response.isSuccessful) {
                syncUserWatchlistsFromRemote()
            }
        } catch (e: Exception) {
            Log.e("WatchlistRepository", "Error deleting watchlist: ${e.message}")
        }
    }

    suspend fun addMusicToWatchlist(watchlistId: Long, musicId: String) {
        try {
            val response = apiService.addSongToWatchlist(watchlistId, AddSongRequest(musicId))
            if (response.isSuccessful) {
                syncUserWatchlistsFromRemote()
            }
        } catch (e: Exception) {
            Log.e("WatchlistRepository", "API addMusicToWatchlist failed: ${e.message}")
        }
    }

    suspend fun removeMusicFromWatchlist(watchlistId: Long, musicId: String) {
        try {
            val response = apiService.removeSongFromWatchlist(watchlistId, musicId)
            if (response.isSuccessful) {
                syncUserWatchlistsFromRemote()
            }
        } catch (e: Exception) {
            Log.e("WatchlistRepository", "API removeMusicFromWatchlist failed: ${e.message}")
        }
    }

    fun getAllWatchlistsWithMusicForUser(userId: String): Flow<List<Watchlist>> {
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            syncUserWatchlistsFromRemote()
        }
        return watchlists
    }

    suspend fun isMusicInUserWatchlist(userId: String, musicId: String): Boolean {
        return try {
            val response = apiService.checkSongInWatchlists(musicId)
            if (response.isSuccessful) {
                response.body()?.inWatchlist ?: false
            } else false
        } catch (e: Exception) {
            Log.e("WatchlistRepository", "API isMusicInUserWatchlist failed: ${e.message}")
            false
        }
    }
}
