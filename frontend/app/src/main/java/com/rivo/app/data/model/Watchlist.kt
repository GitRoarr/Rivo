package com.rivo.app.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "watchlist",
    indices = [androidx.room.Index("createdBy")],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["createdBy"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Watchlist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val description: String,

    @ColumnInfo(name = "createdBy")
    val createdBy: String
) {
    constructor() : this(
        name = "",
        description = "",
        createdBy = ""
    )
}