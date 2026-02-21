package com.rivo.app.data.local

import androidx.room.*
import com.rivo.app.data.model.ArtistAnalytics
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistStatsDao {

    @Query("SELECT * FROM artist_analytics WHERE artistId = :artistId")
    suspend fun getArtistAnalytics(artistId: String): ArtistAnalytics?

    @Query("SELECT * FROM artist_analytics WHERE artistId = :artistId")
    fun getArtistAnalyticsFlow(artistId: String): Flow<ArtistAnalytics?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtistAnalytics(analytics: ArtistAnalytics)

    @Update
    suspend fun updateArtistAnalytics(analytics: ArtistAnalytics)

    @Query("SELECT COUNT(*) FROM artist_analytics")
    suspend fun getAnalyticsCount(): Int

    @Query("SELECT * FROM artist_analytics")
    suspend fun getAllArtistAnalytics(): List<ArtistAnalytics>
}
