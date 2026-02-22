package com.rivo.app.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rivo.app.data.model.FeaturedContent
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.User
import com.rivo.app.data.remote.BannerItem
import com.rivo.app.data.remote.MusicCategory
import com.rivo.app.ui.theme.*
import com.rivo.app.ui.viewmodel.*
import java.util.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    exploreViewModel: ExploreViewModel,
    musicViewModel: MusicViewModel,
    libraryViewModel: LibraryViewModel,
    sessionViewModel: SessionViewModel,
    followViewModel: FollowViewModel,
    notificationViewModel: NotificationViewModel,
    onMusicClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onSeeAllClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onExploreClick: () -> Unit,
    onSeeAllNewReleasesClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onPlaylistClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val trendingMusic   by exploreViewModel.trendingMusic.collectAsState()
    val featuredArtists by exploreViewModel.featuredArtists.collectAsState()
    val newReleases     by exploreViewModel.newReleases.collectAsState()
    val isLoading       by exploreViewModel.isLoading.collectAsState()
    val featuredBanner  by exploreViewModel.featuredBanner.collectAsState()
    val categories      by exploreViewModel.categories.collectAsState()
    val banners         by exploreViewModel.banners.collectAsState()
    val currentUser     by sessionViewModel.currentUser.collectAsState()
    val unreadCount     by notificationViewModel.unreadCount.collectAsState()

    // One-time refresh on entry
    LaunchedEffect(Unit) {
        exploreViewModel.refresh()
    }
    LaunchedEffect(currentUser) {
        currentUser?.id?.let { userId ->
            libraryViewModel.loadUserPlaylists(userId)
            musicViewModel.loadFavorites()
        }
    }

    // ── Animated background orbs ─────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    val orbAlpha by infiniteTransition.animateFloat(
        initialValue = 0.10f, targetValue = 0.22f,
        animationSpec = infiniteRepeatable(tween(3200), RepeatMode.Reverse),
        label = "orb1"
    )
    val orbAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.06f, targetValue = 0.16f,
        animationSpec = infiniteRepeatable(tween(4500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "orb2"
    )
    val orbAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0.04f, targetValue = 0.12f,
        animationSpec = infiniteRepeatable(tween(5800), RepeatMode.Reverse),
        label = "orb3"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .drawBehind {
                // Purple glow — top-left
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(RivoPurple.copy(orbAlpha), Color.Transparent),
                        center = Offset(size.width * 0.0f, size.height * 0.0f),
                        radius = size.width * 0.85f
                    ),
                    radius = size.width * 0.85f,
                    center = Offset(size.width * 0.0f, size.height * 0.0f)
                )
                // Pink glow — center-right
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(RivoPink.copy(orbAlpha2), Color.Transparent),
                        center = Offset(size.width, size.height * 0.45f),
                        radius = size.width * 0.65f
                    ),
                    radius = size.width * 0.65f,
                    center = Offset(size.width, size.height * 0.45f)
                )
                // Blue glow — bottom-center
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(RivoBlue.copy(orbAlpha3), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.85f),
                        radius = size.width * 0.55f
                    ),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.5f, size.height * 0.85f)
                )
            }
    ) {

        // ── Loading shimmer ──────────────────────────────────────────────────
        AnimatedVisibility(
            visible = isLoading && trendingMusic.isEmpty() && banners.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut(tween(400))
        ) {
            RivoShimmerSkeleton()
        }

        // ── Main content ─────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = !isLoading || trendingMusic.isNotEmpty() || banners.isNotEmpty(),
            enter = fadeIn(tween(700)),
            exit = fadeOut()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 110.dp)
            ) {
                // Header
                item {
                    RivoHomeHeader(
                        profileImageUrl = currentUser?.profileImageUrl,
                        unreadCount = unreadCount,
                        onNotificationClick = onNotificationClick,
                        onSearchClick = onSearchClick
                    )
                }

                // Genre category pills (backend-driven)
                if (categories.isNotEmpty()) {
                    item { AnimatedCategoryRow(categories) }
                }

                // Hero banner carousel
                item {
                    Spacer(Modifier.height(8.dp))
                    when {
                        banners.isNotEmpty() -> ModernBannerCarousel(
                            banners = banners,
                            onExploreClick = {}
                        )
                        featuredBanner != null -> FeaturedHero(
                            banner = featuredBanner,
                            onPlayClick = { featuredBanner?.id?.let { onMusicClick(it) } }
                        )
                    }
                }

                // Quick Actions
                item { QuickActionRow() }

                // ── Trending Now ─────────────────────────────────────────
                if (trendingMusic.isNotEmpty()) {
                    item {
                        RivoPulsingHeader(
                            title = "🔥 Trending Now",
                            subtitle = "Global chart-toppers on Rivo",
                            onSeeAllClick = { onSeeAllClick("trending") }
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            itemsIndexed(trendingMusic) { index, music ->
                                AnimatedMusicCardEntry(index) {
                                    TrendingMusicCard(
                                        music = music,
                                        rank = index + 1,
                                        onClick = { onMusicClick(music.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Featured Artists ──────────────────────────────────────
                if (featuredArtists.isNotEmpty()) {
                    item {
                        RivoPulsingHeader(
                            title = "✨ Featured Artists",
                            subtitle = "Rising stars you need to hear",
                            onSeeAllClick = { onSeeAllClick("artists") }
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            itemsIndexed(featuredArtists) { index, artist ->
                                AnimatedMusicCardEntry(index) {
                                    PremiumArtistCard(
                                        artist = artist,
                                        isFollowing = false,
                                        onFollowClick = { followViewModel.toggleFollow(artist.id) },
                                        onClick = { onArtistClick(artist.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Fresh Hits ────────────────────────────────────────────
                if (newReleases.isNotEmpty()) {
                    item {
                        RivoPulsingHeader(
                            title = "💿 Fresh Hits",
                            subtitle = "Hot off the press from creators",
                            onSeeAllClick = onSeeAllNewReleasesClick
                        )
                    }
                    itemsIndexed(newReleases.take(12)) { index, music ->
                        AnimatedListEntry(index) {
                            FreshHitListItem(
                                music = music,
                                rank = index + 1,
                                onClick = { onMusicClick(music.id) }
                            )
                        }
                    }
                }

                // ── Empty state if everything is empty post-load ────────
                if (!isLoading && trendingMusic.isEmpty() && featuredArtists.isEmpty() && newReleases.isEmpty()) {
                    item { RivoEmptyState() }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// BEAUTIFUL HOME HEADER  — no username, time-based greeting + notification badge
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun RivoHomeHeader(
    profileImageUrl: String?,
    unreadCount: Int,
    onNotificationClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    // Time-based greeting computed once per composition
    val (greetingLine1, greetingLine2, greetingEmoji) = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11  -> Triple("Good Morning", "Ready to discover new music?", "☀️")
            in 12..16 -> Triple("Good Afternoon", "What's on your playlist today?", "🎵")
            in 17..20 -> Triple("Good Evening", "Unwind with your favourite tunes", "🌆")
            else      -> Triple("Good Night", "Late night vibes await you", "🌙")
        }
    }

    // Animate greeting in from below
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 52.dp, start = 24.dp, end = 20.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Greeting text
        Column(modifier = Modifier.weight(1f)) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 }
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = greetingEmoji,
                            fontSize = 20.sp
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = greetingLine1,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = White,
                                letterSpacing = (-0.8).sp
                            )
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = greetingLine2,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = LightGray,
                            letterSpacing = 0.2.sp
                        )
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        // Action buttons row
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search button
            RivoHeaderButton(icon = Icons.Default.Search, onClick = onSearchClick)

            // Notification button with animated badge
            Box {
                RivoHeaderButton(
                    icon = Icons.Default.Notifications,
                    onClick = onNotificationClick,
                    hasGlow = unreadCount > 0
                )
                if (unreadCount > 0) {
                    NotificationBadge(
                        count = unreadCount,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }

            // Profile avatar (if available)
            if (!profileImageUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.sweepGradient(listOf(RivoPurple, RivoPink, RivoCyan, RivoPurple)),
                                radius = size.minDimension / 2f
                            )
                        }
                ) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "Profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(34.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                    )
                }
            }
        }
    }
}

// ─── Notification Badge with pulse animation ──────────────────────────────────

@Composable
fun NotificationBadge(count: Int, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.22f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "badge_scale"
    )

    Box(
        modifier = modifier
            .scale(pulseScale)
            .offset(x = 4.dp, y = (-4).dp)
            .size(18.dp)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(RivoPink, Color(0xFFFF6B6B)))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            color = White,
            fontSize = if (count > 9) 7.sp else 9.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Header icon button with optional pink glow ───────────────────────────────

@Composable
fun RivoHeaderButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    hasGlow: Boolean = false
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.85f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "hdr_btn_scale"
    )

    Box(
        modifier = Modifier
            .size(42.dp)
            .scale(scale)
            .drawBehind {
                if (hasGlow) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(RivoPink.copy(0.45f), Color.Transparent)
                        ),
                        radius = size.minDimension * 0.9f
                    )
                }
            }
            .clip(CircleShape)
            .background(
                if (hasGlow)
                    Brush.radialGradient(listOf(RivoPink.copy(0.15f), White.copy(0.05f)))
                else
                    Brush.radialGradient(listOf(White.copy(0.07f), White.copy(0.04f)))
            )
            .border(
                width = 1.dp,
                color = if (hasGlow) RivoPink.copy(0.5f) else White.copy(0.1f),
                shape = CircleShape
            )
            .clickable { pressed = true; onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (hasGlow) RivoPink else White,
            modifier = Modifier.size(20.dp)
        )
    }

    LaunchedEffect(pressed) {
        if (pressed) { kotlinx.coroutines.delay(120); pressed = false }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// SHIMMER SKELETON  — beautiful loading placeholder
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun RivoShimmerSkeleton() {
    val shimmerAnim by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "shimmer_x"
    )
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(0.04f),
            Color.White.copy(0.11f),
            Color.White.copy(0.04f)
        ),
        start = Offset(shimmerAnim - 300f, 0f),
        end   = Offset(shimmerAnim, 0f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 52.dp)
    ) {
        // Header skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                ShimmerBox(width = 160.dp, height = 28.dp, brush = shimmerBrush)
                Spacer(Modifier.height(8.dp))
                ShimmerBox(width = 220.dp, height = 16.dp, brush = shimmerBrush)
            }
            ShimmerBox(width = 42.dp, height = 42.dp, brush = shimmerBrush, isCircle = true)
            Spacer(Modifier.width(10.dp))
            ShimmerBox(width = 42.dp, height = 42.dp, brush = shimmerBrush, isCircle = true)
        }
        Spacer(Modifier.height(24.dp))

        // Category pills
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(4) {
                ShimmerBox(width = 80.dp, height = 34.dp, brush = shimmerBrush, cornerRadius = 20.dp)
            }
        }
        Spacer(Modifier.height(20.dp))

        // Banner skeleton
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(),
            height = 220.dp,
            brush = shimmerBrush,
            cornerRadius = 28.dp
        )
        Spacer(Modifier.height(24.dp))

        // Section header
        ShimmerBox(width = 160.dp, height = 22.dp, brush = shimmerBrush)
        Spacer(Modifier.height(14.dp))

        // Music cards row
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(3) {
                Column {
                    ShimmerBox(width = 150.dp, height = 150.dp, brush = shimmerBrush, cornerRadius = 20.dp)
                    Spacer(Modifier.height(8.dp))
                    ShimmerBox(width = 120.dp, height = 14.dp, brush = shimmerBrush)
                    Spacer(Modifier.height(4.dp))
                    ShimmerBox(width = 90.dp, height = 12.dp, brush = shimmerBrush)
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        // List items
        repeat(4) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(width = 52.dp, height = 52.dp, brush = shimmerBrush, cornerRadius = 12.dp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f), height = 14.dp, brush = shimmerBrush)
                    Spacer(Modifier.height(6.dp))
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f), height = 12.dp, brush = shimmerBrush)
                }
            }
        }
    }
}

@Composable
private fun ShimmerBox(
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = 0.dp,
    height: androidx.compose.ui.unit.Dp,
    brush: Brush,
    cornerRadius: androidx.compose.ui.unit.Dp = 8.dp,
    isCircle: Boolean = false
) {
    val shape = if (isCircle) CircleShape else RoundedCornerShape(cornerRadius)
    val sizedModifier = if (width > 0.dp)
        modifier.size(width, height)
    else
        modifier.height(height)
    Box(
        modifier = sizedModifier
            .clip(shape)
            .background(brush)
    )
}

// ═════════════════════════════════════════════════════════════════════════════
// EMPTY STATE
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun RivoEmptyState() {
    val infiniteTransition = rememberInfiniteTransition(label = "empty_pulse")
    val emojiScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "emoji_scale"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎵", fontSize = 64.sp, modifier = Modifier.scale(emojiScale))
            Spacer(Modifier.height(20.dp))
            Text(
                "Nothing here yet",
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Check back soon for trending\nmusic and featured artists",
                color = LightGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// STAGGERED ENTRY ANIMATIONS
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun AnimatedMusicCardEntry(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 65L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(420)) + slideInHorizontally(tween(420)) { it / 3 }
    ) { content() }
}

@Composable
fun AnimatedListEntry(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 55L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(380)) + slideInVertically(tween(380)) { it / 4 }
    ) { content() }
}

// ─── Animated Category Row ────────────────────────────────────────────────────

@Composable
fun AnimatedCategoryRow(categories: List<MusicCategory>) {
    var selectedId by remember { mutableStateOf<String?>(null) }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(categories) { index, category ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(index * 60L)
                visible = true
            }
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 2 }
            ) {
                val isSelected = selectedId == category.id
                val catColor = remember(category.color) {
                    try { Color(android.graphics.Color.parseColor(category.color)) } catch (e: Exception) { RivoPurple }
                }
                val bgAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0f,
                    animationSpec = tween(250),
                    label = "cat_bg"
                )
                Surface(
                    onClick = { selectedId = if (isSelected) null else category.id },
                    color = Color.Transparent,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) catColor else White.copy(alpha = 0.12f)
                    )
                ) {
                    Box(
                        modifier = Modifier.background(
                            Brush.horizontalGradient(
                                listOf(catColor.copy(alpha = bgAlpha * 0.3f), catColor.copy(alpha = bgAlpha * 0.15f))
                            ),
                            RoundedCornerShape(20.dp)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(catColor)
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                            Text(
                                text = category.title,
                                color = if (isSelected) White else LightGray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// PULSING SECTION HEADER
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun RivoPulsingHeader(title: String, subtitle: String, onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 16.dp, top = 28.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = White,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp
                )
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = LightGray)
            )
        }
        TextButton(onClick = onSeeAllClick) {
            Text("See all", color = RivoPink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Icon(Icons.Default.ChevronRight, null, tint = RivoPink, modifier = Modifier.size(16.dp))
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// FEATURED HERO FALLBACK
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun FeaturedHero(banner: FeaturedContent?, onPlayClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(28.dp))
    ) {
        AsyncImage(
            model = banner?.imageUrl ?: "https://images.unsplash.com/photo-1470225620780-dba8ba36b745",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.85f)))))
        Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Surface(color = RivoPink.copy(0.9f), shape = RoundedCornerShape(6.dp)) {
                Text("FEATURED", Modifier.padding(horizontal = 8.dp, vertical = 3.dp), color = White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Text(banner?.title ?: "New Sound Vibes", style = MaterialTheme.typography.headlineSmall.copy(color = White, fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onPlayClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Box(Modifier.background(Brush.horizontalGradient(listOf(RivoPurple, RivoPink)), RoundedCornerShape(14.dp)).padding(horizontal = 18.dp, vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, null, tint = White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Listen Now", color = White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ─── Backward-compat aliases ─────────────────────────────────────────────────
@Composable
fun AnimatedHomeHeader(
    userName: String,
    profileImageUrl: String?,
    onNotificationClick: () -> Unit,
    onSearchClick: () -> Unit
) = RivoHomeHeader(
    profileImageUrl = profileImageUrl,
    unreadCount = 0,
    onNotificationClick = onNotificationClick,
    onSearchClick = onSearchClick
)

@Composable
fun CreativeSectionHeader(title: String, subtitle: String, onSeeAllClick: () -> Unit) =
    RivoPulsingHeader(title, subtitle, onSeeAllClick)

@Composable
fun CategoryRow(categories: List<MusicCategory>) = AnimatedCategoryRow(categories)

@Composable
fun HomeHeader(
    userName: String,
    profileImageUrl: String?,
    onNotificationClick: () -> Unit,
    onSearchClick: () -> Unit
) = RivoHomeHeader(profileImageUrl = profileImageUrl, unreadCount = 0, onNotificationClick = onNotificationClick, onSearchClick = onSearchClick)

@Composable
fun FullScreenLoader() = RivoShimmerSkeleton()
