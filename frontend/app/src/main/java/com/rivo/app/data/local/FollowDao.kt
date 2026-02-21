package com.rivo.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rivo.app.data.model.Follow

@Dao
interface FollowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun follow(follow: Follow)
    
    @Delete
    suspend fun unfollow(follow: Follow)

    @Query("DELETE FROM follows WHERE followerId = :userId AND followingId = :artistId")
    suspend fun unfollowByIds(userId: String, artistId: String)
    
    @Query("SELECT COUNT(*) FROM follows WHERE followingId = :artistId")
    suspend fun getFollowersCount(artistId: String): Int
    
    @Query("SELECT COUNT(*) FROM follows WHERE followerId = :userId")
    suspend fun getFollowingCount(userId: String): Int
    
    @Query("SELECT EXISTS(SELECT 1 FROM follows WHERE followerId = :userId AND followingId = :artistId)")
    suspend fun isFollowing(userId: String, artistId: String): Boolean
    
    @Query("SELECT * FROM follows WHERE followerId = :userId")
    suspend fun getFollowing(userId: String): List<Follow>
    
    @Query("SELECT * FROM follows WHERE followingId = :artistId")
    suspend fun getFollowers(artistId: String): List<Follow>
}
