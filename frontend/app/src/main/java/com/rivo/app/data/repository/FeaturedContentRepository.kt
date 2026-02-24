package com.rivo.app.data.repository

import android.util.Log
import com.rivo.app.data.model.FeaturedContent
import com.rivo.app.data.model.FeaturedType
import com.rivo.app.data.remote.ApiService
import com.rivo.app.data.remote.FeaturedContentRequest
import com.rivo.app.data.remote.FeaturedContentResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeaturedContentRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    private val _featuredContent = MutableStateFlow<List<FeaturedContent>>(emptyList())
    val featuredContent: StateFlow<List<FeaturedContent>> = _featuredContent.asStateFlow()

    fun getAllFeaturedContent(): Flow<List<FeaturedContent>> {
        refreshFeaturedContentBackground()
        return featuredContent
    }

    private fun refreshFeaturedContentBackground() {
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            refreshFeaturedContent()
        }
    }

    fun getAllActiveFeaturedContent(): Flow<List<FeaturedContent>> = featuredContent

    fun getFeaturedContentByType(type: FeaturedType): Flow<List<FeaturedContent>> {
        return featuredContent.map { list -> list.filter { it.type == type } }
    }

    suspend fun getLatestFeaturedBanner(type: FeaturedType): FeaturedContent? = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFeaturedContent()
            if (response.isSuccessful) {
                response.body()?.filter { it.type == type.name }?.map { it.toModel() }?.maxByOrNull { it.position }
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun refreshFeaturedContent(): Result<List<FeaturedContent>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFeaturedContent()
            if (response.isSuccessful && response.body() != null) {
                val featuredList = response.body()!!.map { it.toModel() }
                _featuredContent.value = featuredList
                return@withContext Result.success(featuredList)
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
                refreshFeaturedContent()
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
                refreshFeaturedContent()
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
            createdBy = "ADMIN",
            featuredBy = "ADMIN",
            position = this.order
        )
    }
}
