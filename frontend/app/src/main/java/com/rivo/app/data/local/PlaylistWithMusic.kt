package com.rivo.app.data.local

import androidx.room.Embedded
import androidx.room.Relation
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.Playlist

data class PlaylistWithMusic(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "id",
        entityColumn = "playlistId"
    )
    val musicList: List<Music>
)
