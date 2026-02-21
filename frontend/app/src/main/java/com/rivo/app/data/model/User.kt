package com.rivo.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.rivo.app.data.local.Converters
import java.util.Date

enum class UserType {
    LISTENER, ARTIST, ADMIN, GUEST
}

@Entity(tableName = "users")
@TypeConverters(Converters::class)
data class User(
    @PrimaryKey
    val id: String,
    val email: String,
    val password: String,
    val name: String,
    val fullName: String,
    val userType: UserType = UserType.LISTENER,
    val profilePictureUrl: String? = null,
    val profileImageUrl: String? = null,
    val bio: String? = null,
    val isActive: Boolean = true,
    val isApproved: Boolean = false,
    val isVerified: Boolean = false,
    val isSuspended: Boolean = false,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val createdAt: Date = Date(),
    val verificationStatus: VerificationStatus = VerificationStatus.UNVERIFIED,
    val verificationRequestDate: Date? = null,
    val verificationApprovalDate: Date? = null,
    val verificationRejectionReason: String? = null,
    val verificationRequestId: String? = null,
    val phoneNumber: String? = null,
    val location: String? = null,
    val website: String? = null,
    val coverImageUrl: String? = null,
    val primaryGenre: String? = null,
    val socialLinks: Map<String, String> = emptyMap()
)