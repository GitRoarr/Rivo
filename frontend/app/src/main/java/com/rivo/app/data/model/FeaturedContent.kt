package com.rivo.app.data.model

enum class FeaturedType {
    SONG,
    ARTIST,
    BANNER
}

data class FeaturedContent(
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