package com.rivo.app.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.User
import com.rivo.app.data.remote.BannerItem
import com.rivo.app.ui.theme.*
import kotlinx.coroutines.delay

// ═════════════════════════════════════════════════════════════════════════════
// MODERN BANNER CAROUSEL
// ═════════════════════════════════════════════════════════════════════════════

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ModernBannerCarousel(
    banners: List<BannerItem>,
    onExploreClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (banners.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { banners.size })

    // Auto-scroll every 4.5 s
    LaunchedEffect(pagerState.pageCount) {
        if (pagerState.pageCount <= 1) return@LaunchedEffect
        while (true) {
            delay(4500)
            pagerState.animateScrollToPage(
                (pagerState.currentPage + 1) % pagerState.pageCount,
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            )
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .padding(horizontal = 20.dp),
            pageSpacing = 12.dp
        ) { page ->
            val banner = banners[page]
            // Scale-in effect per page
            val pageOffset = (pagerState.currentPage - page) +
                    pagerState.currentPageOffsetFraction
            val scale = lerp(0.93f, 1f, 1f - pageOffset.coerceIn(-1f, 1f).let {
                if (it < 0) -it else it
            })
            BannerCard(
                banner = banner,
                scale = scale,
                onExploreClick = onExploreClick
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Pill dot indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(banners.size) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateDpAsState(
                    targetValue = if (isSelected) 24.dp else 6.dp,
                    animationSpec = tween(350, easing = FastOutSlowInEasing),
                    label = "dot_w"
                )
                val dotColor by animateColorAsState(
                    targetValue = if (isSelected) RivoPink else LightGray.copy(0.3f),
                    animationSpec = tween(300),
                    label = "dot_c"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }
    }
}

@Composable
private fun BannerCard(banner: BannerItem, scale: Float, onExploreClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .scale(scale)
            .clip(RoundedCornerShape(28.dp))
    ) {
        AsyncImage(
            model = banner.imageUrl,
            contentDescription = banner.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Multi-stop gradient for depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(0.18f),
                        0.4f to Color.Transparent,
                        1f to Color.Black.copy(0.88f)
                    )
                )
        )
        // Shimmer side accent
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(RivoPurple.copy(0.2f), Color.Transparent, RivoPink.copy(0.1f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(22.dp)
        ) {
            // Glowing pill tag
            GlowingPill(text = "FEATURED")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = banner.title ?: "",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = White,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = banner.subtitle ?: "",
                style = MaterialTheme.typography.bodyMedium.copy(color = White.copy(0.75f)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Play Now button
                Button(
                    onClick = { onExploreClick(banner.actionUrl) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(listOf(RivoPurple, RivoPink)),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, null, tint = White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Listen Now", color = White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
                // Info ghost button
                OutlinedButton(
                    onClick = {},
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = White),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, White.copy(0.35f)),
                    modifier = Modifier.height(38.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Info, null, tint = White, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

@Composable
private fun GlowingPill(text: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pill_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "glow"
    )
    Box(
        modifier = Modifier
            .background(RivoPink.copy(glowAlpha * 0.9f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + fraction * (stop - start)

@Composable
fun CategoryRow(
    categories: List<com.rivo.app.data.remote.MusicCategory>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        items(categories) { category ->
            CategoryChip(category = category, onClick = { onCategoryClick(category.id) })
        }
    }
}

@Composable
private fun CategoryChip(category: com.rivo.app.data.remote.MusicCategory, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "chip_scale"
    )

    val bgColor = remember(category.color) {
        try {
            Color(android.graphics.Color.parseColor(category.color))
        } catch (_: Exception) {
            RivoPink
        }
    }

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(bgColor.copy(0.7f), bgColor.copy(0.4f))))
            .border(1.dp, Color.White.copy(0.12f), RoundedCornerShape(18.dp))
            .clickable {
                isPressed = true
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = when(category.icon.lowercase()) {
                    "mic" -> Icons.Default.Mic
                    "album" -> Icons.Default.Album
                    "spa" -> Icons.Default.Spa
                    "trending" -> Icons.Default.TrendingUp
                    else -> Icons.Default.MusicNote
                },
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(15.dp)
            )
            Text(category.title, color = White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
    LaunchedEffect(isPressed) {
        if (isPressed) { delay(130); isPressed = false }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// TRENDING MUSIC CARD  (with rank badge)
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun TrendingMusicCard(
    music: Music,
    rank: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    Column(
        modifier = modifier
            .width(158.dp)
            .scale(scale)
            .clickable { isPressed = true; onClick() }
    ) {
        Box(modifier = Modifier.size(158.dp).clip(RoundedCornerShape(22.dp))) {

            // Album art
            AsyncImage(
                model = music.artworkUri ?: "https://images.unsplash.com/photo-1470225620780-dba8ba36b745",
                contentDescription = music.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Bottom gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.45f to Color.Transparent,
                            1f to Color.Black.copy(0.7f)
                        )
                    )
            )

            // Gradient play button bottom-right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(RivoPurple, RivoPink))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, "Play", tint = White, modifier = Modifier.size(20.dp))
            }

            // Rank badge top-left
            if (rank in 1..3) {
                val rankColor = when (rank) {
                    1 -> Color(0xFFFFD700)
                    2 -> Color(0xFFC0C0C0)
                    else -> Color(0xFFCD7F32)
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(rankColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text("#$rank", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            } else if (rank > 3) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(0.55f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text("#$rank", color = LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Play count badge top-right
            if (music.playCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(0.55f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Default.Headphones, null, tint = RivoPink, modifier = Modifier.size(10.dp))
                        Text(formatCount(music.playCount), color = White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(9.dp))
        Text(
            music.title ?: "",
            style = MaterialTheme.typography.bodyLarge.copy(color = White, fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            music.artist ?: "",
            style = MaterialTheme.typography.bodySmall.copy(color = LightGray),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) { delay(140); isPressed = false }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// PREMIUM ARTIST CARD
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun PremiumArtistCard(
    artist: User,
    isFollowing: Boolean,
    followerCount: Int,
    onFollowClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var localFollowing by remember(isFollowing) { mutableStateOf(isFollowing) }
    var isPressed by remember { mutableStateOf(false) }
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "artist_scale"
    )

    // Animated gradient ring rotation
    val infiniteTransition = rememberInfiniteTransition(label = "ring_anim")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing)),
        label = "ring_rot"
    )

    val displayName = if (artist.fullName.isNotBlank()) artist.fullName else artist.name

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(136.dp) // Uniform width
            .scale(cardScale)
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
            .clickable { isPressed = true; onClick() }
            .padding(top = 18.dp, bottom = 14.dp, start = 8.dp, end = 8.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(94.dp)) {
            // Rotating gradient ring
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(rotationZ = ringRotation)
                    .drawBehind {
                        val sweepGradient = Brush.sweepGradient(
                            listOf(RivoPurple, RivoPink, RivoCyan, RivoPurple)
                        )
                        drawCircle(brush = sweepGradient, radius = size.minDimension / 2.1f, style = Stroke(width = 2.5.dp.toPx()))
                    }
            )
            // Avatar
            AsyncImage(
                model = artist.profileImageUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde",
                contentDescription = displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp).clip(CircleShape)
            )
            // Verified badge
            if (artist.isVerified) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(RivoBlue, RivoCyan))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Verified, "Verified", tint = White, modifier = Modifier.size(14.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            displayName,
            style = MaterialTheme.typography.labelLarge.copy(
                color = White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                letterSpacing = (-0.2).sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )
        val displayFollowers = if (followerCount > 0) followerCount else artist.followerCount
        if (displayFollowers > 0) {
            Text(
                "${formatCount(displayFollowers)} fans",
                style = MaterialTheme.typography.bodySmall.copy(color = LightGray, fontSize = 11.sp),
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Animated follow button visibility
        AnimatedVisibility(
            visible = !localFollowing,
            enter = fadeIn(tween(300)) + expandVertically(tween(300)),
            exit = fadeOut(tween(250)) + shrinkVertically(tween(250)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(CircleShape)
                    .background(RivoPink.copy(0.12f))
                    .border(1.dp, RivoPink.copy(0.8f), CircleShape)
                    .clickable {
                        localFollowing = true
                        onFollowClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Add, null, tint = RivoPink, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Follow",
                        color = RivoPink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        if (localFollowing) {
            Spacer(modifier = Modifier.height(34.dp))
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) { delay(130); isPressed = false }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// FRESH HIT LIST ITEM
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun FreshHitListItem(
    music: Music,
    rank: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val bgAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.14f else 0.04f,
        animationSpec = tween(180),
        label = "list_bg"
    )
    val translationX by animateFloatAsState(
        targetValue = if (isPressed) 4f else 0f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "list_tx"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .offset(x = translationX.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(White.copy(bgAlpha), White.copy(bgAlpha / 2f))
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(White.copy(0.07f), Color.Transparent)
                ),
                RoundedCornerShape(18.dp)
            )
            .clickable { isPressed = true; onClick() }
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank number
        if (rank > 0) {
            Text(
                text = if (rank < 10) "0$rank" else "$rank",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = if (rank <= 3) RivoPink else DarkGray,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                ),
                modifier = Modifier.width(28.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        // Album art with glow on top 3
        Box(modifier = Modifier.size(52.dp)) {
            if (rank <= 3 && rank > 0) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(RivoPink.copy(0.4f), Color.Transparent)
                                ),
                                radius = size.minDimension * 0.8f
                            )
                        }
                )
            }
            AsyncImage(
                model = music.artworkUri ?: "https://images.unsplash.com/photo-1470225620780-dba8ba36b745",
                contentDescription = music.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                music.title ?: "",
                style = MaterialTheme.typography.bodyLarge.copy(color = White, fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    music.artist ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(color = LightGray),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (!music.genre.isNullOrBlank()) {
                    Text(" · ", color = DarkGray, fontSize = 11.sp)
                    Text(
                        music.genre,
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkGray, fontSize = 11.sp),
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right side: duration + play count
        Column(horizontalAlignment = Alignment.End) {
            if (music.duration > 0) {
                Text(
                    formatDuration(music.duration),
                    color = LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            if (music.playCount > 0) {
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(Icons.Default.Headphones, null, tint = RivoPink, modifier = Modifier.size(11.dp))
                    Text(formatCount(music.playCount), color = RivoPink, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Play orb
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(RivoPurple.copy(0.6f), RivoPink.copy(0.6f)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, "Play", tint = White, modifier = Modifier.size(20.dp))
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) { delay(130); isPressed = false }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// UTILITIES
// ═════════════════════════════════════════════════════════════════════════════

fun formatCount(count: Int): String = when {
    count >= 1_000_000 -> "%.1fM".format(count / 1_000_000f)
    count >= 1_000     -> "%.1fK".format(count / 1_000f)
    else               -> "$count"
}

fun formatDuration(millis: Long): String {
    val s = millis / 1000
    return "%d:%02d".format(s / 60, s % 60)
}

// ═════════════════════════════════════════════════════════════════════════════
// LEGACY COMPAT — kept so other files compiling against old names don't break
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun GlassMusicCard(music: Music, isFavorite: Boolean, onClick: () -> Unit) =
    TrendingMusicCard(music = music, onClick = onClick)

@Composable
fun CircleArtistCard(artist: User, onClick: () -> Unit) =
    PremiumArtistCard(artist = artist, isFollowing = false, followerCount = artist.followerCount, onFollowClick = {}, onClick = onClick)

@Composable
fun CompactMusicCard(music: Music, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = White.copy(0.05f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = music.artworkUri, contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(music.title ?: "", style = MaterialTheme.typography.bodyMedium.copy(color = White, fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(music.artist ?: "", style = MaterialTheme.typography.labelSmall.copy(color = LightGray), maxLines = 1)
            }
        }
    }
}
