package com.rivo.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

enum class FeaturedType {
    SONG,
    ARTIST,
    BANNER
}

@Entity(
    tableName = "featured_content",
    indices = [
        androidx.room.Index("createdBy"),
        androidx.room.Index("featuredBy")
    ],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["createdBy"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["featuredBy"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FeaturedContent(
    @PrimaryKey
    val id: String,
    val contentId: String? = null,
    val type: FeaturedType,
    val title: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val createdBy: String,
    val featuredBy: String,
    val position: Int = 0,
    val isActive: Boolean = true,
    val createdAt: java.util.Date = java.util.Date(),
    val updatedAt: java.util.Date = java.util.Date()
) {
    constructor() : this(
        id = "",
        type = FeaturedType.SONG,
        title = "",
        createdBy = "",
        featuredBy = ""
    )
}