package com.rivo.app.data.repository

import android.util.Log
import com.rivo.app.data.local.FollowDao
import com.rivo.app.data.model.Follow
import com.rivo.app.data.model.User
import com.rivo.app.data.remote.ApiService
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowRepository @Inject constructor(
    private val followDao: FollowDao,
    private val apiService: ApiService
) {
    suspend fun syncFollows(userId: String) {
        try {
            Log.d("FollowRepository", "Syncing follows for user: $userId")
            val response = apiService.getUserFollowing(userId)
            if (response.isSuccessful) {
                val following = response.body() ?: emptyList()
                Log.d("FollowRepository", "Fetched ${following.size} following from backend")
                for (artist in following) {
                    if (!followDao.isFollowing(userId, artist.id)) {
                        followDao.follow(Follow(followerId = userId, followingId = artist.id, createdAt = Date()))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FollowRepository", "Error syncing follows: ${e.message}")
        }
    }
    suspend fun followArtist(userId: String, artistId: String) {
        try {
            val response = apiService.followUser(artistId)
            if (response.isSuccessful) {
                val follow = Follow(
                    followerId = userId,
                    followingId = artistId,
                    createdAt = Date()
                )
                followDao.follow(follow)
            } else {
                throw Exception(response.errorBody()?.string() ?: "Failed to follow artist")
            }
        } catch (e: Exception) {
            // Only backend-approved follows are stored locally
            throw e
        }
    }

    suspend fun unfollowArtist(userId: String, artistId: String) {
        try {
            val response = apiService.unfollowUser(artistId)
            if (response.isSuccessful) {
                followDao.unfollowByIds(userId, artistId)
            } else {
                throw Exception(response.errorBody()?.string() ?: "Failed to unfollow artist")
            }
        } catch (e: Exception) {
            // Do not mutate local state if backend unfollow fails
            throw e
        }
    }

    suspend fun isFollowing(userId: String, artistId: String): Boolean {
        return followDao.isFollowing(userId, artistId)
    }

    suspend fun getFollowersCount(artistId: String): Int {
        try {
            val response = apiService.getUserFollowers(artistId)
            if (response.isSuccessful) {
                return response.body()?.size ?: 0
            }
        } catch (e: Exception) {}
        return followDao.getFollowersCount(artistId)
    }

    suspend fun getFollowingCount(userId: String): Int {
        try {
            val response = apiService.getUserFollowing(userId)
            if (response.isSuccessful) {
                return response.body()?.size ?: 0
            }
        } catch (e: Exception) {
            // Ignore and fall back to local cache
        }
        return followDao.getFollowingCount(userId)
    }

    suspend fun getFollowers(artistId: String): List<User> {
        try {
            val response = apiService.getUserFollowers(artistId)
            if (response.isSuccessful) {
                return response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            // Ignore and fall back to local cache
        }

        // Fallback: use local follows and fetch user details from backend
        val follows = followDao.getFollowers(artistId)
        val users = mutableListOf<User>()
        for (follow in follows) {
            try {
                val userResponse = apiService.getUserById(follow.followerId)
                if (userResponse.isSuccessful) {
                    userResponse.body()?.let { users.add(it) }
                }
            } catch (_: Exception) {
            }
        }
        return users
    }

    suspend fun getFollowing(userId: String): List<User> {
        try {
            val response = apiService.getUserFollowing(userId)
            if (response.isSuccessful) {
                return response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            // Ignore and fall back to local cache
        }

        // Fallback: use local follows and fetch user details from backend
        val follows = followDao.getFollowing(userId)
        val users = mutableListOf<User>()
        for (follow in follows) {
            try {
                val userResponse = apiService.getUserById(follow.followingId)
                if (userResponse.isSuccessful) {
                    userResponse.body()?.let { users.add(it) }
                }
            } catch (_: Exception) {
            }
        }
        return users
    }
}
