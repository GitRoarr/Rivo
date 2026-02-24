package com.rivo.app.data.model

import java.util.Date

data class Follow(
    val followerId: String,
    val followingId: String,
    val createdAt: Date = Date()
)
