package com.rivo.app.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.MusicApprovalStatus
import kotlinx.coroutines.flow.Flow

data class FeaturedArtist(
    val artistId: String,
    val artist: String
)

@Dao
interface MusicDao {

    @Query("SELECT * FROM music ORDER BY title ASC")
    fun getAllMusic(): Flow<List<Music>>

    @Query("SELECT * FROM music ORDER BY title ASC")
    fun getAllMusicPaged(): PagingSource<Int, Music>

    @Query("SELECT * FROM music WHERE isFavorite = 1 AND userId = :userId ORDER BY title ASC")
    fun getFavoriteMusic(userId: String): Flow<List<Music>>

    @Query("SELECT * FROM music WHERE id = :musicId")
    suspend fun getMusicById(musicId: String): Music?

    @Query("SELECT * FROM music WHERE artistId = :artistId ORDER BY title ASC")
    fun getArtistMusic(artistId: String): Flow<List<Music>>

    @Query("SELECT * FROM music WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchMusic(query: String): Flow<List<Music>>

    @Query("UPDATE music SET isFavorite = :isFavorite WHERE id = :musicId")
    suspend fun updateFavoriteStatus(musicId: String, isFavorite: Boolean)

    @Query("UPDATE music SET isFavorite = 0 WHERE userId = :userId AND isFavorite = 1")
    suspend fun clearAllFavoritesForUser(userId: String)

    @Query("UPDATE music SET isFavorite = 1, userId = :userId WHERE id = :musicId")
    suspend fun markAsFavoriteForUser(musicId: String, userId: String)

    @Query("UPDATE music SET playCount = playCount + 1 WHERE id = :musicId")
    suspend fun incrementPlayCount(musicId: String)

    @Query("SELECT * FROM music ORDER BY uploadDate DESC LIMIT 20")
    fun getNewReleases(): Flow<List<Music>>

    @Query("SELECT * FROM music ORDER BY playCount DESC LIMIT 20")
    fun getTrendingMusic(): Flow<List<Music>>

    @Query("SELECT DISTINCT artistId, artist FROM music GROUP BY artistId ORDER BY COUNT(id) DESC LIMIT 10")
    fun getFeaturedArtists(): Flow<List<FeaturedArtist>>  // Updated

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMusic(music: Music)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMusic(musicList: List<Music>)

    @Delete
    suspend fun deleteMusic(music: Music)

    @Query("SELECT * FROM music WHERE approvalStatus = :status")
    fun getMusicByApprovalStatus(status: MusicApprovalStatus): Flow<List<Music>>
}
