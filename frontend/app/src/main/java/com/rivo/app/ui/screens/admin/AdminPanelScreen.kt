package com.rivo.app.ui.screens.admin

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rivo.app.data.model.User
import com.rivo.app.ui.theme.*
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

    val infiniteTransition = rememberInfiniteTransition(label = "panel_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f, targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(4000, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "glow"
    )

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        // Ambient glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(RivoBlue.copy(alpha = glowAlpha), DarkBackground)
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Premium header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 44.dp, start = 20.dp, end = 20.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.06f))
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = White, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Admin Panel",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black, color = White, letterSpacing = (-0.5).sp
                        )
                    )
                    Text(
                        "Manage the platform",
                        style = MaterialTheme.typography.bodySmall.copy(color = LightGray)
                    )
                }

                // Pending badge
                val totalPending = pendingVerifications.size + pendingMusic.size
                if (totalPending > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = WarningYellow.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, WarningYellow.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, null, tint = WarningYellow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$totalPending", style = MaterialTheme.typography.labelMedium.copy(color = WarningYellow, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // Premium Tab Switcher
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(White.copy(alpha = 0.04f), RoundedCornerShape(26.dp))
                    .padding(4.dp)
            ) {
                data class TabData(val tab: AdminTab, val icon: ImageVector, val label: String)
                val tabs = listOf(
                    TabData(AdminTab.DASHBOARD, Icons.Outlined.Dashboard, "Home"),
                    TabData(AdminTab.USERS, Icons.Outlined.People, "Users"),
                    TabData(AdminTab.MUSIC, Icons.Outlined.MusicNote, "Music"),
                    TabData(AdminTab.VERIFICATION, Icons.Outlined.Verified, "Verify")
                )

                tabs.forEach { (tab, icon, label) ->
                    val isSelected = selectedTab == tab
                    val bgColor by animateColorAsState(
                        if (isSelected) RivoBlue.copy(alpha = 0.2f) else Color.Transparent,
                        label = "tab_bg"
                    )
                    val hasBadge = (tab == AdminTab.VERIFICATION && pendingVerifications.isNotEmpty()) ||
                                   (tab == AdminTab.MUSIC && pendingMusic.isNotEmpty())

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(22.dp))
                            .background(bgColor)
                            .clickable { selectedTab = tab },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BadgedBox(
                                badge = {
                                    if (hasBadge) {
                                        Badge(containerColor = WarningYellow) {
                                            Text(
                                                if (tab == AdminTab.VERIFICATION) pendingVerifications.size.toString()
                                                else pendingMusic.size.toString(),
                                                style = MaterialTheme.typography.labelSmall.copy(color = DarkBackground)
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    icon, null,
                                    tint = if (isSelected) RivoBlue else LightGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) White else LightGray,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            // Tab Content
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    slideInHorizontally { it / 3 } + fadeIn() togetherWith
                    slideOutHorizontally { -it / 3 } + fadeOut()
                },
                label = "admin_tab"
            ) { tab ->
                when (tab) {
                    AdminTab.DASHBOARD -> DashboardTab(
                        adminStats = stats,
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
                        onEditMusicClick = { },
                        adminViewModel = adminViewModel
                    )
                    AdminTab.VERIFICATION -> VerificationTab(
                        pendingVerifications = pendingVerifications,
                        featuredArtists = featuredArtists,
                        onVerificationClick = onVerificationClick,
                        onApproveVerificationClick = { adminViewModel.approveVerification(it) },
                        onRejectVerificationClick = { adminViewModel.rejectVerification(it) },
                        onFeatureArtistClick = { user -> adminViewModel.featureArtist(user) },
                        onRemoveFromFeaturedClick = { adminViewModel.removeArtistFromFeatured(it) },
                        adminViewModel = adminViewModel
                    )
                }
            }
        }
    }
}
