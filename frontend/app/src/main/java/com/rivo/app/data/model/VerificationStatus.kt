package com.rivo.app.data.model

/**
 * Enum representing the verification status of an artist
 */
enum class VerificationStatus {
    UNVERIFIED,    // Default state
    PENDING,       // Verification request submitted
    VERIFIED,      // Verification approved
    REJECTED       // Verification rejected
}
