package com.rivo.app.data.repository

import com.rivo.app.data.local.FeaturedContentDao
import com.rivo.app.data.model.FeaturedContent
import com.rivo.app.data.model.FeaturedType
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.User
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeaturedContentRepository @Inject constructor(
    private val featuredContentDao: FeaturedContentDao
) {

    fun getAllFeaturedContent(): Flow<List<FeaturedContent>> =
        featuredContentDao.getAllFeaturedContent()

    fun getAllActiveFeaturedContent(): Flow<List<FeaturedContent>> =
        featuredContentDao.getAllActiveFeaturedContent()

    fun getFeaturedContentByType(type: FeaturedType): Flow<List<FeaturedContent>> =
        featuredContentDao.getFeaturedContentByType(type)

    suspend fun getLatestFeaturedBanner(type: FeaturedType): FeaturedContent? =
        featuredContentDao.getLatestFeaturedContentByType(type)

    suspend fun featureContent(
        contentId: String,
        type: FeaturedType,
        title: String,
        description: String? = null,
        imageUrl: String? = null,
        createdBy: String,
        position: Int = 0
    ): String {
        val id = UUID.randomUUID().toString()
        val featuredContent = FeaturedContent(
            id = id,
            contentId = contentId,
            type = type,
            title = title,
            description = description,
            imageUrl = imageUrl,
            createdBy = createdBy,
            featuredBy = createdBy,
            position = position
        )
        featuredContentDao.insertFeaturedContent(featuredContent)
        return id
    }

    suspend fun insertFeaturedContent(featuredContent: FeaturedContent) {
        featuredContentDao.insertFeaturedContent(featuredContent)
    }

    suspend fun updateFeaturedContent(featuredContent: FeaturedContent) {
        featuredContentDao.updateFeaturedContent(featuredContent)
    }

    suspend fun removeFeaturedContent(id: String) {
        featuredContentDao.deleteFeaturedContentById(id)
    }

    suspend fun isContentFeatured(contentId: String, type: FeaturedType): Boolean =
        featuredContentDao.getFeaturedContentByContentId(contentId, type) != null
    // Method to get featured songs from DAO
    fun getFeaturedSongs(): Flow<List<FeaturedContent>> {
        return featuredContentDao.getFeaturedContentByType(FeaturedType.SONG)
    }

    // Method to get featured artists from DAO
    fun getFeaturedArtists(): Flow<List<FeaturedContent>> {
        return featuredContentDao.getFeaturedContentByType(FeaturedType.ARTIST)
    }
}
