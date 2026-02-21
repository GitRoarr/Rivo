package com.rivo.app.data.local

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.Watchlist
import com.rivo.app.data.model.WatchlistMusicCrossRef

data class WatchlistWithMusic(
    @Embedded val watchlist: Watchlist,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            WatchlistMusicCrossRef::class,
            parentColumn = "watchlistId",
            entityColumn = "musicId"
        )
    )
    val music: List<Music>
)
