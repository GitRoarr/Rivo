package com.rivo.app.ui.screens.explore

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.User
import com.rivo.app.data.remote.MusicCategory
import com.rivo.app.ui.theme.*
import com.rivo.app.ui.viewmodel.ExploreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onSearchClick: () -> Unit,
    onMusicClick: (Music) -> Unit,
    onArtistClick: (User) -> Unit,
    onViewAllTrendingClick: () -> Unit,
    onViewAllArtistsClick: () -> Unit,
    onViewAllNewReleasesClick: () -> Unit,
    onPlayFeaturedClick: (String) -> Unit,
    onExploreClick: () -> Unit,
    exploreViewModel: ExploreViewModel
) {
    val artists by exploreViewModel.artists.collectAsState()
    val songs by exploreViewModel.songs.collectAsState()
    val categories by exploreViewModel.categories.collectAsState()
    val isLoading by exploreViewModel.isLoading.collectAsState()
    val selectedCategory by exploreViewModel.selectedCategory.collectAsState()
    val categoryMusic by exploreViewModel.categoryMusic.collectAsState()
    val categoryLoading by exploreViewModel.categoryLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Dynamic Glowing Background
        MeshBackground()

        // If a category is selected, show category detail view
        if (selectedCategory != null) {
            CategoryDetailView(
                category = selectedCategory!!,
                music = categoryMusic,
                isLoading = categoryLoading,
                onBackClick = { exploreViewModel.clearSelectedCategory() },
                onMusicClick = onMusicClick
            )
        } else {
            // Main explore view
            if (isLoading && songs.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RivoPurple)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header & Creative Search
                    item(span = { GridItemSpan(2) }) {
                        ExploreTopSection(onSearchClick)
                    }

                    // Trending Artists (Horizontal)
                    item(span = { GridItemSpan(2) }) {
                        Column {
                            SectionLabel("Featured Artists")
                            // De-duplicate artists by ID to prevent duplicates
                            val uniqueArtists = remember(artists) {
                                artists.distinctBy { it.id }
                            }
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(uniqueArtists, key = { it.id }) { artist ->
                                    ArtistCircleItem(artist, onClick = { onArtistClick(artist) })
                                }
                            }
                        }
                    }

                    // Moods & Categories Heading
                    item(span = { GridItemSpan(2) }) {
                        SectionLabel("Browse Categories")
                    }

                    // Categories Grid - now clickable
                    items(categories) { category ->
                        CategoryCard(
                            category = category,
                            onClick = { exploreViewModel.selectCategory(category) }
                        )
                    }

                    // Recommendation Section
                    item(span = { GridItemSpan(2) }) {
                        SectionLabel("Discover Daily")
                    }

                    items(songs.take(10)) { music ->
                        ModernMusicGridItem(music, onClick = { onMusicClick(music) })
                    }
                }
            }
        }
    }
}

@Composable
fun MeshBackground() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse)
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(RivoPink.copy(alpha = alpha), Color.Transparent),
                center = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.2f),
                radius = 400.dp.toPx()
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(RivoPurple.copy(alpha = alpha), Color.Transparent),
                center = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.5f),
                radius = 500.dp.toPx()
            )
        )
    }
}

@Composable
fun ExploreTopSection(onSearchClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 12.dp)
    ) {
        Text(
            text = "Explore",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                color = White,
                letterSpacing = (-1).sp
            )
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Creative Search Bar
        Surface(
            onClick = onSearchClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = White.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, White.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = LightGray)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Artists, songs, or podcasts", color = LightGray, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        style = MaterialTheme.typography.titleMedium.copy(color = White, fontWeight = FontWeight.Bold)
    )
}

@Composable
fun ArtistCircleItem(artist: User, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(80.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = DarkSurface,
            border = BorderStroke(2.dp, Brush.linearGradient(BrandGradient))
        ) {
            AsyncImage(
                model = artist.profileImageUrl ?: artist.profilePictureUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artist.name,
            style = MaterialTheme.typography.labelMedium.copy(color = White, textAlign = TextAlign.Center),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CategoryCard(category: MusicCategory, onClick: () -> Unit) {
    val color = remember(category.color) { Color(android.graphics.Color.parseColor(category.color)) }
    
    Box(
        modifier = Modifier
            .padding(horizontal = 24.dp) // Adjusted padding to align with grid
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(color.copy(alpha = 0.7f), color)))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = category.title,
            style = MaterialTheme.typography.titleMedium.copy(color = White, fontWeight = FontWeight.Black),
            modifier = Modifier.align(Alignment.TopStart)
        )
        
        Icon(
            imageVector = when(category.icon) {
                "mic" -> Icons.Default.Mic
                "album" -> Icons.Default.Album
                "spa" -> Icons.Default.Spa
                else -> Icons.Default.MusicNote
            },
            contentDescription = null,
            tint = White.copy(alpha = 0.2f),
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.BottomEnd)
                .rotate(-20f)
                .offset(x = 10.dp, y = 10.dp)
        )
    }
}

@Composable
fun ModernMusicGridItem(music: Music, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface
        ) {
            AsyncImage(
                model = music.artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = music.title ?: "",
            style = MaterialTheme.typography.bodyMedium.copy(color = White, fontWeight = FontWeight.Bold),
            maxLines = 1
        )
        Text(
            text = music.artist ?: "",
            style = MaterialTheme.typography.labelSmall.copy(color = LightGray),
            maxLines = 1
        )
    }
}

@Composable
fun CategoryDetailView(
    category: MusicCategory,
    music: List<Music>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onMusicClick: (Music) -> Unit
) {
    val color = remember(category.color) { Color(android.graphics.Color.parseColor(category.color)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header with back button and category info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(color.copy(alpha = 0.8f), color.copy(alpha = 0.3f), DarkBackground)
                    )
                )
        ) {
            // Back button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(top = 40.dp, start = 16.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = White
                )
            }

            // Category title and icon
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 24.dp)
            ) {
                Icon(
                    imageVector = when (category.icon) {
                        "mic" -> Icons.Default.Mic
                        "album" -> Icons.Default.Album
                        "spa" -> Icons.Default.Spa
                        else -> Icons.Default.MusicNote
                    },
                    contentDescription = null,
                    tint = White.copy(alpha = 0.8f),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = White,
                        fontWeight = FontWeight.Black
                    )
                )
                Text(
                    text = "${music.size} songs",
                    style = MaterialTheme.typography.bodyMedium.copy(color = White.copy(alpha = 0.7f))
                )
            }
        }

        // Loading or content
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = color)
            }
        } else if (music.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.MusicOff,
                        contentDescription = null,
                        tint = LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No songs in this category yet",
                        style = MaterialTheme.typography.bodyLarge.copy(color = LightGray),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Music grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(music) { song ->
                    ModernMusicGridItem(song, onClick = { onMusicClick(song) })
                }
            }
        }
    }
}
