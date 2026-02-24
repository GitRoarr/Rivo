package com.rivo.app.data.repository

import android.util.Log
import com.rivo.app.data.model.User
import com.rivo.app.data.model.UserType
import com.rivo.app.data.model.VerificationStatus
import com.rivo.app.data.remote.ApiService
import com.rivo.app.data.remote.LoginRequest
import com.rivo.app.data.remote.UserRegistrationRequest
import com.rivo.app.data.remote.UserUpdateRequest
import com.rivo.app.data.remote.VerificationUpdateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val sessionManager: SessionManager,
    private val apiService: ApiService,
    private val connectivityRepository: ConnectivityRepository
) {
    private val _allUsers = kotlinx.coroutines.flow.MutableStateFlow<List<User>>(emptyList())
    val allUsers: kotlinx.coroutines.flow.StateFlow<List<User>> = _allUsers.asStateFlow()

    private val _pendingVerificationUsers = kotlinx.coroutines.flow.MutableStateFlow<List<User>>(emptyList())
    val pendingVerificationUsers: kotlinx.coroutines.flow.StateFlow<List<User>> = _pendingVerificationUsers.asStateFlow()

    suspend fun registerUser(
        fullName: String,
        name: String,
        email: String,
        password: String,
        userType: UserType
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.registerUser(
                UserRegistrationRequest(
                    id = generateUserId(),
                    email = email,
                    password = password,
                    name = name,
                    fullName = fullName,
                    userType = userType.name
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val userResponse = response.body()!!
                val user = User(
                    id = userResponse.id,
                    email = email,
                    password = hashPassword(password),
                    name = name,
                    fullName = fullName,
                    userType = userType,
                    verificationStatus = VerificationStatus.UNVERIFIED,
                    socialLinks = emptyMap()
                )
                sessionManager.saveToken(userResponse.token)
                sessionManager.createSession(user)
                return@withContext Result.success(user)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Registration failed"
                return@withContext Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.loginUser(
                LoginRequest(email = email, password = password)
            )

            if (response.isSuccessful && response.body() != null) {
                val userResponse = response.body()!!
                sessionManager.saveToken(userResponse.token)
                val user = User(
                    id = userResponse.id,
                    email = email,
                    password = hashPassword(password),
                    name = userResponse.name,
                    fullName = userResponse.fullName,
                    userType = UserType.valueOf(userResponse.userType),
                    verificationStatus = VerificationStatus.UNVERIFIED,
                    socialLinks = emptyMap()
                )
                sessionManager.createSession(user)
                return@withContext Result.success(user)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Login failed"
                return@withContext Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun deleteAccount(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteUser(userId)
            if (response.isSuccessful) {
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Failed to delete user"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    fun getAllUsers(): Flow<List<User>> {
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            refreshAllUsers()
        }
        return allUsers
    }

    suspend fun refreshAllUsers(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllUsers()
            if (response.isSuccessful && response.body() != null) {
                _allUsers.value = response.body()!!
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Failed to refresh users"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun refreshPendingVerifications(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUsersAwaitingVerification()
            if (response.isSuccessful && response.body() != null) {
                // Store pending verifications separately — do NOT overwrite _allUsers
                _pendingVerificationUsers.value = response.body()!!
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Failed to refresh pending verifications"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    fun getArtists(): Flow<List<User>> {
        val flow = kotlinx.coroutines.flow.MutableStateFlow<List<User>>(emptyList())
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getExploreData()
                if (response.isSuccessful) {
                    flow.value = response.body()?.featuredArtists ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Failed to fetch artists: ${e.message}")
            }
        }
        return flow
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            val response = apiService.getUserById(userId)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun promoteUserToArtist(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateUserType(userId, "ARTIST")
            if (response.isSuccessful) {
                refreshAllUsers()
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Failed to promote user"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun promoteUserToAdmin(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateUserType(userId, "ADMIN")
            if (response.isSuccessful) {
                refreshAllUsers()
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Failed to promote user"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun updateUserProfile(
        email: String,
        fullName: String? = null,
        bio: String? = null,
        location: String? = null,
        website: String? = null,
        profileImagePath: String? = null,
        coverImagePath: String? = null
    ): Result<User> = withContext(Dispatchers.IO) {
        return@withContext try {
            val profilePart = profileImagePath?.let { path ->
                val file = File(path.replace("file://", ""))
                if (file.exists()) {
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("profileImage", file.name, requestFile)
                } else null
            }

            val coverPart = coverImagePath?.let { path ->
                val file = File(path.replace("file://", ""))
                if (file.exists()) {
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("coverImage", file.name, requestFile)
                } else null
            }

            val fullNamePart = fullName?.toRequestBody("text/plain".toMediaTypeOrNull())
            val bioPart = bio?.toRequestBody("text/plain".toMediaTypeOrNull())
            val locationPart = location?.toRequestBody("text/plain".toMediaTypeOrNull())
            val websitePart = website?.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.updateUserProfile(
                profileImage = profilePart,
                coverImage = coverPart,
                fullName = fullNamePart,
                bio = bioPart,
                location = locationPart,
                website = websitePart
            )

            if (response.isSuccessful && response.body() != null) {
                val updatedUser = response.body()!!
                sessionManager.createSession(updatedUser)
                Result.success(updatedUser)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Update failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserByEmail(email: String): User? {
        return try {
            val response = apiService.getUserByEmail(email)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserPassword(email: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.resetPassword(email, newPassword)
            if (response.isSuccessful) {
                return@withContext Result.success(Unit)
            }
            Result.failure(Exception("Failed to update password"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfileImage(email: String, imageUrl: String): Result<Unit> {
        return try {
            val result = updateUserProfile(email, profileImagePath = imageUrl)
            if (result.isSuccess) Result.success(Unit) else Result.failure(result.exceptionOrNull()!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCoverImage(email: String, imageUrl: String): Result<Unit> {
        return try {
            val result = updateUserProfile(email, coverImagePath = imageUrl)
            if (result.isSuccess) Result.success(Unit) else Result.failure(result.exceptionOrNull()!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveArtist(artistId: String, approved: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.approveArtist(artistId, approved)
            if (response.isSuccessful) {
                refreshAllUsers()
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Failed to approve artist"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun suspendUser(userId: String, isSuspended: Boolean = true): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.suspendUser(userId, isSuspended)
            if (response.isSuccessful) {
                refreshAllUsers()
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Failed to suspend user"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun generateUserId(): String {
        return java.util.UUID.randomUUID().toString()
    }

    fun getUsersAwaitingVerification(): Flow<List<User>> {
        val flow = kotlinx.coroutines.flow.MutableStateFlow<List<User>>(emptyList())
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getUsersAwaitingVerification()
                if (response.isSuccessful) {
                    flow.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Failed to fetch verifications: ${e.message}")
            }
        }
        return flow
    }

    suspend fun approveUserVerification(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateVerificationStatus(userId, VerificationUpdateRequest(status = "VERIFIED"))
            if (response.isSuccessful) {
                refreshAllUsers()
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Failed to approve verification"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun rejectUserVerification(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateVerificationStatus(userId, VerificationUpdateRequest(status = "REJECTED"))
            if (response.isSuccessful) {
                refreshAllUsers()
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Failed to reject verification"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun followUser(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.followUser(userId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to follow user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unfollowUser(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.unfollowUser(userId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to unfollow user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserFollowers(userId: String): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUserFollowers(userId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to get followers"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserFollowing(userId: String): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUserFollowing(userId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to get following"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
