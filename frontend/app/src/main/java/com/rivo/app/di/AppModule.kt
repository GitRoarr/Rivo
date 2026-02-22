package com.rivo.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.media3.exoplayer.ExoPlayer
import androidx.room.Room
import com.rivo.app.data.local.*
import com.rivo.app.data.remote.ApiService
import com.rivo.app.data.remote.RetrofitClient
import com.rivo.app.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private const val DB_NAME = "rivo_db"

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
    @Provides @Singleton fun provideMusicDao(db: AppDatabase): MusicDao = db.musicDao()
    @Provides @Singleton fun providePlaylistDao(db: AppDatabase): PlaylistDao = db.playlistDao()
    @Provides @Singleton fun provideWatchlistDao(db: AppDatabase): WatchlistDao = db.watchlistDao()
    @Provides @Singleton fun provideArtistStatsDao(db: AppDatabase): ArtistStatsDao = db.artistStatsDao()
    @Provides @Singleton fun provideFollowDao(db: AppDatabase): FollowDao = db.followDao()
    @Provides @Singleton fun provideFeaturedContentDao(db: AppDatabase): FeaturedContentDao = db.featuredContentDao()
    @Provides @Singleton fun provideNotificationDao(db: AppDatabase): NotificationDao = db.notificationDao()
    @Provides @Singleton fun provideMusicPlayedDao(db: AppDatabase): MusicPlayedDao = db.musicPlayedDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("session_prefs")
        }

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager =
        SessionManager(context)

    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer =
        ExoPlayer.Builder(context).build()
    @Provides
    @Singleton
    fun provideRetrofitClient(sessionManager: SessionManager): RetrofitClient =
        RetrofitClient(sessionManager)


    @Provides
    @Singleton
    fun provideApiService(retrofitClient: RetrofitClient): ApiService =
        retrofitClient.apiService

    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        sessionManager: SessionManager,
        apiService: ApiService,
        connectivityRepository: ConnectivityRepository
    ): UserRepository =
        UserRepository(userDao, sessionManager, apiService, connectivityRepository)


    @Provides
    @Singleton
    fun provideMusicRepository(
        musicDao: MusicDao,
        musicPlayedDao: MusicPlayedDao,
        sessionManager: SessionManager,
        apiService: ApiService,
        @ApplicationContext context: Context
    ): MusicRepository =
        MusicRepository(musicDao, musicPlayedDao, sessionManager, apiService, context)

    @Provides
    @Singleton
    fun provideNotificationRepository(
        @ApplicationContext context: Context,
        notificationDao: NotificationDao,
        apiService: ApiService
    ): NotificationRepository =
        NotificationRepository(context, notificationDao, apiService)

    @Provides
    @Singleton
    fun provideArtistStatsRepository(
        artistStatsDao: ArtistStatsDao,
        apiService: ApiService
    ): ArtistStatsRepository =
        ArtistStatsRepository(artistStatsDao, apiService)

    @Provides
    @Singleton
    fun provideAnalyticsRepository(
        artistStatsDao: ArtistStatsDao,
        musicRepository: MusicRepository
    ): AnalyticsRepository =
        AnalyticsRepository(artistStatsDao, musicRepository)

    @Provides
    @Singleton
    fun provideConnectivityRepository(@ApplicationContext context: Context): ConnectivityRepository =
        ConnectivityRepository(context)



    @Provides
    @Singleton
    fun provideFollowRepository(
        followDao: FollowDao,
        userDao: UserDao,
        apiService: ApiService
    ): FollowRepository =
        FollowRepository(followDao, userDao, apiService)

    @Provides
    @Singleton
    fun providePlayerRepository(
        @ApplicationContext context: Context,
        musicRepository: MusicRepository,
        notificationRepository: NotificationRepository,
        artistStatsRepository: ArtistStatsRepository
    ): PlayerRepository =
        PlayerRepository(context, musicRepository, notificationRepository, artistStatsRepository)

    @Provides
    @Singleton
    fun providePlaylistRepository(
        playlistDao: PlaylistDao,
        userRepository: UserRepository,
        musicDao: MusicDao,
        apiService: ApiService
    ): PlaylistRepository =
        PlaylistRepository(playlistDao, musicDao, userRepository, apiService)

    @Provides
    @Singleton
    fun provideSearchRepository(
        musicDao: MusicDao,
        userDao: UserDao
    ): SearchRepository =
        SearchRepository(musicDao, userDao)

    @Provides
    @Singleton
    fun provideVerificationRepository(
        userDao: UserDao,
        notificationRepository: NotificationRepository,
        @ApplicationContext context: Context
    ): VerificationRepository =
        VerificationRepository(userDao, notificationRepository, context)

    @Provides
    @Singleton
    fun provideWatchlistRepository(
        watchlistDao: WatchlistDao,
        userRepository: UserRepository,
        apiService: ApiService
    ): WatchlistRepository =
        WatchlistRepository(watchlistDao, userRepository, apiService)
}
