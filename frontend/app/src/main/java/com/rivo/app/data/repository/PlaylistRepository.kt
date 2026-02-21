package com.rivo.app.data.repository

import android.util.Log
import com.rivo.app.data.local.MusicDao
import com.rivo.app.data.local.PlaylistDao
import com.rivo.app.data.model.Playlist
import com.rivo.app.data.model.PlaylistMusicCrossRef
import com.rivo.app.data.local.PlaylistWithMusic
import com.rivo.app.data.remote.ApiService
import com.rivo.app.data.remote.PlaylistRequest
import com.rivo.app.data.remote.PlaylistUpdateRequest
import com.rivo.app.data.remote.AddSongRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val musicDao: MusicDao,
    private val userRepository: UserRepository,
    private val apiService: ApiService
) {
    suspend fun createPlaylist(playlist: Playlist): Boolean {
        return try {
            val userId = playlist.createdBy
            val user = userRepository.getUserById(userId)

            if (user == null) {
                Log.e("PlaylistRepository", "No user found with ID: $userId")
                return false
            }

            // Try API first
            try {
                val response = apiService.createPlaylist(
                    PlaylistRequest(
                        id = playlist.id,
                        name = playlist.name,
                        description = playlist.description,
                        coverArtUrl = playlist.coverArtUrl,
                        isPublic = playlist.isPublic
                    )
                )

                if (response.isSuccessful) {
                    // Save to local DB as well
                    playlistDao.insertPlaylist(playlist)
                    return true
                }
            } catch (e: Exception) {
                Log.e("PlaylistRepository", "API createPlaylist failed: ${e.message}", e)
            }

            // Fall back to local
            playlistDao.insertPlaylist(playlist)
            true
        } catch (e: Exception) {
            Log.e("PlaylistRepository", "Error creating playlist: ${e.message}", e)
            false
        }
    }

    suspend fun updatePlaylist(playlist: Playlist) {
        try {
            // Try API first
            try {
                val response = apiService.updatePlaylist(
                    playlist.id,
                    PlaylistUpdateRequest(
                        name = playlist.name,
                        description = playlist.description,
                        coverArtUrl = playlist.coverArtUrl,
                        isPublic = playlist.isPublic
                    )
                )

                if (response.isSuccessful) {
                    // Update local DB as well
                    playlistDao.updatePlaylist(playlist)
                    return
                }
            } catch (e: Exception) {
                Log.e("PlaylistRepository", "API updatePlaylist failed: ${e.message}", e)
            }

            // Fall back to local
            playlistDao.updatePlaylist(playlist)
        } catch (e: Exception) {
            Log.e("PlaylistRepository", "Error updating playlist: ${e.message}", e)
            throw e
        }
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        try {
            // Try API first
            try {
                val response = apiService.deletePlaylist(playlist.id)

                if (response.isSuccessful) {
                    // Delete from local DB as well
                    playlistDao.deletePlaylist(playlist)
                    return
                }
            } catch (e: Exception) {
                Log.e("PlaylistRepository", "API deletePlaylist failed: ${e.message}", e)
            }

            // Fall back to local
            playlistDao.deletePlaylist(playlist)
        } catch (e: Exception) {
            Log.e("PlaylistRepository", "Error deleting playlist: ${e.message}", e)
            throw e
        }
    }

    fun getPlaylistsByUser(userId: String): Flow<List<Playlist>> {
        // We'll use local DB for this to support offline mode
        return playlistDao.getPlaylistsByUser(userId)
    }

    fun getPlaylistWithMusic(playlistId: Long): Flow<PlaylistWithMusic?> {
        // We'll use local DB for this to support offline mode
        return playlistDao.getPlaylistWithMusic(playlistId)
    }

    suspend fun addMusicToPlaylist(playlistId: Long, musicId: String) {
        val playlist = playlistDao.getPlaylistById(playlistId)
        val music = musicDao.getMusicById(musicId)

        if (playlist != null && music != null) {
            // Try API first
            try {
                val response = apiService.addSongToPlaylist(
                    playlistId,
                    AddSongRequest(musicId)
                )

                if (response.isSuccessful) {
                    // Add to local DB as well
                    val crossRef = PlaylistMusicCrossRef(
                        playlistId = playlistId,
                        musicId = musicId
                    )
                    playlistDao.insertPlaylistMusicCrossRef(crossRef)
                    return
                }
            } catch (e: Exception) {
                Log.e("PlaylistRepository", "API addMusicToPlaylist failed: ${e.message}", e)
            }

            // Fall back to local
            val crossRef = PlaylistMusicCrossRef(
                playlistId = playlistId,
                musicId = musicId
            )
            playlistDao.insertPlaylistMusicCrossRef(crossRef)
        } else {
            Log.e("PlaylistRepository", "Playlist or Music not found")
        }
    }

    suspend fun removeMusicFromPlaylist(playlistId: Long, musicId: String) {
        // Try API first
        try {
            val response = apiService.removeSongFromPlaylist(playlistId, musicId)

            if (response.isSuccessful) {
                // Remove from local DB as well
                playlistDao.deletePlaylistMusicCrossRef(playlistId, musicId)
                return
            }
        } catch (e: Exception) {
            Log.e("PlaylistRepository", "API removeMusicFromPlaylist failed: ${e.message}", e)
        }

        // Fall back to local
        playlistDao.deletePlaylistMusicCrossRef(playlistId, musicId)
    }

    suspend fun getPlaylistById(playlistId: Long): Playlist? {
        // Try API first
        try {
            val response = apiService.getPlaylistById(playlistId)

            if (response.isSuccessful) {
                val playlist = response.body()
                if (playlist != null) {
                    // Update local DB
                    playlistDao.insertPlaylist(playlist)
                    return playlist
                }
            }
        } catch (e: Exception) {
            Log.e("PlaylistRepository", "API getPlaylistById failed: ${e.message}", e)
        }

        // Fall back to local
        return playlistDao.getPlaylistById(playlistId)
    }

    suspend fun getPlaylistMusicCount(playlistId: Long): Int {
        return playlistDao.getPlaylistMusicCount(playlistId)
    }

}
