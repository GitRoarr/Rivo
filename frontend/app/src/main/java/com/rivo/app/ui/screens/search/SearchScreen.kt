package com.rivo.app.ui.screens.search

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.User
import com.rivo.app.ui.theme.*
import com.rivo.app.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.delay
import java.util.Locale

enum class SearchTab { ALL, SONGS, ARTISTS }

@Composable
fun SearchScreen(
    onMusicClick: (Music) -> Unit,
    onArtistClick: (User) -> Unit,
    onBackClick: () -> Unit,
    searchViewModel: SearchViewModel
) {
    val query by searchViewModel.searchQuery.collectAsState()
    val isSearching by searchViewModel.isSearching.collectAsState()
    val musicResults by searchViewModel.musicResults.collectAsState()
    val artistResults by searchViewModel.artistResults.collectAsState()
    val recentSearches by searchViewModel.recentSearches.collectAsState()

    var selectedTab by remember { mutableStateOf(SearchTab.ALL) }

    // Animated background orbs (consistent with HomeScreen)
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    val orbAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f, targetValue = 0.12f,
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse),
        label = "orb1"
    )
    val orbAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.04f, targetValue = 0.10f,
        animationSpec = infiniteRepeatable(tween(5500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "orb2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(RivoPurple.copy(orbAlpha), Color.Transparent),
                        center = Offset(size.width * 0.9f, size.height * 0.1f),
                        radius = size.width * 0.7f
                    ),
                    radius = size.width * 0.7f,
                    center = Offset(size.width * 0.9f, size.height * 0.1f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(RivoPink.copy(orbAlpha2), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.8f),
                        radius = size.width * 0.6f
                    ),
                    radius = size.width * 0.6f,
                    center = Offset(size.width * 0.1f, size.height * 0.8f)
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(52.dp))

            // Premium Search Bar
            PremiumSearchBar(
                query = query,
                onQueryChange = { searchViewModel.search(it) },
                onBackClick = onBackClick,
                onClearClick = { searchViewModel.clearSearch() }
            )

            // Dynamic Tabs
            AnimatedSearchTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Box(modifier = Modifier.weight(1f)) {
                // Loading State
                Box(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isSearching,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(2.dp),
                            color = RivoPink,
                            trackColor = Color.Transparent
                        )
                    }
                }

                if (query.isEmpty() && !isSearching) {
                    RecentSearchesSection(
                        recentSearches = recentSearches,
                        onSearch = { searchViewModel.search(it) },
                        onClearAll = { searchViewModel.clearRecentSearches() }
                    )
                } else if (!isSearching) {
                    SearchResultsContent(
                        selectedTab = selectedTab,
                        musicResults = musicResults,
                        artistResults = artistResults,
                        onMusicClick = onMusicClick,
                        onArtistClick = onArtistClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onClearClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val glowAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.15f else 0.05f,
        animationSpec = tween(300),
        label = "glow"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .background(White.copy(0.05f), CircleShape)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Search Input
        Surface(
            color = White.copy(0.07f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, if (isFocused) RivoPink.copy(0.5f) else White.copy(0.1f)),
            modifier = Modifier
                .weight(1f)
                .drawBehind {
                    if (isFocused) {
                        drawCircle(
                            brush = Brush.radialGradient(listOf(RivoPink.copy(glowAlpha), Color.Transparent)),
                            radius = size.width * 1.2f
                        )
                    }
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = if (isFocused) RivoPink else LightGray,
                    modifier = Modifier.size(20.dp)
                )

                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            "Artists, songs, or podcasts",
                            color = LightGray.copy(0.6f),
                            fontSize = 15.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        cursorColor = RivoPink,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = White,
                        unfocusedTextColor = White
                    ),
                    singleLine = true
                )

                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = onClearClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = LightGray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedSearchTabs(
    selectedTab: SearchTab,
    onTabSelected: (SearchTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SearchTab.values().forEach { tab ->
            val isSelected = selectedTab == tab
            val bgAlpha by animateFloatAsState(if (isSelected) 1f else 0.05f)
            
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .background(if (isSelected) RivoPink else White.copy(0.05f))
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = if (isSelected) White else LightGray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun RecentSearchesSection(
    recentSearches: List<String>,
    onSearch: (String) -> Unit,
    onClearAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Searches",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = White,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
            )
            if (recentSearches.isNotEmpty()) {
                Text(
                    text = "Clear All",
                    modifier = Modifier.clickable { onClearAll() },
                    color = RivoPink,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (recentSearches.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(White.copy(0.03f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(32.dp), tint = LightGray.copy(0.3f))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("No recent searches", color = LightGray.copy(0.5f), fontWeight = FontWeight.Medium)
                }
            }
        } else {
            LazyColumn {
                itemsIndexed(recentSearches) { index, search ->
                    StaggeredFadeIn(index) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSearch(search) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(20.dp), tint = LightGray.copy(0.6f))
                            Spacer(Modifier.width(16.dp))
                            Text(text = search, color = LightGray, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowOutward, contentDescription = null, modifier = Modifier.size(16.dp), tint = LightGray.copy(0.3f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultsContent(
    selectedTab: SearchTab,
    musicResults: List<Music>,
    artistResults: List<User>,
    onMusicClick: (Music) -> Unit,
    onArtistClick: (User) -> Unit
) {
    if (musicResults.isEmpty() && artistResults.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔎", fontSize = 48.sp)
                Spacer(Modifier.height(16.dp))
                Text("No results found", color = LightGray, fontWeight = FontWeight.Medium)
                Text("Try searching for something else", color = LightGray.copy(0.5f), fontSize = 14.sp)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        if (selectedTab == SearchTab.ALL || selectedTab == SearchTab.SONGS) {
            if (musicResults.isNotEmpty()) {
                item {
                    Text(
                        "Songs",
                        color = White,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }
                itemsIndexed(musicResults) { index, music ->
                    StaggeredFadeIn(index) {
                        SearchMusicListItem(music = music, onClick = { onMusicClick(music) })
                    }
                }
            }
        }

        if (selectedTab == SearchTab.ALL || selectedTab == SearchTab.ARTISTS) {
            if (artistResults.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Artists",
                        color = White,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }
                itemsIndexed(artistResults) { index, artist ->
                    StaggeredFadeIn(index) {
                        SearchArtistListItem(artist = artist, onClick = { onArtistClick(artist) })
                    }
                }
            }
        }
    }
}

@Composable
fun SearchMusicListItem(music: Music, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = music.artworkUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = music.title,
                color = White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = music.artist,
                color = LightGray,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
        Icon(Icons.Default.MoreVert, contentDescription = null, tint = LightGray.copy(0.6f))
    }
}

@Composable
fun SearchArtistListItem(artist: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(White.copy(0.05f))
        ) {
            AsyncImage(
                model = artist.profileImageUrl ?: artist.profilePictureUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                color = White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Artist • ${formatFollowers(artist.followerCount)} fans",
                color = RivoPink,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = LightGray.copy(0.6f))
    }
}

@Composable
fun StaggeredFadeIn(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 40L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 5 }
    ) {
        content()
    }
}

private fun formatFollowers(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000f)
        count >= 1_000 -> String.format(Locale.US, "%.1fK", count / 1_000f)
        else -> count.toString()
    }
}
