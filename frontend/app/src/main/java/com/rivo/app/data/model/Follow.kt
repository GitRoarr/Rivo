package com.rivo.app.data.model

import androidx.room.Entity
import java.util.Date

@Entity(tableName = "follows", primaryKeys = ["followerId", "followingId"])
data class Follow(
    val followerId: String,
    val followingId: String,
    val createdAt: Date = Date()
)
