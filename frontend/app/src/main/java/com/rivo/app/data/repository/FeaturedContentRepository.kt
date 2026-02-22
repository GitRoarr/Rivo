package com.rivo.app.data.repository

import android.util.Log
import com.rivo.app.data.local.FeaturedContentDao
import com.rivo.app.data.model.FeaturedContent
import com.rivo.app.data.model.FeaturedType
import com.rivo.app.data.remote.ApiService
import com.rivo.app.data.remote.FeaturedContentRequest
import com.rivo.app.data.remote.FeaturedContentResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeaturedContentRepository @Inject constructor(
    private val featuredContentDao: FeaturedContentDao,
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    fun getAllFeaturedContent(): Flow<List<FeaturedContent>> =
        featuredContentDao.getAllFeaturedContent()

    fun getAllActiveFeaturedContent(): Flow<List<FeaturedContent>> =
        featuredContentDao.getAllActiveFeaturedContent()

    fun getFeaturedContentByType(type: FeaturedType): Flow<List<FeaturedContent>> =
        featuredContentDao.getFeaturedContentByType(type)

    suspend fun getLatestFeaturedBanner(type: FeaturedType): FeaturedContent? = withContext(Dispatchers.IO) {
        featuredContentDao.getLatestFeaturedContentByType(type)
    }

    suspend fun refreshFeaturedContent(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFeaturedContent()
            if (response.isSuccessful && response.body() != null) {
                val featuredList = response.body()!!.map { it.toModel() }
                featuredContentDao.insertAllFeaturedContent(featuredList)
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Failed to refresh featured content"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun createFeaturedContent(
        title: String,
        description: String? = null,
        type: FeaturedType,
        contentId: String? = null,
        imageUrl: String? = null,
        actionUrl: String? = null,
        order: Int = 0
    ): Result<FeaturedContent> = withContext(Dispatchers.IO) {
        try {
            val request = FeaturedContentRequest(
                type = type.name,
                contentId = contentId,
                title = title,
                description = description,
                imageUrl = imageUrl,
                actionUrl = actionUrl,
                order = order
            )
            val response = apiService.addFeaturedContent(request)
            if (response.isSuccessful && response.body() != null) {
                val featured = response.body()!!.toModel()
                featuredContentDao.insertFeaturedContent(featured)
                return@withContext Result.success(featured)
            } else {
                return@withContext Result.failure(Exception("Failed to create featured content"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun removeFeaturedContent(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteFeaturedContent(id)
            if (response.isSuccessful) {
                featuredContentDao.deleteFeaturedContentById(id)
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Failed to remove featured content"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    private fun FeaturedContentResponse.toModel(): FeaturedContent {
        return FeaturedContent(
            id = this.id,
            type = FeaturedType.valueOf(this.type),
            contentId = this.contentId,
            title = this.title,
            description = this.description,
            imageUrl = this.imageUrl,
            createdBy = "ADMIN", // Default for now
            featuredBy = "ADMIN",
            position = this.order
        )
    }

    suspend fun insertFeaturedContent(featuredContent: FeaturedContent) {
        featuredContentDao.insertFeaturedContent(featuredContent)
    }

    suspend fun removeFeaturedContentLocally(id: String) {
        featuredContentDao.deleteFeaturedContentById(id)
    }
}
