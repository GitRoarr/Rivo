package com.rivo.app.data.local

import androidx.room.*
import com.rivo.app.data.model.Watchlist
import com.rivo.app.data.model.WatchlistMusicCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist WHERE createdBy = :userId")
    fun getWatchlistsByUser(userId: String): Flow<List<Watchlist>>

    @Query("SELECT * FROM watchlist WHERE createdBy = :userId")
    suspend fun getWatchlistsByUserSync(userId: String): List<Watchlist>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(watchlist: Watchlist): Long

    @Transaction
    @Query("SELECT * FROM watchlist WHERE id = :watchlistId")
    fun getWatchlistWithMusic(watchlistId: Long): Flow<WatchlistWithMusic>

    @Update
    suspend fun updateWatchlist(watchlist: Watchlist)

    @Delete
    suspend fun deleteWatchlist(watchlist: Watchlist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMusicToWatchlist(crossRef: WatchlistMusicCrossRef)

    @Delete
    suspend fun removeMusicFromWatchlist(crossRef: WatchlistMusicCrossRef)

    @Transaction
    @Query("SELECT * FROM watchlist WHERE createdBy = :userId")
    fun getAllWatchlistsWithMusicForUser(userId: String): Flow<List<WatchlistWithMusic>>

    @Query("SELECT COUNT(*) FROM watchlist_music_cross_ref WHERE watchlistId = :watchlistId AND musicId = :musicId")
    suspend fun countMusicInWatchlist(watchlistId: Long, musicId: String): Int

    @Query("DELETE FROM watchlist_music_cross_ref WHERE watchlistId = :watchlistId AND musicId = :musicId")
    suspend fun deleteWatchlistMusicCrossRefByIds(watchlistId: Long, musicId: String)
}
