package com.rivo.app.data.local

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.Playlist
import com.rivo.app.data.model.PlaylistMusicCrossRef

data class PlaylistWithMusic(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            PlaylistMusicCrossRef::class,
            parentColumn = "playlistId",
            entityColumn = "musicId"
        )
    )
    val musicList: List<Music>
)
