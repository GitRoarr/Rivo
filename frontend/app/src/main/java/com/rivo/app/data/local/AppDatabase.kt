package com.rivo.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rivo.app.data.model.*

@Database(
    entities = [
        User::class,
        Music::class,
        Playlist::class,
        Watchlist::class,
        ArtistAnalytics::class,
        Follow::class,
        FeaturedContent::class,
        Notification::class,
        PlaylistMusicCrossRef::class,
        WatchlistMusicCrossRef::class,
        ArtistDocument::class ,
        VerificationRequest::class,
        MusicPlayed::class



    ],
    version = 27,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun musicDao(): MusicDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun artistStatsDao(): ArtistStatsDao
    abstract fun followDao(): FollowDao
    abstract fun featuredContentDao(): FeaturedContentDao
    abstract fun notificationDao(): NotificationDao
    abstract fun musicPlayedDao(): MusicPlayedDao

}
