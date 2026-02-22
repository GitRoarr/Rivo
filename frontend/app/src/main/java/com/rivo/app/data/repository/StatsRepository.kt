package com.rivo.app.data.repository

import com.rivo.app.data.remote.AdminStatsResponse
import com.rivo.app.data.remote.ApiService
import com.rivo.app.data.remote.ArtistStatsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getArtistStats(): Result<ArtistStatsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getArtistStats()
            if (response.isSuccessful && response.body() != null) {
                kotlin.Result.success(response.body()!!)
            } else {
                kotlin.Result.failure(Exception("Failed to fetch artist stats"))
            }
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }

    suspend fun getAdminStats(): Result<AdminStatsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAdminStats()
            if (response.isSuccessful && response.body() != null) {
                kotlin.Result.success(response.body()!!)
            } else {
                kotlin.Result.failure(Exception("Failed to fetch admin stats"))
            }
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }
}
