package com.rivo.app.ui.screens.search

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import kotlinx.coroutines.launch

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.User
import com.rivo.app.ui.theme.*
import com.rivo.app.ui.viewmodel.MusicViewModel
import com.rivo.app.ui.viewmodel.SearchViewModel
import kotlinx.coroutines.flow.collectLatest

enum class SearchTab { ALL, SONGS, ARTISTS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onMusicClick: (Music) -> Unit,
    onArtistClick: (User) -> Unit,
    onBackClick: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
    musicViewModel: MusicViewModel,
    searchViewModel: SearchViewModel
) {
    val query by viewModel.searchQuery.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val musicResults by viewModel.musicResults.collectAsState()
    val artistResults by viewModel.artistResults.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()

    var selectedTab by remember { mutableStateOf(SearchTab.ALL) }
    var searchActive by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Subtle Background Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(RivoPurple.copy(alpha = 0.05f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(0f, 0f),
                        radius = 1000f
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Premium Search Bar
            PremiumSearchBar(
                query = query,
                onQueryChange = { viewModel.search(it) },
                onBackClick = onBackClick,
                onClearClick = { viewModel.clearSearch() }
            )

            // Tab Switcher (Horizontal Pill Style)
            SearchTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            // Results Area
            Box(modifier = Modifier.fillMaxSize()) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isSearching,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Box(modifier = Modifier.padding(top = 20.dp)) {
                        CircularProgressIndicator(color = RivoPink, modifier = Modifier.size(32.dp))
                    }
                }

                if (query.isEmpty() && !isSearching) {
                    RecentSearchesView(
                        recentSearches = recentSearches,
                        onSearch = { viewModel.search(it) },
                        onClearAll = { viewModel.clearRecentSearches() }
                    )
                } else if (!isSearching) {
                    SearchResultsList(
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

@Composable
fun PremiumSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
            }
            
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("What do you want to listen to?", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    cursorColor = RivoPink,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
            
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearClick) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                }
            } else {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
            }
        }
    }
}

@Composable
fun SearchTabs(
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
            Surface(
                onClick = { onTabSelected(tab) },
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) RivoPink else Color.White.copy(alpha = 0.05f),
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                        color = if (isSelected) Color.White else Color.Gray,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun RecentSearchesView(
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
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            if (recentSearches.isNotEmpty()) {
                Text(
                    text = "Clear All",
                    modifier = Modifier.clickable { onClearAll() },
                    color = RivoPink,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        if (recentSearches.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.DarkGray)
                    Spacer(Modifier.height(12.dp))
                    Text("No recent searches", color = Color.Gray)
                }
            }
        } else {
            LazyColumn {
                items(recentSearches) { search ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSearch(search) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                        Spacer(Modifier.width(16.dp))
                        Text(text = search, color = Color.LightGray, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.NorthWest, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.DarkGray)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultsList(
    selectedTab: SearchTab,
    musicResults: List<Music>,
    artistResults: List<User>,
    onMusicClick: (Music) -> Unit,
    onArtistClick: (User) -> Unit
) {
    if (musicResults.isEmpty() && artistResults.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No results found", color = Color.Gray)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (selectedTab == SearchTab.ALL || selectedTab == SearchTab.SONGS) {
            if (musicResults.isNotEmpty()) {
                item {
                    Text("Songs", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                itemsIndexed(musicResults) { index, music ->
                    SearchMusicItem(music = music, onClick = { onMusicClick(music) })
                }
            }
        }

        if (selectedTab == SearchTab.ALL || selectedTab == SearchTab.ARTISTS) {
            if (artistResults.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Artists", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                itemsIndexed(artistResults) { index, artist ->
                    SearchArtistItem(artist = artist, onClick = { onArtistClick(artist) })
                }
            }
        }
    }
}

@Composable
fun SearchMusicItem(music: Music, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = music.artworkUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = music.title, color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = music.artist, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
fun SearchArtistItem(artist: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            AsyncImage(
                model = artist.profilePictureUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = artist.name, color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = "Artist • ${artist.followerCount} fans", color = RivoPink, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}

// private fun String.capitalize() was here, removing if it exists
