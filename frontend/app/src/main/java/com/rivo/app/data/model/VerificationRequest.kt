package com.rivo.app.data.model

import java.util.Date

/**
 * Entity representing a verification request from an artist
 */
data class VerificationRequest(
    val id: String,
    val artistId: String,
    val documentUrl: String,
    val additionalInfo: String? = null,
    val status: VerificationStatus = VerificationStatus.PENDING,
    val submissionDate: Date = Date(),
    val reviewDate: Date? = null,
    val reviewedBy: String? = null,
    val rejectionReason: String? = null,
    val artistName: String
)