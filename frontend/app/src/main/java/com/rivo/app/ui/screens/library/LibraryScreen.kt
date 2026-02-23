package com.rivo.app.ui.screens.library

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.Playlist
import com.rivo.app.ui.theme.*
import com.rivo.app.ui.viewmodel.LibraryViewModel
import com.rivo.app.ui.viewmodel.MusicViewModel

enum class LibraryTab { PLAYLISTS, LIKED_SONGS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel,
    musicViewModel: MusicViewModel,
    userId: String,
    onPlaylistClick: (Long) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onMusicClick: (Music) -> Unit
) {
    val userPlaylists by libraryViewModel.userPlaylists.collectAsState()
    val favoriteMusic by musicViewModel.favoriteMusic.collectAsState()
    val isLoading by libraryViewModel.isLoading.collectAsState()
    
    var selectedTab by remember { mutableStateOf(LibraryTab.PLAYLISTS) }
    var selectedPlaylistId by remember { mutableStateOf<Long?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()

    LaunchedEffect(userId) {
        libraryViewModel.loadUserPlaylists(userId)
        musicViewModel.loadFavorites()
    }

    if (selectedPlaylistId != null) {
        PlaylistDetailsScreen(
            playlistId = selectedPlaylistId!!,
            libraryViewModel = libraryViewModel,
            musicViewModel = musicViewModel,
            onBackClick = { selectedPlaylistId = null },
            onMusicClick = onMusicClick
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            // Ambient Background Magic
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            RivoPurple.copy(alpha = 0.15f),
                            DarkBackground,
                            DarkBackground
                        )
                    )
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Premium Header
                LibraryHeader(onCreateClick = { showCreatePlaylistDialog = true })

                // Sub-header with Stats
                LibraryStatsRow(
                    playlistCount = userPlaylists.size,
                    likedCount = favoriteMusic.size
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Modern Tab Switcher
                LibraryTabSwitcher(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                // Dynamic Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp)
                ) {
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = RivoPink)
                        }
                    } else {
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
                                slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                            }
                        ) { tab ->
                            when (tab) {
                                LibraryTab.PLAYLISTS -> PlaylistsContent(
                                    playlists = userPlaylists,
                                    onPlaylistClick = { selectedPlaylistId = it }
                                )
                                LibraryTab.LIKED_SONGS -> LikedSongsContent(
                                    songs = favoriteMusic,
                                    onMusicClick = onMusicClick
                                )
                            }
                        }
                    }
                }
            }
            
            // Create Playlist Dialog
            if (showCreatePlaylistDialog) {
                CreatePlaylistDialog(
                    onDismiss = { showCreatePlaylistDialog = false },
                    onCreate = { name, desc ->
                        libraryViewModel.createLibraryItem(name, desc, userId, true)
                        showCreatePlaylistDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun LibraryHeader(onCreateClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, start = 24.dp, end = 24.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Library",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                color = White,
                letterSpacing = (-1).sp
            )
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassIconButton(icon = Icons.Default.Search, onClick = {})
            GlassIconButton(icon = Icons.Default.Add, onClick = onCreateClick, containerColor = RivoPink)
        }
    }
}

@Composable
fun LibraryStatsRow(playlistCount: Int, likedCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        StatItem(count = playlistCount.toString(), label = "Playlists")
        StatItem(count = likedCount.toString(), label = "Liked Songs")
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleMedium.copy(color = White, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = LightGray)
        )
    }
}

@Composable
fun LibraryTabSwitcher(selectedTab: LibraryTab, onTabSelected: (LibraryTab) -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .padding(4.dp)
    ) {
        LibraryTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            val background by animateColorAsState(if (isSelected) White.copy(alpha = 0.1f) else Color.Transparent)
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(background)
                    .clickable { onTabSelected(tab) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (tab == LibraryTab.PLAYLISTS) "Playlists" else "Liked Songs",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) White else LightGray
                    )
                )
            }
        }
    }
}

@Composable
fun PlaylistsContent(playlists: List<Playlist>, onPlaylistClick: (Long) -> Unit) {
    if (playlists.isEmpty()) {
        EmptyLibraryMessage(
            icon = Icons.Outlined.QueueMusic,
            title = "Cloud Playlists",
            message = "Your custom playlists will appear here.\nStart curating your unique vibe!"
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(playlists) { playlist ->
                AmazingPlaylistCard(playlist, onClick = { onPlaylistClick(playlist.id) })
            }
        }
    }
}

@Composable
fun AmazingPlaylistCard(playlist: Playlist, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(28.dp))
        ) {
            if (playlist.coverArtUrl != null) {
                AsyncImage(
                    model = playlist.coverArtUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(DarkSurface, DarkSurfaceVariant))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = LightGray, modifier = Modifier.size(40.dp))
                }
            }
            
            // Play Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = playlist.name ?: "",
            style = MaterialTheme.typography.titleMedium.copy(color = White, fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "Collection",
            style = MaterialTheme.typography.bodySmall.copy(color = LightGray)
        )
    }
}

@Composable
fun LikedSongsContent(songs: List<Music>, onMusicClick: (Music) -> Unit) {
    if (songs.isEmpty()) {
        EmptyLibraryMessage(
            icon = Icons.Outlined.FavoriteBorder,
            title = "No Liked Songs",
            message = "Tap the heart on any song to save it here for quick access."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(songs) { music ->
                AmazingMusicRow(music, onClick = { onMusicClick(music) })
            }
        }
    }
}

@Composable
fun AmazingMusicRow(music: Music, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(12.dp),
            color = DarkSurface
        ) {
            AsyncImage(
                model = music.artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = music.title ?: "",
                style = MaterialTheme.typography.bodyLarge.copy(color = White, fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = music.artist ?: "",
                style = MaterialTheme.typography.bodySmall.copy(color = LightGray),
                maxLines = 1
            )
        }
        
        Icon(
            Icons.Default.Favorite,
            contentDescription = null,
            tint = RivoPink,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        IconButton(onClick = {}) {
            Icon(Icons.Default.MoreVert, contentDescription = null, tint = LightGray)
        }
    }
}

@Composable
fun EmptyLibraryMessage(icon: ImageVector, title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(White.copy(alpha = 0.05f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = RivoPurple, modifier = Modifier.size(48.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(color = White, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(color = LightGray),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // Amazing Logic: Auto-suggest description and name based on context
    LaunchedEffect(Unit) {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        if (name.isBlank()) {
            name = when {
                hour in 5..11 -> "Morning Vibes ☕"
                hour in 12..16 -> "Afternoon Chill ☀️"
                hour in 17..20 -> "Golden Hour Mix 🌅"
                else -> "Late Night Drifting 🌙"
            }
        }
    }

    LaunchedEffect(name) {
        val lower = name.lowercase()
        if (description.isBlank()) {
            when {
                lower.contains("party") || lower.contains("dance") -> description = "Time to dance! 💃"
                lower.contains("chill") || lower.contains("relax") -> description = "Pure relaxation... 🌊"
                lower.contains("gym") || lower.contains("workout") -> description = "Push your limits! 💪"
                lower.contains("sad") || lower.contains("lofi") -> description = "Vibin' in the rain 🌧️"
                lower.contains("study") -> description = "Deep focus mode 📚"
                lower.contains("love") || lower.contains("sweet") -> description = "Love is in the air ❤️"
                lower.contains("morning") -> description = "Start your day right!"
                lower.contains("night") -> description = "Under the stars..."
            }
        }
    }

    val isReady = name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.clip(RoundedCornerShape(32.dp)),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = DarkSurface,
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(RivoPurple, RivoPink))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = White, modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "New Playlist",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, color = White)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Playlist Name", color = LightGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RivoPink,
                    unfocusedBorderColor = White.copy(alpha = 0.1f),
                    focusedTextColor = White,
                    unfocusedTextColor = White
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)", color = LightGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RivoPink,
                    unfocusedBorderColor = White.copy(alpha = 0.1f),
                    focusedTextColor = White,
                    unfocusedTextColor = White
                ),
                shape = RoundedCornerShape(16.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Text("Cancel", color = LightGray)
                }

                Button(
                    onClick = { onCreate(name, description) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    enabled = isReady,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RivoPink,
                        disabledContainerColor = RivoPink.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Create", color = White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
}

@Composable
fun GlassIconButton(icon: ImageVector, onClick: () -> Unit, containerColor: Color = White.copy(alpha = 0.05f)) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = White, modifier = Modifier.size(20.dp))
    }
}

// End of file
