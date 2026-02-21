package com.rivo.app.data.local

import androidx.room.*
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.Playlist
import com.rivo.app.data.model.PlaylistMusicCrossRef
import kotlinx.coroutines.flow.Flow

@Suppress("AndroidUnresolvedRoomSqlReference")
@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Query("SELECT * FROM playlists WHERE createdBy = :userId")  // Use createdBy instead of userId
    fun getPlaylistsByUser(userId: String): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): Playlist?

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistWithMusic(playlistId: Long): Flow<PlaylistWithMusic>

    @Query("SELECT COUNT(*) FROM playlist_music_cross_ref WHERE playlistId = :playlistId")
    suspend fun getPlaylistMusicCount(playlistId: Long): Int

    @Query("SELECT * FROM playlists WHERE name LIKE '%' || :query || '%'")
    fun searchPlaylists(query: String): Flow<List<Playlist>>

    // For adding and removing music from a playlist
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistMusicCrossRef(crossRef: PlaylistMusicCrossRef)

    @Query("DELETE FROM playlist_music_cross_ref WHERE playlistId = :playlistId AND musicId = :musicId")
    suspend fun deletePlaylistMusicCrossRef(playlistId: Long, musicId: String)
}

data class PlaylistWithMusic(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "id",
        entityColumn = "playlistId"
    )
    val musicList: List<Music>
)
