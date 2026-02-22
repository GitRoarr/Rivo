package com.rivo.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rivo.app.data.model.FeaturedContent
import com.rivo.app.data.model.FeaturedType
import kotlinx.coroutines.flow.Flow

@Dao
interface FeaturedContentDao {
    @Query("SELECT * FROM featured_content ORDER BY position ASC")
    fun getAllFeaturedContent(): Flow<List<FeaturedContent>>

    @Query("SELECT * FROM featured_content WHERE isActive = 1 ORDER BY position ASC")
    fun getAllActiveFeaturedContent(): Flow<List<FeaturedContent>>

    @Query("SELECT * FROM featured_content WHERE type = :type ORDER BY position ASC")
    fun getFeaturedContentByType(type: FeaturedType): Flow<List<FeaturedContent>>

    @Query("SELECT * FROM featured_content WHERE contentId = :contentId AND type = :type LIMIT 1")
    suspend fun getFeaturedContentByContentId(contentId: String, type: FeaturedType): FeaturedContent?

    @Query("SELECT * FROM featured_content WHERE type = :type ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestFeaturedContentByType(type: FeaturedType): FeaturedContent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeaturedContent(featuredContent: FeaturedContent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFeaturedContent(featuredContentList: List<FeaturedContent>)

    @Update
    suspend fun updateFeaturedContent(featuredContent: FeaturedContent)

    @Query("DELETE FROM featured_content WHERE id = :id")
    suspend fun deleteFeaturedContentById(id: String)
}