package com.rivo.app.ui.screens.home

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rivo.app.data.model.FeaturedContent
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.User
import com.rivo.app.data.remote.MusicCategory
import com.rivo.app.ui.theme.*
import com.rivo.app.ui.viewmodel.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    exploreViewModel: ExploreViewModel,
    musicViewModel: MusicViewModel,
    libraryViewModel: LibraryViewModel,
    sessionViewModel: SessionViewModel,
    followViewModel: FollowViewModel,
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
    val trendingMusic by exploreViewModel.trendingMusic.collectAsState()
    val featuredArtists by exploreViewModel.featuredArtists.collectAsState()
    val newReleases by exploreViewModel.newReleases.collectAsState()
    val isLoading by exploreViewModel.isLoading.collectAsState()
    val featuredBanner by exploreViewModel.featuredBanner.collectAsState()
    val categories by exploreViewModel.categories.collectAsState()
    val favoriteMusic by musicViewModel.favoriteMusic.collectAsState()
    val currentUser by sessionViewModel.currentUser.collectAsState()
    
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentUser) {
        exploreViewModel.refresh()
        currentUser?.id?.let { userId ->
            libraryViewModel.loadUserPlaylists(userId)
            musicViewModel.loadFavorites()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Gradient Magic Backdrop
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkBackground
        ) {
            Box {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(RivoPurple.copy(alpha = 0.2f), DarkBackground)
                        )
                    )
                }
            }
        }

        if (isLoading && trendingMusic.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RivoPink)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Creative Header
                item {
                    HomeHeader(
                        userName = currentUser?.name ?: "Listener",
                        profileImageUrl = currentUser?.profileImageUrl,
                        onNotificationClick = onNotificationClick,
                        onSearchClick = onSearchClick
                    )
                }

                // Moods / Categories
                item {
                    CategoryRow(categories)
                }

                // Featured Hero Section
                item {
                    FeaturedHero(
                        banner = featuredBanner,
                        onPlayClick = {
                            featuredBanner?.id?.let { onMusicClick(it) } // Simplified
                        }
                    )
                }

                // Trending Section
                item {
                    CreativeSectionHeader(
                        title = "Trending Now",
                        subtitle = "Global chart-toppers on Rivo",
                        onSeeAllClick = { onSeeAllClick("trending") }
                    )
                    
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(trendingMusic) { music ->
                            GlassMusicCard(
                                music = music,
                                isFavorite = favoriteMusic.any { it.id == music.id },
                                onClick = { onMusicClick(music.id) }
                            )
                        }
                    }
                }

                // Featured Artists
                item {
                    CreativeSectionHeader(
                        title = "Featured Artists",
                        subtitle = "Rising stars you should know",
                        onSeeAllClick = { onSeeAllClick("artists") }
                    )
                    
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(featuredArtists) { artist ->
                            CircleArtistCard(
                                artist = artist,
                                onClick = { onArtistClick(artist.id) }
                            )
                        }
                    }
                }

                // New Releases Grid Wrap
                item {
                    CreativeSectionHeader(
                        title = "Fresh Hits",
                        subtitle = "Newest uploads from the community",
                        onSeeAllClick = onSeeAllNewReleasesClick
                    )
                }

                items(newReleases.take(6).chunked(2)) { pair ->
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pair.forEach { music ->
                            CompactMusicCard(
                                music = music,
                                modifier = Modifier.weight(1f),
                                onClick = { onMusicClick(music.id) }
                            )
                        }
                        if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun HomeHeader(
    userName: String,
    profileImageUrl: String?,
    onNotificationClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyLarge.copy(color = LightGray)
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = White,
                    letterSpacing = (-1).sp
                )
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.05f))
                    .clickable(onClick = onSearchClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = White, modifier = Modifier.size(20.dp))
            }
            
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.05f))
                    .clickable(onClick = onNotificationClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun CategoryRow(categories: List<MusicCategory>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(categories) { category ->
            Surface(
                onClick = {},
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(category.color)))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = category.title, color = White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun FeaturedHero(banner: FeaturedContent?, onPlayClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(24.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        AsyncImage(
            model = banner?.imageUrl ?: "https://images.unsplash.com/photo-1470225620780-dba8ba36b745",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Text(
                text = "FEATURED",
                style = MaterialTheme.typography.labelSmall.copy(color = RivoPink, fontWeight = FontWeight.Bold)
            )
            Text(
                text = banner?.title ?: "New Sound Vibes",
                style = MaterialTheme.typography.headlineSmall.copy(color = White, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onPlayClick,
                colors = ButtonDefaults.buttonColors(containerColor = White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Listen Now", color = Color.Black, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun CreativeSectionHeader(title: String, subtitle: String, onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(color = White, fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = LightGray)
            )
        }
        Text(
            text = "See all",
            modifier = Modifier.clickable(onClick = onSeeAllClick),
            color = RivoPink,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun GlassMusicCard(music: Music, isFavorite: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            AsyncImage(
                model = music.artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Hover-like Play Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = music.title,
            style = MaterialTheme.typography.bodyLarge.copy(color = White, fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = music.artist,
            style = MaterialTheme.typography.bodySmall.copy(color = LightGray),
            maxLines = 1
        )
    }
}

@Composable
fun CircleArtistCard(artist: User, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(84.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(White.copy(alpha = 0.05f))
        ) {
            AsyncImage(
                model = artist.profileImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artist.name,
            style = MaterialTheme.typography.labelSmall.copy(color = White, fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CompactMusicCard(music: Music, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = music.artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = music.title,
                    style = MaterialTheme.typography.bodyMedium.copy(color = White, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = music.artist,
                    style = MaterialTheme.typography.labelSmall.copy(color = LightGray),
                    maxLines = 1
                )
            }
        }
    }
}
