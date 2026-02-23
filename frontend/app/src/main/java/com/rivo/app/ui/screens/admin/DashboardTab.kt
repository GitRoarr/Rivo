package com.rivo.app.ui.screens.admin

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.rivo.app.data.remote.AdminStatsResponse
import com.rivo.app.ui.theme.*

@Composable
fun DashboardTab(
    adminStats: AdminStatsResponse?,
    pendingVerifications: Int,
    pendingMusicApprovals: Int,
    onFeaturedContentClick: () -> Unit,
    onVerificationTabClick: () -> Unit,
    onMusicTabClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "admin_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "glow"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(DarkBackground),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                // Ambient glow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(RivoBlue.copy(alpha = glowAlpha), DarkBackground)
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        "System Overview",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = RivoBlue,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Platform Performance",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = White,
                            fontWeight = FontWeight.Black
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Real-time analytics and management",
                        style = MaterialTheme.typography.bodyMedium.copy(color = LightGray)
                    )
                }
            }
        }

        // Stats Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AdminStatCard(
                        title = "Total Users",
                        value = formatAdminNumber(adminStats?.totalUsers ?: 0),
                        subtext = "${adminStats?.newUsersToday ?: 0} today",
                        icon = Icons.Filled.Group,
                        color = RivoPurple,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        title = "Total Songs",
                        value = formatAdminNumber(adminStats?.totalMusic ?: 0),
                        subtext = "${adminStats?.newMusicToday ?: 0} today",
                        icon = Icons.Filled.MusicNote,
                        color = RivoBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AdminStatCard(
                        title = "Total plays",
                        value = formatAdminNumber(adminStats?.totalPlays ?: 0),
                        subtext = "Platform wide",
                        icon = Icons.Filled.PlayArrow,
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        title = "Artists",
                        value = formatAdminNumber(adminStats?.totalArtists ?: 0),
                        subtext = "${adminStats?.totalListeners ?: 0} listeners",
                        icon = Icons.Filled.Mic,
                        color = RivoPink,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Alerts Section
        if (pendingVerifications > 0 || pendingMusicApprovals > 0) {
            item {
                Text(
                    "PENDING ACTIONS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = LightGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 12.dp)
                )
            }

            if (pendingVerifications > 0) {
                item {
                    AlertActionCard(
                        icon = Icons.Filled.VerifiedUser,
                        title = "Verification Requests",
                        subtitle = "$pendingVerifications artists awaiting review",
                        badgeCount = pendingVerifications,
                        accentColor = WarningYellow,
                        onClick = onVerificationTabClick,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }

            if (pendingMusicApprovals > 0) {
                item {
                    AlertActionCard(
                        icon = Icons.Filled.QueueMusic,
                        title = "Content Moderation",
                        subtitle = "$pendingMusicApprovals songs awaiting approval",
                        badgeCount = pendingMusicApprovals,
                        accentColor = RivoPink,
                        onClick = onMusicTabClick,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Recent Activity - Users
        adminStats?.recentUsers?.let { users ->
            if (users.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Recent Users",
                        onSeeAllClick = { /* Navigate to users tab */ }
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        users.forEach { user ->
                            RecentUserChip(user)
                        }
                    }
                }
            }
        }

        // Recent Activity - Music
        adminStats?.recentMusic?.let { music ->
            if (music.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Recently Uploaded",
                        onSeeAllClick = onMusicTabClick
                    )
                }
                items(music) { track ->
                    RecentMusicItem(track, modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
                }
            }
        }

        // Quick Controls
        item {
            Text(
                "QUICK CONTROLS",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = LightGray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 12.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionIcon(
                    icon = Icons.Filled.Star,
                    label = "Feature",
                    color = Color(0xFFF59E0B),
                    onClick = onFeaturedContentClick,
                    modifier = Modifier.weight(1f)
                )
                QuickActionIcon(
                    icon = Icons.Filled.Shield,
                    label = "Verified",
                    color = RivoBlue,
                    onClick = onVerificationTabClick,
                    modifier = Modifier.weight(1f)
                )
                QuickActionIcon(
                    icon = Icons.Filled.Rule,
                    label = "Manual",
                    color = RivoPurple,
                    onClick = onMusicTabClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(
                color = White,
                fontWeight = FontWeight.Black
            )
        )
        TextButton(onClick = onSeeAllClick) {
            Text("See All", color = RivoBlue, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = White.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, White.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
                Icon(Icons.Default.TrendingUp, null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                )
            )
            Text(
                title,
                style = MaterialTheme.typography.labelMedium.copy(color = LightGray)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                subtext,
                style = MaterialTheme.typography.labelSmall.copy(color = color.copy(alpha = 0.8f))
            )
        }
    }
}

@Composable
private fun RecentUserChip(user: com.rivo.app.data.model.User) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = White.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    user.name.take(1).uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(color = White, fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(user.name, style = MaterialTheme.typography.labelLarge.copy(color = White, fontWeight = FontWeight.Bold))
                Text(user.userType.name, style = MaterialTheme.typography.labelSmall.copy(color = LightGray))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
private fun RecentMusicItem(track: com.rivo.app.data.model.Music, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = White.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MusicNote, null, tint = RivoBlue)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(track.title ?: "", style = MaterialTheme.typography.bodyLarge.copy(color = White, fontWeight = FontWeight.Bold))
                Text(track.artist ?: "", style = MaterialTheme.typography.bodySmall.copy(color = LightGray))
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(RivoBlue.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(track.genre ?: "Music", color = RivoBlue, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun QuickActionIcon(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = color.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
        ) {
            Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = LightGray, fontWeight = FontWeight.Medium))
    }
}

@Composable
private fun AlertActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeCount: Int,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = accentColor.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(color = White, fontWeight = FontWeight.Bold))
                Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = LightGray))
            }

            // Badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    badgeCount.toString(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = DarkBackground,
                        fontWeight = FontWeight.Black
                    )
                )
            }
        }
    }
}


@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = White.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, White.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        Brush.linearGradient(gradient.map { it.copy(alpha = 0.2f) })
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = gradient.first(), modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = White,
                    fontWeight = FontWeight.Black
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelMedium.copy(color = LightGray)
            )
        }
    }
}

@Composable
private fun AlertActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeCount: Int,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = accentColor.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(color = White, fontWeight = FontWeight.Bold))
                Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = LightGray))
            }

            // Badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    badgeCount.toString(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = DarkBackground,
                        fontWeight = FontWeight.Black
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(Icons.Default.ChevronRight, null, tint = accentColor.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun PremiumActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = White.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(gradient.map { it.copy(alpha = 0.15f) })
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = gradient.first(), modifier = Modifier.size(26.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall.copy(color = LightGray)
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = DarkGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun formatAdminNumber(number: Int): String {
    return when {
        number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format("%.1fK", number / 1_000.0)
        else -> number.toString()
    }
}
