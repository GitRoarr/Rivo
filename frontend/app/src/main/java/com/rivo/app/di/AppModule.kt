package com.rivo.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.media3.exoplayer.ExoPlayer
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
@Suppress("unused")
object AppModule {

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
    fun provideMusicRepository(
        sessionManager: SessionManager,
        apiService: ApiService,
        @ApplicationContext context: Context
    ): MusicRepository =
        MusicRepository(sessionManager, apiService, context)

    @Provides
    @Singleton
    fun provideNotificationRepository(
        @ApplicationContext context: Context,
        apiService: ApiService
    ): NotificationRepository =
        NotificationRepository(context, apiService)

    @Provides
    @Singleton
    fun provideArtistStatsRepository(
        apiService: ApiService
    ): ArtistStatsRepository =
        ArtistStatsRepository(apiService)

    @Provides
    @Singleton
    fun provideAnalyticsRepository(
        apiService: ApiService,
        musicRepository: MusicRepository
    ): AnalyticsRepository =
        AnalyticsRepository(apiService, musicRepository)

    @Provides
    @Singleton
    fun provideConnectivityRepository(@ApplicationContext context: Context): ConnectivityRepository =
        ConnectivityRepository(context)

    @Provides
    @Singleton
    fun provideFollowRepository(
        apiService: ApiService
    ): FollowRepository =
        FollowRepository(apiService)

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
        apiService: ApiService
    ): PlaylistRepository =
        PlaylistRepository(apiService)

    @Provides
    @Singleton
    fun provideSearchRepository(
        apiService: ApiService
    ): SearchRepository =
        SearchRepository(apiService)

    @Provides
    @Singleton
    fun provideVerificationRepository(
        apiService: ApiService,
        notificationRepository: NotificationRepository,
        @ApplicationContext context: Context
    ): VerificationRepository =
        VerificationRepository(apiService, notificationRepository, context)

    @Provides
    @Singleton
    fun provideWatchlistRepository(
        apiService: ApiService
    ): WatchlistRepository =
        WatchlistRepository(apiService)
}
