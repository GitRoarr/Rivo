package com.rivo.app.data.repository

import android.util.Log
import com.rivo.app.data.model.User
import com.rivo.app.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun followArtist(artistId: String): Result<Unit> {
        return try {
            val response = apiService.followUser(artistId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to follow artist"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unfollowArtist(artistId: String): Result<Unit> {
        return try {
            val response = apiService.unfollowUser(artistId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to unfollow artist"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isFollowing(artistId: String): Boolean {
        return try {
            val response = apiService.checkFollowStatus(artistId)
            if (response.isSuccessful) {
                response.body()?.isFollowing ?: false
            } else false
        } catch (e: Exception) {
            Log.e("FollowRepository", "Error checking follow status: ${e.message}")
            false
        }
    }

    suspend fun getFollowersCount(artistId: String): Int {
        return try {
            val response = apiService.getUserFollowers(artistId)
            if (response.isSuccessful) response.body()?.size ?: 0 else 0
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getFollowingCount(userId: String): Int {
        return try {
            val response = apiService.getUserFollowing(userId)
            if (response.isSuccessful) response.body()?.size ?: 0 else 0
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getFollowers(artistId: String): List<User> {
        return try {
            val response = apiService.getUserFollowers(artistId)
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFollowing(userId: String): List<User> {
        return try {
            val response = apiService.getUserFollowing(userId)
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getListenerTotalPlays(userId: String): Int {
        return try {
            val response = apiService.getListenerStats(userId)
            if (response.isSuccessful) response.body()?.totalPlays ?: 0 else 0
        } catch (e: Exception) {
            Log.e("FollowRepository", "Error getting listener stats: ${e.message}")
            0
        }
    }
}
