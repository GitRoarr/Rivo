package com.rivo.app.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivo.app.data.model.VerificationRequest
import com.rivo.app.data.model.VerificationStatus
import com.rivo.app.data.repository.VerificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val verificationRepository: VerificationRepository
) : ViewModel() {

    private val _verificationStatus = MutableStateFlow<VerificationStatus?>(null)
    val verificationStatus: StateFlow<VerificationStatus?> = _verificationStatus.asStateFlow()

    private val _verificationRequest = MutableStateFlow<VerificationRequest?>(null)
    val verificationRequest: StateFlow<VerificationRequest?> = _verificationRequest.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _idDocumentUri = MutableStateFlow<Uri?>(null)
    val idDocumentUri: StateFlow<Uri?> = _idDocumentUri.asStateFlow()

    private val _proofOfArtistryUri = MutableStateFlow<Uri?>(null)
    val proofOfArtistryUri: StateFlow<Uri?> = _proofOfArtistryUri.asStateFlow()

    private val _submissionSuccess = MutableStateFlow(false)
    val submissionSuccess: StateFlow<Boolean> = _submissionSuccess.asStateFlow()

    fun checkVerificationStatus(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val status = verificationRepository.getVerificationStatus(userId)
                _verificationStatus.value = status

                if (status == VerificationStatus.PENDING) {
                    _verificationRequest.value = verificationRepository.getVerificationRequest(userId)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to check verification status: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitVerification(
        userId: String,
        artistName: String,
        email: String,
        phoneNumber: String,
        location: String,
        primaryGenre: String,
        artistBio: String,
        socialLinks: Map<String, String>
    ) {
        if (_idDocumentUri.value == null) {
            _errorMessage.value = "Please upload ID document"
            return
        }

        if (_proofOfArtistryUri.value == null) {
            _errorMessage.value = "Please upload proof of artistry"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val additionalInfo = buildString {
                    append("Bio: $artistBio\n")
                    append("Location: $location\n")
                    append("Genre: $primaryGenre\n")
                    append("Social Links:\n")
                    socialLinks.forEach { (platform, username) ->
                        if (username.isNotBlank()) {
                            append("- $platform: $username\n")
                        }
                    }
                }

                val success = verificationRepository.submitEnhancedVerification(
                    userId = userId,
                    artistName = artistName,
                    email = email,
                    phoneNumber = phoneNumber,
                    location = location,
                    primaryGenre = primaryGenre,
                    artistBio = artistBio,
                    socialLinks = socialLinks,
                    idDocumentUri = _idDocumentUri.value!!,
                    proofOfArtistryUri = _proofOfArtistryUri.value!!
                )

                if (success) {
                    _verificationStatus.value = VerificationStatus.PENDING
                    _submissionSuccess.value = true
                } else {
                    _errorMessage.value = "Failed to submit verification request"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error submitting verification: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setIdDocumentUri(uri: Uri) {
        _idDocumentUri.value = uri
    }

    fun setProofOfArtistryUri(uri: Uri) {
        _proofOfArtistryUri.value = uri
    }

    fun clearSubmissionSuccess() {
        _submissionSuccess.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }
}