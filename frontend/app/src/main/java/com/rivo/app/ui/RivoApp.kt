package com.rivo.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rivo.app.data.model.UserType
import com.rivo.app.data.repository.SessionManager
import com.rivo.app.ui.components.RivoBottomNavBar
import com.rivo.app.ui.components.MiniPlayer
import com.rivo.app.ui.navigation.RivoNavGraph
import com.rivo.app.ui.navigation.RivoScreens
import com.rivo.app.ui.viewmodel.AuthViewModel
import com.rivo.app.ui.viewmodel.MusicViewModel

@Composable
fun RivoApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
    musicViewModel: MusicViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentMusic by musicViewModel.currentMusic.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState(initial = false)
    val context = LocalContext.current

    val sessionManager = remember { SessionManager(context) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val publicScreens = setOf(
        RivoScreens.Welcome.name,
        RivoScreens.Login.name,
        RivoScreens.Register.name,
        RivoScreens.ForgotPassword.name
    )

    val bottomBarRoutes = setOf(
        RivoScreens.Home.name,
        RivoScreens.Search.name,
        RivoScreens.Library.name,
        RivoScreens.Profile.name,
        RivoScreens.Explore.name
    )

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn && currentRoute != null && currentRoute !in publicScreens) {
            navController.navigate(RivoScreens.Login.name) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val isPlayerScreen = currentRoute?.startsWith("${RivoScreens.Player.name}/") == true

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                RivoBottomNavBar(
                    navController,
                    currentUser?.userType ?: UserType.LISTENER
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = modifier.padding(innerPadding)) {
            RivoNavGraph(
                navController = navController,
                startDestination = if (isLoggedIn) {
                    RivoScreens.Home.name
                } else {
                    RivoScreens.Welcome.name
                },
                modifier = Modifier,
                sessionManager = sessionManager,
            )

            if (!isPlayerScreen) {
                currentMusic?.let { music ->
                    MiniPlayer(
                        musicViewModel = musicViewModel,
                        onMiniPlayerClick = {
                            navController.navigate("${RivoScreens.Player.name}/${music.id}")
                        },
                        modifier = Modifier
                    )
                }
            }
        }
    }
}
