package com.rivo.app.data.repository

import com.rivo.app.data.local.FollowDao
import com.rivo.app.data.local.UserDao
import com.rivo.app.data.model.Follow
import com.rivo.app.data.model.User
import com.rivo.app.data.remote.ApiService
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class FollowRepository @Inject constructor(
    private val followDao: FollowDao,
    private val userDao: UserDao,
    private val apiService: ApiService
) {
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
            }
        } catch (e: Exception) {
            // Log or handle error, maybe still save locally?
            val follow = Follow(
                followerId = userId,
                followingId = artistId,
                createdAt = Date()
            )
            followDao.follow(follow)
        }
    }

    suspend fun unfollowArtist(userId: String, artistId: String) {
        try {
            val response = apiService.unfollowUser(artistId)
            if (response.isSuccessful) {
                followDao.unfollowByIds(userId, artistId)
            }
        } catch (e: Exception) {
            followDao.unfollowByIds(userId, artistId)
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
        return followDao.getFollowingCount(userId)
    }

    suspend fun getFollowers(artistId: String): List<User> {
        val follows = followDao.getFollowers(artistId)
        return follows.mapNotNull { follow ->
            userDao.getUserByEmail(follow.followerId)
        }
    }

    suspend fun getFollowing(userId: String): List<User> {
        val follows = followDao.getFollowing(userId)
        return follows.mapNotNull { follow ->
            userDao.getUserByEmail(follow.followingId)
        }
    }
}
