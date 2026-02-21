package com.rivo.app.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.rivo.app.data.model.User
import com.rivo.app.ui.theme.Primary
import com.rivo.app.ui.viewmodel.AdminViewModel

enum class AdminTab {
    DASHBOARD, USERS, MUSIC, VERIFICATION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBackClick: () -> Unit,
    onUserClick: (User) -> Unit,
    onMusicClick: (String) -> Unit,
    onVerificationClick: (String) -> Unit,
    onFeaturedContentClick: () -> Unit,
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(AdminTab.DASHBOARD) }

    val users by adminViewModel.allUsers.collectAsState()
    val pendingVerifications by adminViewModel.pendingVerifications.collectAsState()
    val pendingMusic by adminViewModel.pendingMusic.collectAsState()
    val allMusic by adminViewModel.allMusic.collectAsState()
    val featuredSongs by adminViewModel.featuredSongs.collectAsState()
    val featuredArtists by adminViewModel.featuredArtists.collectAsState()
    val stats by adminViewModel.platformStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = Color(0xFF1E1E1E),
                contentColor = Primary
            ) {
                Tab(
                    selected = selectedTab == AdminTab.DASHBOARD,
                    onClick = { selectedTab = AdminTab.DASHBOARD },
                    text = { Text("Dashboard", color = if (selectedTab == AdminTab.DASHBOARD) Primary else Color.White) }
                )
                Tab(
                    selected = selectedTab == AdminTab.USERS,
                    onClick = { selectedTab = AdminTab.USERS },
                    text = { Text("Users", color = if (selectedTab == AdminTab.USERS) Primary else Color.White) }
                )
                Tab(
                    selected = selectedTab == AdminTab.MUSIC,
                    onClick = { selectedTab = AdminTab.MUSIC },
                    text = { Text("Music", color = if (selectedTab == AdminTab.MUSIC) Primary else Color.White) }
                )
                Tab(
                    selected = selectedTab == AdminTab.VERIFICATION,
                    onClick = { selectedTab = AdminTab.VERIFICATION },
                    text = { Text("Verification", color = if (selectedTab == AdminTab.VERIFICATION) Primary else Color.White) }
                )
            }

            when (selectedTab) {
                AdminTab.DASHBOARD -> DashboardTab(
                    stats = stats,
                    pendingVerifications = pendingVerifications.size,
                    pendingMusicApprovals = pendingMusic.size,
                    onFeaturedContentClick = onFeaturedContentClick,
                    onVerificationTabClick = { selectedTab = AdminTab.VERIFICATION },
                    onMusicTabClick = { selectedTab = AdminTab.MUSIC }
                )
                AdminTab.USERS -> UsersTab(
                    users = users,
                    onUserClick = onUserClick,
                    onSuspendUser = { adminViewModel.suspendUser(it) },
                    onMakeAdmin = { adminViewModel.makeAdmin(it) },
                    onMakeArtist = { adminViewModel.makeArtist(it) },
                    onFeatureArtist = { adminViewModel.featureArtist(it) }
                )
                AdminTab.MUSIC -> MusicTab(
                    onRemoveFromFeaturedClick = { adminViewModel.removeFromFeatured(it) },

                    pendingMusicApprovals = pendingMusic,
                    allMusic = allMusic,
                    featuredMusic = featuredSongs,
                    onMusicClick = onMusicClick,
                    onApproveMusicClick = { adminViewModel.approveMusic(it) },
                    onRejectMusicClick = { adminViewModel.rejectMusic(it) },
                    onFeatureMusicClick = { adminViewModel.featureMusic(it) },
                    onDeleteMusicClick = { adminViewModel.rejectMusic(it) },
                    onEditMusicClick = { /* Navigate to edit music screen */ },
                    adminViewModel = adminViewModel
                )
                AdminTab.VERIFICATION -> VerificationTab(
                    pendingVerifications = pendingVerifications,
                    featuredArtists = featuredArtists,
                    onVerificationClick = onVerificationClick,
                    onApproveVerificationClick = { adminViewModel.approveVerification(it) },
                    onRejectVerificationClick = { adminViewModel.rejectVerification(it) },
                    onFeatureArtistClick = { user -> adminViewModel.featureArtist(user) },  // Ensure the parameter type matches
                    onRemoveFromFeaturedClick = { adminViewModel.removeArtistFromFeatured(it) },
                    adminViewModel = adminViewModel
                )
            }
        }
    }
}
