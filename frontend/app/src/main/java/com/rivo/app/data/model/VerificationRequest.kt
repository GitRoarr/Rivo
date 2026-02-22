package com.rivo.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.rivo.app.data.local.Converters
import java.util.Date

/**
 * Entity representing a verification request from an artist
 */
@Entity(
    tableName = "verification_requests",
    indices = [Index("artistId")],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["artistId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(Converters::class)
data class VerificationRequest(
    @PrimaryKey
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
) {
    constructor() : this(
        id = "",
        artistId = "",
        documentUrl = "",
        artistName = ""
    )
}