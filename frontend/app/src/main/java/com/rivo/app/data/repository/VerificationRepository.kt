package com.rivo.app.data.repository

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresPermission
import com.rivo.app.data.model.User
import com.rivo.app.data.model.VerificationStatus
import com.rivo.app.data.remote.ApiService
import com.rivo.app.data.remote.VerificationUpdateRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import java.io.File
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Singleton

@Singleton
class VerificationRepository @Inject constructor(
    private val apiService: ApiService,
    private val notificationRepository: NotificationRepository,
    private val context: Context
) {
    suspend fun submitVerificationRequest(userId: String): Result<Unit> {
        return Result.failure(Exception("Use submitEnhancedVerification instead"))
    }

    fun getPendingVerificationRequests(): Flow<List<User>> = flow {
        try {
            val response = apiService.getUsersAwaitingVerification()
            if (response.isSuccessful) emit(response.body() ?: emptyList())
            else emit(emptyList())
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    suspend fun approveVerification(artistId: String): Boolean {
        return try {
            val response = apiService.updateVerificationStatus(
                artistId,
                VerificationUpdateRequest(status = "APPROVED")
            )
            if (response.isSuccessful) {
                // Fetch user to show notification
                val userResponse = apiService.getUserById(artistId)
                if (userResponse.isSuccessful) {
                    userResponse.body()?.let { notificationRepository.showVerificationNotification(it, true) }
                }
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    suspend fun rejectVerification(artistId: String, reason: String): Boolean {
        return try {
            val response = apiService.updateVerificationStatus(
                artistId,
                VerificationUpdateRequest(status = "REJECTED", reason = reason)
            )
            if (response.isSuccessful) {
                val userResponse = apiService.getUserById(artistId)
                if (userResponse.isSuccessful) {
                    userResponse.body()?.let { notificationRepository.showVerificationNotification(it, false) }
                }
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getVerificationStatus(artistId: String): VerificationStatus? {
        return try {
            val response = apiService.getVerificationStatus(artistId)
            if (response.isSuccessful) VerificationStatus.valueOf(response.body()?.status ?: "PENDING")
            else null
        } catch (e: Exception) {
            null
        }
    }

    fun prepareFileForUpload(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload_", ".tmp", context.cacheDir)
        inputStream.use { input ->
            tempFile.outputStream().use { out ->
                input?.copyTo(out)
            }
        }
        return tempFile
    }

    fun getVerificationRequestsForAdmin(): Flow<List<User>> = getPendingVerificationRequests()

    suspend fun getVerificationRequest(userId: String): com.rivo.app.data.model.VerificationRequest? {
        return null
    }

    suspend fun submitEnhancedVerification(
        userId: String,
        artistName: String,
        email: String,
        phoneNumber: String,
        location: String,
        primaryGenre: String,
        artistBio: String,
        socialLinks: Map<String, String>,
        idDocumentUri: Uri,
        proofOfArtistryUri: Uri
    ): Boolean {
        return try {
            val idFile = prepareFileForUpload(idDocumentUri)
            val proofFile = prepareFileForUpload(proofOfArtistryUri)
            
            val idPart = MultipartBody.Part.createFormData(
                "idDocument", idFile.name, idFile.asRequestBody("image/*".toMediaTypeOrNull())
            )
            val proofPart = MultipartBody.Part.createFormData(
                "proofOfArtistry", proofFile.name, proofFile.asRequestBody("image/*".toMediaTypeOrNull())
            )
            
            val jsonLinks = JSONObject(socialLinks).toString()

            val response = apiService.submitVerificationRequest(
                userId = userId,
                idDocument = idPart,
                proofOfArtistry = proofPart,
                artistName = artistName.toRequestBody("text/plain".toMediaTypeOrNull()),
                email = email.toRequestBody("text/plain".toMediaTypeOrNull()),
                phoneNumber = phoneNumber.toRequestBody("text/plain".toMediaTypeOrNull()),
                location = location.toRequestBody("text/plain".toMediaTypeOrNull()),
                primaryGenre = primaryGenre.toRequestBody("text/plain".toMediaTypeOrNull()),
                artistBio = artistBio.toRequestBody("text/plain".toMediaTypeOrNull()),
                socialLinks = jsonLinks.toRequestBody("application/json".toMediaTypeOrNull())
            )
            
            idFile.delete()
            proofFile.delete()

            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}