package com.rivo.app.data.model

data class PlaylistWithMusic(
    val playlist: Playlist,
    val musicList: List<Music> = emptyList()
)

data class PlaylistMusicCrossRef(
    val playlistId: Long,
    val musicId: String
)