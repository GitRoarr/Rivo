package com.rivo.app.ui.navigation

// Define all screens as enum values
enum class RivoScreens {
    Welcome,
    Login,
    Register,
    ForgotPassword,
    Home,
    Search,
    Explore,
    Library,
    Player,
    ArtistDetail,
    Profile,
    Settings,
    HelpSupport,
    About,
    EditProfile,
    MusicList,
    Favorite,
    Playlist,
    PlaylistDetail,
    WatchlistDetail,
    Notification,
    ArtistDashboard,
    ArtistProfile,
    ArtistVerification,
    ArtistAnalytics,
    ArtistList,
    AdminPanel,
    AdminProfile,
    AdminFeaturedContent;

    override fun toString(): String {
        return name
    }
}
