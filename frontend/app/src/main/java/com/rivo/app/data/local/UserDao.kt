package com.rivo.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rivo.app.data.model.ArtistDocument
import com.rivo.app.data.model.User
import com.rivo.app.data.model.UserType
import com.rivo.app.data.model.VerificationRequest
import com.rivo.app.data.model.VerificationStatus
import kotlinx.coroutines.flow.Flow
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUsers(users: List<User>)

    @Delete
    suspend fun deleteUser(user: User)
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)



    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE userType = :userType")
    fun getUsersByType(userType: UserType): Flow<List<User>>

    @Query("SELECT * FROM users WHERE verificationStatus = :status")
    fun getUsersByVerificationStatus(status: VerificationStatus): Flow<List<User>>

    @Query("UPDATE users SET isApproved = :approved WHERE id = :userId")
    suspend fun updateArtistApprovalStatus(userId: String, approved: Boolean)

    @Query("UPDATE users SET profileImageUrl = :imageUrl WHERE email = :email")
    suspend fun updateProfileImage(email: String, imageUrl: String)

    @Query("UPDATE users SET coverImageUrl = :imageUrl WHERE email = :email")
    suspend fun updateCoverImage(email: String, imageUrl: String)

    @Query("UPDATE users SET isApproved = :approved WHERE email = :email")
    suspend fun updateApprovalStatus(email: String, approved: Boolean)

    @Query("UPDATE users SET isSuspended = :isSuspended WHERE id = :userId")
    suspend fun updateUserSuspension(userId: String, isSuspended: Boolean)

    @Query("""
        SELECT * 
        FROM users 
        WHERE userType = :userType 
          AND (LOWER(fullName) LIKE '%' || LOWER(:query) || '%' 
               OR LOWER(email) LIKE '%' || LOWER(:query) || '%')
    """)
    fun searchUsersByType(query: String, userType: UserType): Flow<List<User>>

    @Insert
    suspend fun insertVerificationRequest(request: VerificationRequest)

    @Query("SELECT * FROM verification_requests WHERE artistId = :artistId LIMIT 1")
    suspend fun getVerificationRequestByArtistId(artistId: String): VerificationRequest?

    @Query("SELECT * FROM verification_requests WHERE status = :status")
    fun getVerificationRequestsByStatus(status: VerificationStatus): Flow<List<VerificationRequest>>

    @Query("SELECT * FROM verification_requests")
    fun getVerificationRequestsWithDetails(): Flow<List<VerificationRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtistDocument(artistDocument: ArtistDocument)

    @Query("UPDATE users SET userType = :newType WHERE id = :userId")
    suspend fun updateUserType(userId: String, newType: String)

}
