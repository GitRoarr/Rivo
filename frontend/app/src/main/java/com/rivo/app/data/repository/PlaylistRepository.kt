package com.rivo.app.data.repository

import android.util.Log
import com.rivo.app.data.model.Playlist
import com.rivo.app.data.model.PlaylistWithMusic
import com.rivo.app.data.remote.ApiService
import com.rivo.app.data.remote.PlaylistRequest
import com.rivo.app.data.remote.PlaylistUpdateRequest
import com.rivo.app.data.remote.AddSongRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val apiService: ApiService
) {
    private val _playlists = kotlinx.coroutines.flow.MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: kotlinx.coroutines.flow.StateFlow<List<Playlist>> = _playlists.asStateFlow()

    suspend fun syncUserPlaylistsFromRemote() {
        try {
            val response = apiService.getUserPlaylists()
            if (response.isSuccessful) {
                _playlists.value = response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("PlaylistRepository", "Error syncing playlists from remote: ${e.message}")
        }
    }

    suspend fun createPlaylist(playlist: Playlist): Boolean {
        return try {
            val response = apiService.createPlaylist(
                PlaylistRequest(
                    id = playlist.id,
                    name = playlist.name ?: "",
                    description = playlist.description ?: "",
                    coverArtUrl = playlist.coverArtUrl ?: "",
                    isPublic = playlist.isPublic
                )
            )
            if (response.isSuccessful) {
                syncUserPlaylistsFromRemote()
                return true
            }
            false
        } catch (e: Exception) {
            Log.e("PlaylistRepository", "Error creating playlist: ${e.message}")
            false
        }
    }

    suspend fun updatePlaylist(playlist: Playlist) {
        try {
            val response = apiService.updatePlaylist(
                playlist.id,
                PlaylistUpdateRequest(
                    name = playlist.name ?: "",
                    description = playlist.description ?: "",
                    coverArtUrl = playlist.coverArtUrl ?: "",
                    isPublic = playlist.isPublic
                )
            )
            if (response.isSuccessful) {
                syncUserPlaylistsFromRemote()
            }
        } catch (e: Exception) {
            Log.e("PlaylistRepository", "Error updating playlist: ${e.message}")
        }
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        try {
            val response = apiService.deletePlaylist(playlist.id)
            if (response.isSuccessful) {
                syncUserPlaylistsFromRemote()
            }
        } catch (e: Exception) {
            Log.e("PlaylistRepository", "Error deleting playlist: ${e.message}")
        }
    }

    fun getPlaylistsByUser(userId: String): Flow<List<Playlist>> {
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            syncUserPlaylistsFromRemote()
        }
        return playlists
    }

    fun getPlaylistWithMusic(playlistId: Long): Flow<PlaylistWithMusic?> {
        val flow = kotlinx.coroutines.flow.MutableStateFlow<PlaylistWithMusic?>(null)
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val response = apiService.getPlaylistById(playlistId)
                if (response.isSuccessful) {
                    val playlist = response.body()
                    if (playlist != null) {
                        val musicList = playlist.songs.mapNotNull { songId ->
                            try {
                                val musicResponse = apiService.getMusicById(songId)
                                if (musicResponse.isSuccessful) musicResponse.body() else null
                            } catch (e: Exception) { null }
                        }
                        flow.value = PlaylistWithMusic(playlist = playlist, musicList = musicList)
                    }
                }
            } catch (e: Exception) {
                Log.e("PlaylistRepository", "Error fetching playlist with music: ${e.message}")
            }
        }
        return flow
    }

    suspend fun addMusicToPlaylist(playlistId: Long, musicId: String) {
        try {
            val response = apiService.addSongToPlaylist(playlistId, AddSongRequest(musicId))
            if (response.isSuccessful) {
                syncUserPlaylistsFromRemote()
            }
        } catch (e: Exception) {
            Log.e("PlaylistRepository", "API addMusicToPlaylist failed: ${e.message}")
        }
    }

    suspend fun removeMusicFromPlaylist(playlistId: Long, musicId: String) {
        try {
            val response = apiService.removeSongFromPlaylist(playlistId, musicId)
            if (response.isSuccessful) {
                syncUserPlaylistsFromRemote()
            }
        } catch (e: Exception) {
            Log.e("PlaylistRepository", "API removeMusicFromPlaylist failed: ${e.message}")
        }
    }

    suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return try {
            val response = apiService.getPlaylistById(playlistId)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPlaylistMusicCount(playlistId: Long): Int {
        return getPlaylistById(playlistId)?.songs?.size ?: 0
    }
}
