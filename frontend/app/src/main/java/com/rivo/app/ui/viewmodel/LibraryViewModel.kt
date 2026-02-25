package com.rivo.app.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.Playlist
import com.rivo.app.data.model.PlaylistWithMusic
import com.rivo.app.data.model.WatchlistWithMusic
import com.rivo.app.data.model.Watchlist
import com.rivo.app.data.repository.PlaylistRepository
import com.rivo.app.data.repository.WatchlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.delay

sealed class LibraryItem {
    data class PlaylistItem(val playlistWithMusic: PlaylistWithMusic) : LibraryItem()
    data class WatchlistItem(val watchlistWithMusic: WatchlistWithMusic) : LibraryItem()
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    private val _userPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val userPlaylists: StateFlow<List<Playlist>> = _userPlaylists.asStateFlow()

    private val _userWatchlists = MutableStateFlow<List<Watchlist>>(emptyList())
    val userWatchlists: StateFlow<List<Watchlist>> = _userWatchlists.asStateFlow()

    private val _currentLibraryItem = MutableStateFlow<LibraryItem?>(null)
    val currentLibraryItem: StateFlow<LibraryItem?> = _currentLibraryItem.asStateFlow()

    private val _playlistWithMusicList = mutableStateListOf<PlaylistWithMusic>()
    val playlistWithMusicList: List<PlaylistWithMusic> = _playlistWithMusicList

    private val _currentPlaylistWithMusic = MutableStateFlow<PlaylistWithMusic?>(null)
    val currentPlaylistWithMusic: StateFlow<PlaylistWithMusic?> = _currentPlaylistWithMusic.asStateFlow()

    fun loadPlaylistWithMusic(playlistId: Long) {
        _currentPlaylistWithMusic.value = null
        viewModelScope.launch {
            playlistRepository.getPlaylistWithMusic(playlistId).collect {
                _currentPlaylistWithMusic.value = it
            }
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _snackBarMessage = MutableStateFlow<String?>(null)
    val snackBarMessage: StateFlow<String?> = _snackBarMessage.asStateFlow()

    fun loadUserPlaylists(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Sync with remote - use timeout to prevent infinite hang
                try {
                    kotlinx.coroutines.withTimeoutOrNull(10_000L) {
                        playlistRepository.syncUserPlaylistsFromRemote()
                    } ?: Log.w("LibraryViewModel", "Sync playlists timed out, using cached data")
                } catch (e: Exception) {
                    Log.w("LibraryViewModel", "Sync failed, using cached data: ${e.message}")
                }

                // Get playlists - use first() for a one-shot fetch instead of infinite collect
                val playlists = try {
                    kotlinx.coroutines.withTimeoutOrNull(8_000L) {
                        playlistRepository.getPlaylistsByUser(userId).first()
                    } ?: emptyList()
                } catch (e: Exception) {
                    Log.e("LibraryViewModel", "Failed to get playlists: ${e.message}")
                    emptyList()
                }

                _userPlaylists.value = playlists
                _playlistWithMusicList.clear()

                // Fetch each playlist's music with timeout
                playlists.forEach { playlist ->
                    try {
                        val pwm = kotlinx.coroutines.withTimeoutOrNull(6_000L) {
                            playlistRepository.getPlaylistWithMusic(playlist.id).first { it != null }
                        }
                        pwm?.let { _playlistWithMusicList.add(it) }
                    } catch (e: Exception) {
                        Log.w("LibraryViewModel", "Skipped playlist ${playlist.id}: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error loading playlists: ${e.message}", e)
                _error.value = "Failed to load playlists: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                playlistRepository.updatePlaylist(playlist)
                delay(300)
                loadUserPlaylists(playlist.createdBy ?: "")
                _snackBarMessage.value = "Playlist updated successfully"
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error updating playlist: ${e.message}", e)
                _error.value = "Failed to update playlist: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserWatchlists(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Sync with remote - use timeout to prevent infinite hang
                try {
                    kotlinx.coroutines.withTimeoutOrNull(10_000L) {
                        watchlistRepository.syncUserWatchlistsFromRemote()
                    } ?: Log.w("LibraryViewModel", "Sync watchlists timed out, using cached data")
                } catch (e: Exception) {
                    Log.w("LibraryViewModel", "Sync watchlists failed: ${e.message}")
                }

                // Get watchlists - use first() for one-shot fetch
                val watchlists = try {
                    kotlinx.coroutines.withTimeoutOrNull(8_000L) {
                        watchlistRepository.getWatchlistsByUser(userId).first()
                    } ?: emptyList()
                } catch (e: Exception) {
                    Log.e("LibraryViewModel", "Failed to get watchlists: ${e.message}")
                    emptyList()
                }

                _userWatchlists.value = watchlists

                // If we have no watchlists, create a default one
                if (watchlists.isEmpty()) {
                    Log.d("LibraryViewModel", "Creating default watchlist for user: $userId")
                    try {
                        createLibraryItem("My Watchlist", "Favorite songs", userId, false)
                        delay(800)
                        val reloaded = kotlinx.coroutines.withTimeoutOrNull(5_000L) {
                            watchlistRepository.getWatchlistsByUser(userId).first()
                        } ?: emptyList()
                        _userWatchlists.value = reloaded
                        Log.d("LibraryViewModel", "Reloaded watchlists, count: ${reloaded.size}")
                    } catch (e: Exception) {
                        Log.w("LibraryViewModel", "Failed to create default watchlist: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error loading watchlists: ${e.message}", e)
                _error.value = "Failed to load watchlists: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun getPlaylistWithMusic(playlistId: Long): Flow<PlaylistWithMusic?> {
        return playlistRepository.getPlaylistWithMusic(playlistId)
    }

    fun loadLibraryItem(libraryItemId: Long, isPlaylist: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (isPlaylist) {
                    val result = kotlinx.coroutines.withTimeoutOrNull(8_000L) {
                        playlistRepository.getPlaylistWithMusic(libraryItemId).first()
                    }
                    _currentLibraryItem.value = result?.let { LibraryItem.PlaylistItem(it) }
                } else {
                    val result = kotlinx.coroutines.withTimeoutOrNull(8_000L) {
                        watchlistRepository.getWatchlistWithMusic(libraryItemId).first()
                    }
                    _currentLibraryItem.value = result?.let { LibraryItem.WatchlistItem(it) }
                }
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error loading library item: ${e.message}", e)
                _error.value = "Failed to load item: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createLibraryItem(name: String, description: String, userId: String, isPlaylist: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (isPlaylist) {
                    Log.d("LibraryViewModel", "Creating playlist: $name for user: $userId")
                    val playlist = Playlist(
                        name = name,
                        description = description,
                        createdBy = userId
                    )
                    playlistRepository.createPlaylist(playlist)

                    delay(500)
                    loadUserPlaylists(userId)
                    _snackBarMessage.value = "Playlist created successfully"
                } else {
                    Log.d("LibraryViewModel", "Creating watchlist: $name for user: $userId")
                    val watchlist = Watchlist(
                        name = name,
                        description = description,
                        createdBy = userId
                    )
                    watchlistRepository.createWatchlist(watchlist)
                    delay(500)
                    loadUserWatchlists(userId)
                    _snackBarMessage.value = "Watchlist created successfully"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error creating library item: ${e.message}", e)
                _error.value = "Failed to create item: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deleteLibraryItem(libraryItem: Any, isPlaylist: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (isPlaylist) {
                    if (libraryItem is Playlist) {
                        playlistRepository.deletePlaylist(libraryItem)

                        delay(300)
                        loadUserPlaylists(libraryItem.createdBy ?: "")
                        _snackBarMessage.value = "Playlist deleted successfully"
                    } else {
                        _error.value = "Provided item is not a Playlist"
                        return@launch
                    }
                } else {
                    if (libraryItem is Watchlist) {
                        watchlistRepository.deleteWatchlist(libraryItem)

                        delay(300)
                        loadUserWatchlists(libraryItem.createdBy ?: "")
                        _snackBarMessage.value = "Watchlist deleted successfully"
                    } else {
                        _error.value = "Provided item is not a Watchlist"
                        return@launch
                    }
                }
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error deleting library item: ${e.message}", e)
                _error.value = "Failed to delete item: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun addMusicToLibraryItem(libraryItemId: Long, music: Music, isPlaylist: Boolean) {
        try {
            if (isPlaylist) {
                // Check if the playlist exists before adding music
                val playlist = _userPlaylists.value.find { it.id == libraryItemId }
                if (playlist != null) {
                    playlistRepository.addMusicToPlaylist(libraryItemId, music.id)

                    // Refresh details immediately for the current view
                    loadPlaylistWithMusic(libraryItemId)

                    // After adding music, reload playlists list
                    val userId = playlist.createdBy ?: ""
                    loadUserPlaylists(userId)
                    _snackBarMessage.value = "Added to ${playlist.name}"
                } else {
                    _error.value = "Playlist not found. Please try again."
                }
            } else {
                // Handle the case for watchlist
                val watchlist = _userWatchlists.value.find { it.id == libraryItemId }
                if (watchlist != null) {
                    watchlistRepository.addMusicToWatchlist(libraryItemId, music.id)
                    _snackBarMessage.value = "Added to Watchlist"

                    // Reload watchlists to update UI
                    val userId = watchlist.createdBy ?: ""
                    loadUserWatchlists(userId)
                } else {
                    _error.value = "Watchlist not found. Please try again."
                }
            }
        } catch (e: Exception) {
            Log.e("LibraryViewModel", "Error adding music to library item: ${e.message}", e)
            _error.value = "Failed to add music: ${e.message}"
        }
    }

    fun removeMusicFromLibraryItem(libraryItemId: Long, musicId: String, isPlaylist: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (isPlaylist) {
                    playlistRepository.removeMusicFromPlaylist(libraryItemId, musicId)

                    // Refresh details immediately
                    loadPlaylistWithMusic(libraryItemId)

                    val userId = _userPlaylists.value.find { it.id == libraryItemId }?.createdBy

                    if (userId != null) {
                        delay(300)
                        loadUserPlaylists(userId)
                        _snackBarMessage.value = "Removed from playlist"
                    }
                } else {
                    watchlistRepository.removeMusicFromWatchlist(libraryItemId, musicId)

                    val userId = _userWatchlists.value.find { it.id == libraryItemId }?.createdBy

                    if (userId != null) {
                        delay(300)
                        loadUserWatchlists(userId)
                        _snackBarMessage.value = "Removed from watchlist"
                    }
                }
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error removing music from library item: ${e.message}", e)
                _error.value = "Failed to remove music: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

}
