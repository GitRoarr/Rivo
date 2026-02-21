package com.rivo.app.data.repository

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresPermission
import com.rivo.app.data.local.UserDao
import com.rivo.app.data.model.ArtistDocument
import com.rivo.app.data.model.UserType
import com.rivo.app.data.model.VerificationRequest
import com.rivo.app.data.model.VerificationStatus
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerificationRepository @Inject constructor(
    private val userDao: UserDao,
    private val notificationRepository: NotificationRepository,
    private val context: Context
) {


    suspend fun submitVerificationRequest(
        artistId: String,
        documentUrl: String,
        additionalInfo: String? = null
    ): Result<String> {
        return try {
            val user = userDao.getUserByEmail(artistId)
                ?: return Result.failure(Exception("User not found"))

            if (user.userType != UserType.ARTIST) {
                return Result.failure(Exception("Only artists can request verification"))
            }

            val requestId = UUID.randomUUID().toString()
            val now = Date()

            val request = VerificationRequest(
                id = requestId,
                artistId = artistId,
                documentUrl = documentUrl,
                additionalInfo = additionalInfo,
                status = VerificationStatus.PENDING,
                submissionDate = now,
                reviewDate = null,
                reviewedBy = null,
                rejectionReason = null,
                artistName = user.fullName
            )
            userDao.insertVerificationRequest(request)

            val updatedUser = user.copy(
                verificationStatus = VerificationStatus.PENDING,
                verificationRequestId = requestId,
                verificationRequestDate = now
            )
            userDao.insertUser(updatedUser)

            Result.success(requestId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPendingVerificationRequests(): Flow<List<VerificationRequest>> {
        return userDao.getVerificationRequestsByStatus(VerificationStatus.PENDING)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    suspend fun approveVerification(artistId: String): Boolean {
        return try {
            val user = userDao.getUserByEmail(artistId) ?: return false

            val updatedUser = user.copy(
                verificationStatus = VerificationStatus.VERIFIED,
                verificationApprovalDate = Date()
            )
            userDao.insertUser(updatedUser)

            notificationRepository.showVerificationNotification(updatedUser, true)
            true
        } catch (e: Exception) {
            false
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    suspend fun rejectVerification(artistId: String, reason: String): Boolean {
        return try {
            val user = userDao.getUserByEmail(artistId) ?: return false

            val updatedUser = user.copy(
                verificationStatus = VerificationStatus.REJECTED,
                verificationRejectionReason = reason
            )
            userDao.insertUser(updatedUser)

            notificationRepository.showVerificationNotification(updatedUser, false)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getVerificationStatus(artistId: String): VerificationStatus? {
        return userDao.getUserByEmail(artistId)?.verificationStatus
    }


    suspend fun getVerificationRequest(artistId: String): VerificationRequest? {
        return userDao.getVerificationRequestByArtistId(artistId)
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
            val idDocFile = prepareFileForUpload(idDocumentUri)
            val idDocumentUrl = idDocFile.absolutePath

            val proofFile = prepareFileForUpload(proofOfArtistryUri)
            val proofUrl = proofFile.absolutePath

            val metadata = buildString {
                append("## Artist Verification Details\n\n")
                append("### Personal Information\n")
                append("- Name: $artistName\n")
                append("- Email: $email\n")
                append("- Phone: $phoneNumber\n")
                append("- Location: $location\n\n")

                append("### Music Information\n")
                append("- Genre: $primaryGenre\n")
                append("- Bio: $artistBio\n\n")

                append("### Social Media\n")
                socialLinks.forEach { (platform, username) ->
                    if (username.isNotBlank()) {
                        append("- $platform: $username\n")
                    }
                }
                append("\n")

                append("### Documents\n")
                append("- ID Document: $idDocumentUrl\n")
                append("- Proof of Artistry: $proofUrl\n")
            }

            val result = submitVerificationRequest(
                artistId = userId,
                documentUrl = idDocumentUrl,
                additionalInfo = metadata
            )

            if (result.isSuccess) {
                val requestId = result.getOrNull()!!

                val user = userDao.getUserByEmail(userId)
                if (user != null) {
                    val updatedUser = user.copy(
                        name = artistName,
                        email = email,
                        bio = artistBio,
                        location = location,
                        primaryGenre = primaryGenre,
                        phoneNumber = phoneNumber,
                        socialLinks = socialLinks
                    )
                    userDao.insertUser(updatedUser)
                }

                val idDocument = ArtistDocument(
                    id = UUID.randomUUID().toString(),
                    artistId = userId,
                    documentType = "ID",
                    documentUrl = idDocumentUrl,
                    verificationRequestId = requestId
                )
                userDao.insertArtistDocument(idDocument)

                val proofDocument = ArtistDocument(
                    id = UUID.randomUUID().toString(),
                    artistId = userId,
                    documentType = "PROOF_OF_ARTISTRY",
                    documentUrl = proofUrl,
                    verificationRequestId = requestId
                )
                userDao.insertArtistDocument(proofDocument)
            }

            result.isSuccess
        } catch (e: Exception) {
            false
        }
    }


    fun getVerificationRequestsForAdmin(): Flow<List<VerificationRequest>> {
        return userDao.getVerificationRequestsWithDetails()
    }




}