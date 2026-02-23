package com.rivo.app.ui.navigation

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rivo.app.data.model.NotificationType
import com.rivo.app.data.model.UserType
import com.rivo.app.data.repository.SessionManager
import com.rivo.app.ui.screens.admin.*
import com.rivo.app.ui.screens.artist.*
import com.rivo.app.ui.screens.explore.ExploreScreen
import com.rivo.app.ui.screens.music.FavoriteScreen
import com.rivo.app.ui.screens.forgotpassword.ForgotPasswordScreen
import com.rivo.app.ui.screens.home.HomeScreen
import com.rivo.app.ui.screens.notification.NotificationScreen
import com.rivo.app.ui.screens.library.LibraryScreen
import com.rivo.app.ui.screens.listener.ListenerProfileScreen
import com.rivo.app.ui.screens.login.LoginScreen
import com.rivo.app.ui.screens.music.MusicListScreen
import com.rivo.app.ui.screens.player.PlayerScreen
import com.rivo.app.ui.screens.profile.*
import com.rivo.app.ui.screens.register.RegisterScreen
import com.rivo.app.ui.screens.search.SearchScreen
import com.rivo.app.ui.screens.welcome.WelcomeScreen
import com.rivo.app.ui.viewmodel.*


@Composable
fun RivoNavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    sessionManager: SessionManager,
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val musicViewModel: MusicViewModel = hiltViewModel()
    val exploreViewModel: ExploreViewModel = hiltViewModel()
    val searchViewModel: SearchViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()
    val artistViewModel: ArtistViewModel = hiltViewModel()
    val libraryViewModel: LibraryViewModel = hiltViewModel()
    val adminViewModel: AdminViewModel = hiltViewModel()
    val followViewModel: FollowViewModel = hiltViewModel()
    val notificationViewModel: NotificationViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(RivoScreens.Welcome.name) {
            WelcomeScreen(
                onLoginClick = { navController.navigate(RivoScreens.Login.name) },
                onRegisterClick = { navController.navigate(RivoScreens.Register.name) },
                onGuestClick = {
                    // Set user as guest in session manager
                    navController.navigate(RivoScreens.Home.name) {
                        popUpTo(RivoScreens.Welcome.name) { inclusive = true }
                    }
                }
            )
        }

        composable(RivoScreens.Login.name) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(RivoScreens.Home.name) {
                        popUpTo(RivoScreens.Welcome.name) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() },
                onSignUpClick = { navController.navigate(RivoScreens.Register.name) },
                onForgotPasswordClick = {
                    navController.navigate(RivoScreens.ForgotPassword.name)
                },
            )
        }

        composable(RivoScreens.ForgotPassword.name) {
            ForgotPasswordScreen(
                viewModel = hiltViewModel(),
                authViewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onResetSuccess = { navController.navigate(RivoScreens.Login.name) },
            )
        }

        // Register Screen with optional "from" parameter to track where user came from
        composable(
            route = "${RivoScreens.Register.name}?from={from}",
            arguments = listOf(
                navArgument("from") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val fromScreen = backStackEntry.arguments?.getString("from")

            RegisterScreen(
                authViewModel = authViewModel,
                viewModel = hiltViewModel(),
                onRegisterSuccess = {
                    navController.navigate(RivoScreens.Home.name) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                // Handle back button based on where user came from
                onBackClick = {
                    if (fromScreen != null) {
                        // If redirected from Library or Profile, go back to Home
                        navController.navigate(RivoScreens.Home.name) {
                            popUpTo(RivoScreens.Home.name) { inclusive = false }
                        }
                    } else {
                        // Normal back behavior
                        navController.popBackStack()
                    }
                },
                onLoginClick = { navController.navigate(RivoScreens.Login.name) }
            )
        }

        composable(RivoScreens.Home.name) {
            val exploreViewModel: ExploreViewModel = hiltViewModel()
            val libraryViewModel: LibraryViewModel = hiltViewModel()
            val sessionViewModel: SessionViewModel = hiltViewModel()
            val followViewModel: FollowViewModel = hiltViewModel()

            HomeScreen(
                exploreViewModel = exploreViewModel,
                musicViewModel = musicViewModel,
                libraryViewModel = libraryViewModel,
                sessionViewModel = sessionViewModel,
                notificationViewModel = notificationViewModel,
                onMusicClick = { musicId ->
                    musicViewModel.loadMusic(musicId)
                    navController.navigate("${RivoScreens.Player.name}/$musicId")
                },
                onArtistClick = { artistId ->
                    navController.navigate("${RivoScreens.ArtistDetail.name}/$artistId")
                },
                onSearchClick = {
                    navController.navigate(RivoScreens.Search.name)
                },
                onExploreClick = {
                    navController.navigate(RivoScreens.Explore.name)
                },
                onSeeAllNewReleasesClick = {
                    navController.navigate("${RivoScreens.MusicList.name}/new")
                },
                onNotificationClick = {
                    navController.navigate(RivoScreens.Notification.name)
                },
                onSeeAllClick = { section ->
                    when (section) {
                        "artists" -> navController.navigate(RivoScreens.ArtistList.name)
                        else -> navController.navigate("${RivoScreens.MusicList.name}/$section")
                    }
                },
                onPlaylistClick = { playlistId ->
                    navController.navigate("${RivoScreens.PlaylistDetail.name}/$playlistId")
                },
                modifier = Modifier,
                followViewModel = followViewModel,
            )
        }

        composable(RivoScreens.Search.name) {
            SearchScreen(
                onMusicClick = { music ->
                    musicViewModel.loadMusic(music.id)
                    navController.navigate("${RivoScreens.Player.name}/${music.id}")
                },
                onArtistClick = { artist ->
                    navController.navigate("${RivoScreens.ArtistDetail.name}/${artist.id}")
                },
                onBackClick = { navController.popBackStack() },
                searchViewModel = searchViewModel
            )
        }

        composable(RivoScreens.Explore.name) {
            ExploreScreen(
                exploreViewModel = exploreViewModel,
                onSearchClick = { navController.navigate(RivoScreens.Search.name) },
                onMusicClick = { music ->
                    musicViewModel.loadMusic(music.id)
                    navController.navigate("${RivoScreens.Player.name}/${music.id}")
                },
                onArtistClick = { artist ->
                    navController.navigate("${RivoScreens.ArtistDetail.name}/${artist.id}")
                },
                onViewAllTrendingClick = {
                    navController.navigate("${RivoScreens.MusicList.name}/trending")
                },
                onViewAllArtistsClick = {
                    navController.navigate(RivoScreens.ArtistList.name)
                },
                onViewAllNewReleasesClick = {
                    navController.navigate("${RivoScreens.MusicList.name}/new")
                },
                onPlayFeaturedClick = { musicId ->
                    musicViewModel.loadMusic(musicId)
                    navController.navigate("${RivoScreens.Player.name}/$musicId")
                },
                onExploreClick = { }
            )
        }

        composable(RivoScreens.Notification.name) {
            NotificationScreen(
                onBackClick = { navController.popBackStack() },
                onNotificationClick = { notification ->
                    when (notification.type) {
                        NotificationType.NEW_MUSIC -> {
                            notification.relatedContentId?.let { id ->
                                musicViewModel.loadMusic(id)
                                navController.navigate("${RivoScreens.Player.name}/$id")
                            }
                        }
                        NotificationType.NEW_FOLLOWER -> {
                            notification.relatedContentId?.let { id ->
                                navController.navigate("${RivoScreens.ArtistDetail.name}/$id")
                            }
                        }
                        NotificationType.VERIFICATION -> {
                            navController.navigate(RivoScreens.Profile.name)
                        }
                        else -> { /* Do nothing for generic system notifications */ }
                    }
                },
                viewModel = notificationViewModel
            )
        }

        composable(RivoScreens.Library.name) {
            val currentUser by authViewModel.currentUser.collectAsState()
            val isGuest = currentUser?.userType == UserType.GUEST

            LaunchedEffect(isGuest) {
                if (isGuest) {
                    navController.navigate(RivoScreens.Welcome.name) {
                        popUpTo(0) { inclusive = true }                    }
                }
            }

            if (!isGuest) {
                val userId = currentUser?.id ?: return@composable

                LibraryScreen(
                    libraryViewModel = libraryViewModel,
                    musicViewModel = musicViewModel,
                    userId = userId,
                    onPlaylistClick = { playlistId ->
                        navController.navigate("${RivoScreens.PlaylistDetail.name}/$playlistId")
                    },
                    onCreatePlaylistClick = {
                        // Implement playlist creation
                    },
                    onMusicClick = { music ->
                        musicViewModel.playMusic(music)
                        navController.navigate("${RivoScreens.Player.name}/${music.id}")
                    }
                )
            }
        }

        composable(
            route = "${RivoScreens.Player.name}/{musicId}",
            arguments = listOf(navArgument("musicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val musicId = backStackEntry.arguments?.getString("musicId") ?: ""
            val context = LocalContext.current
            LaunchedEffect(musicId) {
                musicViewModel.loadMusic(musicId)
            }

            PlayerScreen(
                musicViewModel = musicViewModel,
                musicId = musicId,
                onBackClick = { navController.popBackStack() },
                onArtistClick = { artistId ->
                    navController.navigate("${RivoScreens.ArtistDetail.name}/$artistId")
                },
                onShareClick = {
                    musicViewModel.currentMusic.value?.let { current ->
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Check out this song: ${current.title} by ${current.artist}"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    }
                },
                onFavoriteClick = {
                    musicViewModel.currentMusic.value?.id?.let { id ->
                        musicViewModel.toggleFavorite(id)
                    }
                }
            )
        }

        composable(
            route = "${RivoScreens.ArtistDetail.name}/{artistId}",
            arguments = listOf(navArgument("artistId") { type = NavType.StringType })
        ) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
            ArtistDetailScreen(
                artistId = artistId,
                artistViewModel = artistViewModel,
                musicViewModel = musicViewModel,
                onBackClick = { navController.popBackStack() },
                onMusicClick = { music ->
                    musicViewModel.loadMusic(music.id)
                    navController.navigate("${RivoScreens.Player.name}/${music.id}")
                },
                followViewModel = followViewModel,
                sessionManager = sessionManager
            )
        }

        composable(RivoScreens.Profile.name) {
            val currentUser by authViewModel.currentUser.collectAsState()
            val isGuest = currentUser?.userType == UserType.GUEST
            LaunchedEffect(isGuest) {
                if (isGuest) {
                    navController.navigate(RivoScreens.Welcome.name) {
                        popUpTo(RivoScreens.Home.name) { inclusive = true }
                    }
                }
            }

            if (!isGuest) {
                when (currentUser?.userType) {
                    UserType.ADMIN -> {
                        AdminProfileScreen(
                            onAdminPanelClick = { navController.navigate(RivoScreens.AdminPanel.name) },
                            onManageFeaturedClick = { navController.navigate(RivoScreens.AdminFeaturedContent.name) },
                            onSettingsClick = { navController.navigate(RivoScreens.Settings.name) },
                            onHelpAndSupportClick = { navController.navigate(RivoScreens.HelpSupport.name) },
                            onAboutClick = { navController.navigate(RivoScreens.About.name) },
                            onLogoutClick = {
                                authViewModel.logout()
                                navController.navigate(RivoScreens.Login.name) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            currentUser = currentUser,
                            onEditProfileClick = { navController.navigate(RivoScreens.EditProfile.name) },
                            authViewModel = authViewModel
                        )
                    }

                    UserType.ARTIST -> {
                        ArtistProfileScreen(
                            onBackClick = { navController.popBackStack() },
                            onDashboardClick = { navController.navigate(RivoScreens.ArtistDashboard.name) },
                            onEditProfileClick = { navController.navigate(RivoScreens.EditProfile.name) },
                            onGetVerifiedClick = { navController.navigate(RivoScreens.ArtistVerification.name) },
                            onSettingsClick = { navController.navigate(RivoScreens.Settings.name) },
                            onHelpClick = { navController.navigate(RivoScreens.HelpSupport.name) },
                            onAboutClick = { navController.navigate(RivoScreens.About.name) },
                            onLogoutClick = {
                                authViewModel.logout()
                                navController.navigate(RivoScreens.Login.name) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            artistViewModel = artistViewModel,
                            authViewModel = authViewModel,
                            followViewModel = followViewModel,
                            currentUser = currentUser
                        )
                    }

                    UserType.LISTENER -> {
                        ListenerProfileScreen(
                            onBackClick = { navController.popBackStack() },
                            onEditProfileClick = { navController.navigate(RivoScreens.EditProfile.name) },
                            onSettingsClick = { navController.navigate(RivoScreens.Settings.name) },
                            onHelpClick = { navController.navigate(RivoScreens.HelpSupport.name) },
                            onAboutClick = { navController.navigate(RivoScreens.About.name) },
                            onLogoutClick = {
                                authViewModel.logout()
                                navController.navigate(RivoScreens.Login.name) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            authViewModel = authViewModel,
                            followViewModel = followViewModel,
                            currentUser = currentUser,
                        )
                    }

                    else -> {
                        LaunchedEffect(Unit) {
                            val session = sessionManager.getCurrentUser()

                            when {
                                !session.isLoggedIn && session.email.isBlank() -> {
                                    navController.navigate(RivoScreens.Welcome.name) {
                                        popUpTo(0) { inclusive = true }                                    }
                                }

                                !session.isLoggedIn && session.email.isNotBlank() -> {
                                    navController.navigate(RivoScreens.Welcome.name) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }

                                else -> {
                                    navController.navigate(RivoScreens.Home.name) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }

        composable(
            route = "${RivoScreens.MusicList.name}/{listType}",
            arguments = listOf(navArgument("listType") { type = NavType.StringType })
        ) { entry ->
            val type = entry.arguments?.getString("listType") ?: "trending"
            val musicViewModel: MusicViewModel = hiltViewModel()

            MusicListScreen(
                listType = type,
                musicViewModel = musicViewModel,
                onBackClick = { navController.popBackStack() },
                onSearchClick = { navController.navigate(RivoScreens.Search.name) },
                onMusicClick = { track ->
                    musicViewModel.loadMusic(track.id)
                    navController.navigate("${RivoScreens.Player.name}/${track.id}")
                }
            )
        }

        composable(RivoScreens.Settings.name) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(RivoScreens.HelpSupport.name) {
            HelpSupportScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(RivoScreens.About.name) {
            AboutScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(RivoScreens.EditProfile.name) {
            EditProfileScreen(
                userViewModel = userViewModel,
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        composable(RivoScreens.Favorite.name) {
            val musicViewModel: MusicViewModel = hiltViewModel()

            FavoriteScreen(
                musicViewModel = musicViewModel,
                onBackClick = { navController.popBackStack() },
                onMusicClick = { musicId ->
                    musicViewModel.loadMusic(musicId)
                    navController.navigate("${RivoScreens.Player.name}/$musicId")
                },
                onRemoveFromFavoritesClick = { musicId ->
                    musicViewModel.toggleFavorite(musicId)
                }
            )
        }



        composable(RivoScreens.ArtistDashboard.name) {
            ArtistDashboardScreen(
                onBackClick = { navController.popBackStack() },
                onEditTrackClick = { music ->
                    navController.navigate("${RivoScreens.ArtistDashboard.name}/edit/${music.id}")
                },
                onDeleteTrackClick = { music ->
                    artistViewModel.deleteMusic(music.id)
                },
                artistViewModel = artistViewModel,
            )
        }

        composable(RivoScreens.ArtistProfile.name) {
            ArtistProfileScreen(
                onBackClick = { navController.popBackStack() },
                onDashboardClick = { navController.navigate(RivoScreens.ArtistDashboard.name) },
                onEditProfileClick = { navController.navigate("${RivoScreens.ArtistProfile.name}/edit") },
                onGetVerifiedClick = { navController.navigate(RivoScreens.ArtistVerification.name) },
                onSettingsClick = { navController.navigate(RivoScreens.Settings.name) },
                onHelpClick = { navController.navigate(RivoScreens.HelpSupport.name) },
                onAboutClick = { navController.navigate(RivoScreens.About.name) },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(RivoScreens.Login.name) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                artistViewModel = artistViewModel,
                authViewModel = authViewModel,
                followViewModel = followViewModel,
            )
        }

        composable(RivoScreens.ArtistVerification.name) {
            ArtistVerificationScreen(
                onBackClick = { navController.popBackStack() },
                onSubmitClick = {
                    navController.popBackStack()
                }
            )
        }

        // Admin Panel
        composable(RivoScreens.AdminPanel.name) {
            AdminPanelScreen(
                onBackClick = { navController.popBackStack() },
                onUserClick = { user ->
                    navController.navigate("${RivoScreens.AdminProfile.name}/${user.id}")
                },
                onMusicClick = { musicId ->
                    navController.navigate("${RivoScreens.Player.name}/$musicId")
                },
                onVerificationClick = { verificationId ->
                    navController.navigate("${RivoScreens.AdminPanel.name}/verifications/$verificationId")
                },
                onFeaturedContentClick = {
                    navController.navigate(RivoScreens.AdminFeaturedContent.name)
                },
                adminViewModel = adminViewModel
            )
        }

        composable(
            route = "${RivoScreens.AdminProfile.name}/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            AdminProfileScreen(
                onAdminPanelClick = { navController.navigate(RivoScreens.AdminPanel.name) },
                onManageFeaturedClick = { navController.navigate(RivoScreens.AdminFeaturedContent.name) },
                onSettingsClick = { navController.navigate(RivoScreens.Settings.name) },
                onHelpAndSupportClick = { navController.navigate(RivoScreens.HelpSupport.name) },
                onAboutClick = { navController.navigate(RivoScreens.About.name) },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(RivoScreens.Welcome.name) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onEditProfileClick = TODO(),
                currentUser = TODO(),
                authViewModel = TODO()
            )
        }


        composable(RivoScreens.AdminFeaturedContent.name) {
            AdminFeaturedContentScreen(
                adminViewModel = adminViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}