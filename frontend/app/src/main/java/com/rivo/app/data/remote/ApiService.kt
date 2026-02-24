package com.rivo.app.data.remote

import com.rivo.app.data.model.Music
import com.rivo.app.data.model.Playlist
import com.rivo.app.data.model.User
import com.rivo.app.data.model.Watchlist
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // ─── User endpoints ──────────────────────────────────────────────────────
    @POST("api/users/register")
    suspend fun registerUser(@Body user: UserRegistrationRequest): Response<UserResponse>

    @POST("api/users/login")
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<UserResponse>

    @GET("api/users/profile")
    suspend fun getUserProfile(): Response<User>

    @Multipart
    @PUT("api/users/profile")
    suspend fun updateUserProfile(
        @Part profileImage: okhttp3.MultipartBody.Part? = null,
        @Part coverImage: okhttp3.MultipartBody.Part? = null,
        @Part("fullName") fullName: okhttp3.RequestBody? = null,
        @Part("bio") bio: okhttp3.RequestBody? = null,
        @Part("location") location: okhttp3.RequestBody? = null,
        @Part("website") website: okhttp3.RequestBody? = null,
        @Part("password") password: okhttp3.RequestBody? = null
    ): Response<User>

    @PUT("api/users/profile")
    suspend fun updateUserProfileJson(@Body updateRequest: UserUpdateRequest): Response<User>

    @GET("api/users/exists/{email}")
    suspend fun checkUserExists(@Path("email") email: String): Response<UserExistsResponse>

    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") userId: String): Response<User>

    @GET("api/users/email/{email}")
    suspend fun getUserByEmail(@Path("email") email: String): Response<User>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") userId: String): Response<MessageResponse>

    @POST("api/users/reset-password")
    suspend fun resetPassword(@Query("email") email: String, @Query("password") password: String): Response<MessageResponse>

    // Follow / unfollow
    @POST("api/users/{id}/follow")
    suspend fun followUser(@Path("id") userId: String): Response<MessageResponse>

    @DELETE("api/users/{id}/follow")
    suspend fun unfollowUser(@Path("id") userId: String): Response<MessageResponse>

    @GET("api/users/{id}/followers")
    suspend fun getUserFollowers(@Path("id") userId: String): Response<List<User>>

    @GET("api/users/{id}/following")
    suspend fun getUserFollowing(@Path("id") userId: String): Response<List<User>>

    @GET("api/users/{id}/is-following")
    suspend fun checkFollowStatus(@Path("id") artistId: String): Response<FollowStatusResponse>

    // Verification
    @Multipart
    @POST("api/users/{id}/verification")
    suspend fun submitVerificationRequest(
        @Path("id") userId: String,
        @Part idDocument: okhttp3.MultipartBody.Part,
        @Part proofOfArtistry: okhttp3.MultipartBody.Part,
        @Part("artistName") artistName: okhttp3.RequestBody,
        @Part("email") email: okhttp3.RequestBody,
        @Part("phoneNumber") phoneNumber: okhttp3.RequestBody,
        @Part("location") location: okhttp3.RequestBody,
        @Part("primaryGenre") primaryGenre: okhttp3.RequestBody,
        @Part("artistBio") artistBio: okhttp3.RequestBody,
        @Part("socialLinks") socialLinks: okhttp3.RequestBody
    ): Response<MessageResponse>

    @GET("api/users/{id}/verification")
    suspend fun getVerificationStatus(@Path("id") userId: String): Response<VerificationStatusResponse>

    // Admin user management
    @PUT("api/users/{id}/type")
    suspend fun updateUserType(@Path("id") userId: String, @Query("type") type: String): Response<User>

    @PUT("api/users/{id}/approve")
    suspend fun approveArtist(@Path("id") userId: String, @Query("approved") approved: Boolean): Response<MessageResponse>

    @PUT("api/users/{id}/suspend")
    suspend fun suspendUser(@Path("id") userId: String, @Query("suspended") suspended: Boolean): Response<MessageResponse>

    @PUT("api/users/{id}/verification")
    suspend fun updateVerificationStatus(@Path("id") userId: String, @Body request: VerificationUpdateRequest): Response<MessageResponse>

    // ─── Music endpoints ─────────────────────────────────────────────────────
    @GET("api/music")
    suspend fun getAllMusic(): Response<List<Music>>

    @GET("api/music")
    suspend fun getMusicByGenre(@Query("genre") genre: String): Response<List<Music>>

    @GET("api/music/admin/all")
    suspend fun getAllMusicAdmin(): Response<List<Music>>

    @GET("api/music/pending")
    suspend fun getPendingMusic(): Response<List<Music>>

    @GET("api/music/{id}")
    suspend fun getMusicById(@Path("id") musicId: String): Response<Music>

    @Multipart
    @POST("api/music")
    suspend fun uploadMusic(
        @Part audio: okhttp3.MultipartBody.Part,
        @Part coverImage: okhttp3.MultipartBody.Part? = null,
        @Part("title") title: okhttp3.RequestBody,
        @Part("genre") genre: okhttp3.RequestBody,
        @Part("album") album: okhttp3.RequestBody? = null,
        @Part("duration") duration: okhttp3.RequestBody? = null
    ): Response<Music>

    @Multipart
    @PUT("api/music/{id}")
    suspend fun updateMusic(
        @Path("id") musicId: String,
        @Part coverImage: okhttp3.MultipartBody.Part? = null,
        @Part("title") title: okhttp3.RequestBody? = null,
        @Part("genre") genre: okhttp3.RequestBody? = null,
        @Part("album") album: okhttp3.RequestBody? = null
    ): Response<Music>

    @DELETE("api/music/{id}")
    suspend fun deleteMusic(@Path("id") musicId: String): Response<MessageResponse>

    @GET("api/music/artist/{artistId}")
    suspend fun getMusicByArtist(@Path("artistId") artistId: String): Response<List<Music>>

    @POST("api/music/{id}/play")
    suspend fun incrementMusicPlay(@Path("id") musicId: String): Response<PlayCountResponse>

    @PUT("api/music/{id}/approve")
    suspend fun approveMusic(@Path("id") musicId: String): Response<MusicApprovalResponse>

    @PUT("api/music/{id}/reject")
    suspend fun rejectMusic(@Path("id") musicId: String): Response<MusicApprovalResponse>

    // ─── Playlist endpoints ───────────────────────────────────────────────────
    @POST("api/playlists")
    suspend fun createPlaylist(@Body playlist: PlaylistRequest): Response<Playlist>

    @GET("api/playlists")
    suspend fun getUserPlaylists(): Response<List<Playlist>>

    @GET("api/playlists/{id}")
    suspend fun getPlaylistById(@Path("id") playlistId: Long): Response<Playlist>

    @PUT("api/playlists/{id}")
    suspend fun updatePlaylist(
        @Path("id") playlistId: Long,
        @Body updateRequest: PlaylistUpdateRequest
    ): Response<Playlist>

    @DELETE("api/playlists/{id}")
    suspend fun deletePlaylist(@Path("id") playlistId: Long): Response<MessageResponse>

    @POST("api/playlists/{id}/songs")
    suspend fun addSongToPlaylist(
        @Path("id") playlistId: Long,
        @Body request: AddSongRequest
    ): Response<Playlist>

    @DELETE("api/playlists/{id}/songs/{musicId}")
    suspend fun removeSongFromPlaylist(
        @Path("id") playlistId: Long,
        @Path("musicId") musicId: String
    ): Response<Playlist>

    // ─── Watchlist endpoints ──────────────────────────────────────────────────
    @POST("api/watchlists")
    suspend fun createWatchlist(@Body watchlist: WatchlistRequest): Response<Watchlist>

    @GET("api/watchlists")
    suspend fun getUserWatchlists(): Response<List<Watchlist>>

    @GET("api/watchlists/{id}")
    suspend fun getWatchlistById(@Path("id") watchlistId: Long): Response<Watchlist>

    @PUT("api/watchlists/{id}")
    suspend fun updateWatchlist(
        @Path("id") watchlistId: Long,
        @Body updateRequest: WatchlistUpdateRequest
    ): Response<Watchlist>

    @DELETE("api/watchlists/{id}")
    suspend fun deleteWatchlist(@Path("id") watchlistId: Long): Response<MessageResponse>

    @POST("api/watchlists/{id}/songs")
    suspend fun addSongToWatchlist(
        @Path("id") watchlistId: Long,
        @Body request: AddSongRequest
    ): Response<Watchlist>

    @DELETE("api/watchlists/{id}/songs/{musicId}")
    suspend fun removeSongFromWatchlist(
        @Path("id") watchlistId: Long,
        @Path("musicId") musicId: String
    ): Response<Watchlist>

    @GET("api/watchlists/check/{musicId}")
    suspend fun checkSongInWatchlists(@Path("musicId") musicId: String): Response<CheckWatchlistResponse>

    // ─── Search endpoint ──────────────────────────────────────────────────────
    @GET("api/search")
    suspend fun searchAll(@Query("q") query: String): Response<SearchResponse>

    // ─── Explore endpoint ─────────────────────────────────────────────────────
    @GET("api/explore")
    suspend fun getExploreData(): Response<ExploreResponse>

    // ─── Notification endpoints ───────────────────────────────────────────────
    @GET("api/notifications")
    suspend fun getNotifications(): Response<List<NotificationResponse>>

    @GET("api/notifications/unread-count")
    suspend fun getUnreadNotificationCount(): Response<UnreadCountResponse>

    @POST("api/notifications")
    suspend fun createNotification(@Body request: CreateNotificationRequest): Response<NotificationResponse>

    @PUT("api/notifications/read-all")
    suspend fun markAllNotificationsAsRead(): Response<MessageResponse>

    @PUT("api/notifications/{id}")
    suspend fun markNotificationAsRead(@Path("id") notificationId: String): Response<NotificationResponse>

    @DELETE("api/notifications/{id}")
    suspend fun deleteNotification(@Path("id") notificationId: String): Response<MessageResponse>

    @DELETE("api/notifications")
    suspend fun clearAllNotifications(): Response<MessageResponse>

    // ─── Stats endpoints ──────────────────────────────────────────────────────
    @GET("api/stats/artist")
    suspend fun getArtistStats(): Response<ArtistStatsResponse>

    @GET("api/stats/admin")
    suspend fun getAdminStats(): Response<AdminStatsResponse>

    @GET("api/stats/listener/{userId}")
    suspend fun getListenerStats(@Path("userId") userId: String): Response<ListenerStatsResponse>

    // Admin general management
    @GET("api/users")
    suspend fun getAllUsers(): Response<List<User>>

    @GET("api/users/verification/pending")
    suspend fun getUsersAwaitingVerification(): Response<List<User>>

    // Featured Content
    @GET("api/featured")
    suspend fun getFeaturedContent(): Response<List<FeaturedContentResponse>>

    @POST("api/featured")
    suspend fun addFeaturedContent(@Body request: FeaturedContentRequest): Response<FeaturedContentResponse>

    @PUT("api/featured/{id}")
    suspend fun updateFeaturedContent(@Path("id") id: String, @Body request: FeaturedContentRequest): Response<FeaturedContentResponse>

    @DELETE("api/featured/{id}")
    suspend fun deleteFeaturedContent(@Path("id") id: String): Response<MessageResponse>

    // ─── Liked songs endpoints ────────────────────────────────────────────────
    @GET("api/users/liked-songs")
    suspend fun getLikedSongs(): Response<List<Music>>

    @GET("api/users/liked-songs/check/{musicId}")
    suspend fun checkLikedSong(@Path("musicId") musicId: String): Response<LikedSongCheckResponse>

    @POST("api/users/liked-songs/{musicId}")
    suspend fun likeMusic(@Path("musicId") musicId: String): Response<LikedSongResponse>

    @DELETE("api/users/liked-songs/{musicId}")
    suspend fun unlikeMusic(@Path("musicId") musicId: String): Response<LikedSongResponse>
}


// ─── Request / Response data classes ─────────────────────────────────────────

data class UserRegistrationRequest(
    val id: String,
    val email: String,
    val password: String,
    val name: String,
    val fullName: String,
    val userType: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val fullName: String,
    val userType: String,
    val verificationStatus: String? = null,
    val token: String
)

data class UserUpdateRequest(
    val fullName: String? = null,
    val bio: String? = null,
    val location: String? = null,
    val website: String? = null,
    val password: String? = null,
    val profileImageUrl: String? = null,
    val coverImageUrl: String? = null
)

data class UserExistsResponse(
    val exists: Boolean
)

data class VerificationStatusResponse(
    val status: String
)

data class VerificationUpdateRequest(
    val status: String,  // "VERIFIED" or "REJECTED"
    val reason: String? = null
)

data class PlaylistRequest(
    val id: Long,
    val name: String,
    val description: String,
    val coverArtUrl: String? = null,
    val isPublic: Boolean = true
)

data class PlaylistUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val coverArtUrl: String? = null,
    val isPublic: Boolean? = null
)

data class WatchlistRequest(
    val id: Long,
    val name: String,
    val description: String
)

data class WatchlistUpdateRequest(
    val name: String? = null,
    val description: String? = null
)

data class AddSongRequest(
    val musicId: String
)

data class MessageResponse(
    val message: String
)

data class CheckWatchlistResponse(
    val inWatchlist: Boolean,
    val watchlists: List<WatchlistInfo>
)

data class WatchlistInfo(
    val id: Long,
    val name: String
)

data class MusicUploadRequest(
    val title: String,
    val genre: String,
    val album: String? = null,
    val url: String,
    val coverImageUrl: String? = null,
    val duration: Long = 0
)

data class MusicUpdateRequest(
    val title: String? = null,
    val genre: String? = null,
    val album: String? = null,
    val coverImageUrl: String? = null,
    val isApproved: Boolean? = null
)

data class MusicApprovalResponse(
    val message: String,
    val music: Music? = null
)

data class PlayCountResponse(
    val plays: Int
)

data class ListenerStatsResponse(
    val totalPlays: Int
)

data class SearchResponse(
    val music: List<Music>,
    val artists: List<User>
)

data class ExploreResponse(
    val trendingMusic: List<Music>,
    val newReleases: List<Music>,
    val featuredArtists: List<User>,
    val featuredMusic: List<Music>,
    val banners: List<BannerItem>,
    val categories: List<MusicCategory>? = null
)

data class MusicCategory(
    val id: String,
    val title: String,
    val color: String,
    val icon: String
)

data class BannerItem(
    val id: String,
    val imageUrl: String,
    val title: String,
    val subtitle: String,
    val actionUrl: String
)

data class NotificationResponse(
    val id: String,
    val user: String,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val relatedId: String? = null,
    val createdAt: String? = null
)

data class UnreadCountResponse(
    val count: Int
)

data class CreateNotificationRequest(
    val userId: String? = null,
    val type: String,
    val title: String,
    val message: String,
    val relatedId: String? = null
)

data class ArtistStatsResponse(
    val totalPlays: Int,
    val followersCount: Int,
    val followingCount: Int,
    val totalSongs: Int,
    val topSongs: List<Music>,
    val recentUploads: List<Music>,
    val pendingCount: Int,
    val unreadNotifications: Int
)

data class AdminStatsResponse(
    val totalUsers: Int,
    val totalArtists: Int,
    val totalListeners: Int,
    val totalMusic: Int,
    val totalPlays: Int,
    val pendingApproval: Int,
    val pendingVerifications: Int,
    val recentMusic: List<Music>? = null,
    val recentUsers: List<User>? = null,
    val newUsersToday: Int,
    val newMusicToday: Int
)

data class FeaturedContentResponse(
    val id: String,
    val type: String,
    val contentId: String? = null,
    val title: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val actionUrl: String? = null,
    val order: Int = 0,
    val isActive: Boolean = true
)

data class FeaturedContentRequest(
    val type: String,
    val contentId: String? = null,
    val title: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val actionUrl: String? = null,
    val order: Int = 0
)

data class LikedSongResponse(
    val liked: Boolean,
    val musicId: String
)

data class LikedSongCheckResponse(
    val liked: Boolean,
    val musicId: String
)

data class FollowStatusResponse(
    val isFollowing: Boolean
)
