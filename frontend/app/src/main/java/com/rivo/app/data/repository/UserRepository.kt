package com.rivo.app.data.repository

import android.util.Log
import com.rivo.app.data.local.UserDao
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val sessionManager: SessionManager,
    private val apiService: ApiService,
    private val connectivityRepository: ConnectivityRepository
) {

    suspend fun registerUser(
        fullName: String,
        name: String,
        email: String,
        password: String,
        userType: UserType
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            if (isUserExists(email)) {
                Log.d("UserRepository", "User with email $email already exists")
                return@withContext Result.failure(Exception("User with this email already exists"))
            }

            val hashedPassword = hashPassword(password)
            val userId = generateUserId()

            val isConnected = connectivityRepository.isNetworkAvailable.value
            Log.d("UserRepository", "Network available for registration: $isConnected")

            if (isConnected) {
                try {
                    Log.d("UserRepository", "Attempting API registration for $email")
                    val response = apiService.registerUser(
                        UserRegistrationRequest(
                            id = userId,
                            email = email,
                            password = password,
                            name = name,
                            fullName = fullName,
                            userType = userType.name
                        )
                    )

                    if (response.isSuccessful) {
                        val userResponse = response.body()
                        if (userResponse != null) {
                            Log.d("UserRepository", "API registration successful for $email")

                            sessionManager.saveToken(userResponse.token)

                            val user = User(
                                id = userResponse.id,
                                email = email,
                                password = hashedPassword,
                                name = name,
                                fullName = fullName,
                                userType = userType,
                                verificationStatus = VerificationStatus.UNVERIFIED,
                                socialLinks = emptyMap()
                            )
                            userDao.insertUser(user)
                            return@withContext Result.success(user)
                        } else {
                            Log.e("UserRepository", "API registration response body is null")
                            return@withContext Result.failure(Exception("Failed to parse user response"))
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("UserRepository", "API registration failed: $errorBody")

                        return@withContext fallbackToLocalRegistration(userId, email, hashedPassword, name, fullName, userType)
                    }
                } catch (e: Exception) {
                    Log.e("UserRepository", "API registration exception: ${e.message}", e)
                    return@withContext fallbackToLocalRegistration(userId, email, hashedPassword, name, fullName, userType)
                }
            } else {
                Log.d("UserRepository", "No network, using local registration for $email")
                return@withContext fallbackToLocalRegistration(userId, email, hashedPassword, name, fullName, userType)
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Registration exception: ${e.message}", e)
            return@withContext Result.failure(e)
        }
    }

    private suspend fun fallbackToLocalRegistration(
        userId: String,
        email: String,
        hashedPassword: String,
        name: String,
        fullName: String,
        userType: UserType
    ): Result<User> {
        Log.d("UserRepository", "Falling back to local registration for $email")
        val user = User(
            id = userId,
            email = email,
            password = hashedPassword,
            name = name,
            fullName = fullName,
            userType = userType,
            verificationStatus = VerificationStatus.UNVERIFIED,
            socialLinks = emptyMap()
        )
        userDao.insertUser(user)
        return Result.success(user)
    }

    suspend fun loginUser(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val isConnected = connectivityRepository.isNetworkAvailable.value
            Log.d("UserRepository", "Network available for login: $isConnected")

            if (isConnected) {
                try {
                    Log.d("UserRepository", "Attempting API login for $email")
                    val response = apiService.loginUser(
                        LoginRequest(
                            email = email,
                            password = password
                        )
                    )

                    if (response.isSuccessful) {
                        val userResponse = response.body()
                        if (userResponse != null && userResponse.token != null) {
                            Log.d("UserRepository", "API login successful for $email")

                            sessionManager.saveToken(userResponse.token)

                            var user = userDao.getUserByEmail(email)
                            if (user == null) {
                                user = User(
                                    id = userResponse.id,
                                    email = email,
                                    password = hashPassword(password),
                                    name = userResponse.name,
                                    fullName = userResponse.fullName,
                                    userType = UserType.valueOf(userResponse.userType),
                                    verificationStatus = VerificationStatus.UNVERIFIED,
                                    socialLinks = emptyMap()
                                )
                                userDao.insertUser(user)
                            } else {
                                user = user.copy(
                                    id = userResponse.id,
                                    name = userResponse.name,
                                    fullName = userResponse.fullName,
                                    userType = UserType.valueOf(userResponse.userType)
                                )
                                userDao.insertUser(user)
                            }
                            return@withContext Result.success(user)
                        } else {
                            Log.e("UserRepository", "API login response body is null")
                            return@withContext Result.failure(Exception("Failed to parse user response"))
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("UserRepository", "API login failed: $errorBody")

                        return@withContext fallbackToLocalLogin(email, password)
                    }
                } catch (e: Exception) {
                    Log.e("UserRepository", "API login exception: ${e.message}", e)
                    return@withContext fallbackToLocalLogin(email, password)
                }
            } else {
                Log.d("UserRepository", "No network, using local login for $email")
                return@withContext fallbackToLocalLogin(email, password)
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Login exception: ${e.message}", e)
            return@withContext Result.failure(e)
        }
    }

    private suspend fun fallbackToLocalLogin(email: String, password: String): Result<User> {
        Log.d("UserRepository", "Falling back to local login for $email")
        val hashedPassword = hashPassword(password)
        val user = userDao.getUserByEmail(email)

        return if (user != null && user.password == hashedPassword) {
            Log.d("UserRepository", "Local login successful for $email")
            Result.success(user)
        } else {
            Log.e("UserRepository", "Local login failed for $email: Invalid credentials")
            Result.failure(Exception("Invalid credentials"))
        }
    }

    suspend fun deleteAccount(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val isConnected = connectivityRepository.isNetworkAvailable.value

            if (isConnected) {
                try {
                    Log.d("UserRepository", "Attempting API delete for user $userId")
                    val response = apiService.deleteUser(userId)

                    if (response.isSuccessful) {
                        Log.d("UserRepository", "API delete successful for user $userId")
                        userDao.deleteUserById(userId)
                        return@withContext Result.success(Unit)
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("UserRepository", "API delete failed: $errorBody")
                        userDao.deleteUserById(userId)
                        return@withContext Result.success(Unit)
                    }
                } catch (e: Exception) {
                    Log.e("UserRepository", "API delete exception: ${e.message}", e)
                    userDao.deleteUserById(userId)
                    return@withContext Result.success(Unit)
                }
            } else {
                Log.d("UserRepository", "No network, deleting user $userId locally")
                userDao.deleteUserById(userId)
                return@withContext Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Delete account exception: ${e.message}", e)
            return@withContext Result.failure(e)
        }
    }

    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }

    suspend fun refreshAllUsers(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllUsers()
            if (response.isSuccessful && response.body() != null) {
                userDao.insertAllUsers(response.body()!!)
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
                userDao.insertAllUsers(response.body()!!)
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Failed to refresh pending verifications"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    fun getArtists(): Flow<List<User>> {
        return userDao.getUsersByType(UserType.ARTIST)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    suspend fun getUserById(userId: String): User? {
        try {
            val response = apiService.getUserById(userId)

            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    // Update local user
                    userDao.insertUser(user)
                    return user
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "API getUserById failed: ${e.message}", e)
        }

        return userDao.getUserById(userId)
    }

    suspend fun promoteUserToArtist(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateUserType(userId, "ARTIST")
            if (response.isSuccessful && response.body() != null) {
                userDao.insertUser(response.body()!!)
                return@withContext Result.success(Unit)
            } else {
                userDao.updateUserType(userId, "ARTIST")
                return@withContext Result.success(Unit)
            }
        } catch (e: Exception) {
            userDao.updateUserType(userId, "ARTIST")
            return@withContext Result.success(Unit)
        }
    }

    suspend fun promoteUserToAdmin(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateUserType(userId, "ADMIN")
            if (response.isSuccessful && response.body() != null) {
                userDao.insertUser(response.body()!!)
                return@withContext Result.success(Unit)
            } else {
                val user = userDao.getUserById(userId) ?: return@withContext Result.failure(Exception("User not found"))
                userDao.insertUser(user.copy(userType = UserType.ADMIN))
                return@withContext Result.success(Unit)
            }
        } catch (e: Exception) {
            val user = userDao.getUserById(userId) ?: return@withContext Result.failure(e)
            userDao.insertUser(user.copy(userType = UserType.ADMIN))
            return@withContext Result.success(Unit)
        }
    }

    suspend fun updateUserPassword(email: String, newPassword: String): Result<User> {
        return try {
            try {
                val response = apiService.updateUserProfileJson(
                    UserUpdateRequest(
                        password = newPassword
                    )
                )

                if (response.isSuccessful) {
                    val updatedUser = response.body()
                    if (updatedUser != null) {
                        val localUser = userDao.getUserByEmail(email)
                        if (localUser != null) {
                            val hashedPassword = hashPassword(newPassword)
                            val localUpdatedUser = localUser.copy(password = hashedPassword)
                            userDao.insertUser(localUpdatedUser)
                            return Result.success(localUpdatedUser)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "API updateUserPassword failed: ${e.message}", e)
            }

            val user = userDao.getUserByEmail(email) ?: return Result.failure(Exception("User not found"))
            val hashedPassword = hashPassword(newPassword)
            val updatedUser = user.copy(password = hashedPassword)
            userDao.insertUser(updatedUser)
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
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

            if (response.isSuccessful) {
                val updatedUser = response.body()
                if (updatedUser != null) {
                    userDao.insertUser(updatedUser)
                    Result.success(updatedUser)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                // Fallback to local if offline or API fails but we have some data
                val localUser = userDao.getUserByEmail(email)
                if (localUser != null) {
                    val updatedLocalUser = localUser.copy(
                        fullName = fullName ?: localUser.fullName,
                        bio = bio ?: localUser.bio,
                        location = location ?: localUser.location,
                        website = website ?: localUser.website,
                        profileImageUrl = profileImagePath ?: localUser.profileImageUrl,
                        coverImageUrl = coverImagePath ?: localUser.coverImageUrl
                    )
                    userDao.insertUser(updatedLocalUser)
                    Result.success(updatedLocalUser)
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Update failed"))
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "updateUserProfile exception: ${e.message}", e)
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
            if (result.isSuccess) {
                userDao.updateCoverImage(email, imageUrl)
                Result.success(Unit)
            } else {
                userDao.updateCoverImage(email, imageUrl)
                Result.success(Unit) // Fallback local success
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveArtist(artistId: String, approved: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.approveArtist(artistId, approved)
            if (response.isSuccessful) {
                userDao.updateArtistApprovalStatus(artistId, approved)
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Exception("Failed to approve artist"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    suspend fun isUserExists(email: String): Boolean {
        val user = userDao.getUserByEmail(email)
        return user != null
    }

    suspend fun suspendUser(userId: String, isSuspended: Boolean = true): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.suspendUser(userId, isSuspended)
            if (response.isSuccessful) {
                userDao.updateUserSuspension(userId, isSuspended)
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
        return userDao.getUsersByType(UserType.ARTIST).map { users ->
            users.filter { it.verificationStatus == VerificationStatus.PENDING }
        }
    }

    suspend fun approveUserVerification(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateVerificationStatus(userId, VerificationUpdateRequest(status = "VERIFIED"))
            if (response.isSuccessful) {
                val user = userDao.getUserById(userId)
                if (user != null) {
                    userDao.insertUser(user.copy(verificationStatus = VerificationStatus.VERIFIED))
                }
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
                val user = userDao.getUserById(userId)
                if (user != null) {
                    userDao.insertUser(user.copy(verificationStatus = VerificationStatus.REJECTED))
                }
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
