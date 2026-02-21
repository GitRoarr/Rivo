package com.rivo.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rivo.app.data.model.MusicPlayed

@Dao
interface MusicPlayedDao {

    @Query("SELECT COUNT(*) FROM music_played WHERE userId = :userId AND musicId = :musicId")
    suspend fun hasUserPlayedMusic(userId: String, musicId: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMusicPlayed(musicPlayed: MusicPlayed)
}
