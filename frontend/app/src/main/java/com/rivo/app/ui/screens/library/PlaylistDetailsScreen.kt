package com.rivo.app.ui.screens.library

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.shadow

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rivo.app.data.model.PlaylistWithMusic
import com.rivo.app.data.model.Music
import com.rivo.app.ui.theme.*
import com.rivo.app.ui.viewmodel.LibraryViewModel
import com.rivo.app.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailsScreen(
    playlistId: Long,
    libraryViewModel: LibraryViewModel,
    musicViewModel: MusicViewModel,
    onBackClick: () -> Unit,
    onMusicClick: (Music) -> Unit
) {
    // Collect playlist data from the shared StateFlow in LibraryViewModel
    val playlistWithMusic by libraryViewModel.currentPlaylistWithMusic.collectAsState()

    // Load specifically for this ID when screen opens or ID changes
    LaunchedEffect(playlistId) {
        libraryViewModel.loadPlaylistWithMusic(playlistId)
    }

    // Collect all available music for "Add Song" picker
    val allMusic by musicViewModel.allMusic.collectAsState()

    // UI State
    var showAddSongSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var songToRemove by remember { mutableStateOf<Music?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Take a local snapshot for null-safety
    val currentData = playlistWithMusic

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        // Ambient gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            RivoPurple.copy(alpha = 0.35f),
                            RivoPink.copy(alpha = 0.15f),
                            DarkBackground
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = DarkSurface,
                        contentColor = White,
                        actionColor = RivoPink,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Top Bar
                PlaylistTopBar(
                    onBackClick = onBackClick,
                    onOptionsClick = { showOptionsMenu = true }
                )

                if (currentData == null) {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = RivoPink,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Loading playlist…",
                                color = LightGray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    val playlist = currentData.playlist
                    val songs = currentData.musicList

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        // Header
                        item {
                            PlaylistHeroHeader(
                                playlistName = playlist.name ?: "Untitled",
                                description = playlist.description ?: "",
                                coverArtUrl = playlist.coverArtUrl,
                                songCount = songs.size,
                                totalDuration = formatTotalDuration(songs)
                            )
                        }

                        // Action buttons
                        item {
                            PlaylistActionRow(
                                onPlayAll = {
                                    if (songs.isNotEmpty()) {
                                        onMusicClick(songs.first())
                                    }
                                },
                                onShuffle = {
                                    if (songs.isNotEmpty()) {
                                        onMusicClick(songs.random())
                                    }
                                },
                                onAddSong = { showAddSongSheet = true }
                            )
                        }

                        // Songs section header
                        if (songs.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Songs",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = White
                                        )
                                    )
                                    Text(
                                        "${songs.size} track${if (songs.size != 1) "s" else ""}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = LightGray
                                        )
                                    )
                                }
                            }
                        }

                        // Songs list
                        if (songs.isEmpty()) {
                            item {
                                EmptyPlaylistContent(onAddSong = { showAddSongSheet = true })
                            }
                        } else {
                            itemsIndexed(songs) { index, music ->
                                PlaylistSongItem(
                                    music = music,
                                    index = index + 1,
                                    onClick = { onMusicClick(music) },
                                    onRemove = { songToRemove = music }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Options dropdown
        if (showOptionsMenu) {
            PlaylistOptionsMenu(
                onDismiss = { showOptionsMenu = false },
                onDelete = {
                    showOptionsMenu = false
                    showDeleteDialog = true
                },
                onAddSongs = {
                    showOptionsMenu = false
                    showAddSongSheet = true
                }
            )
        }

        // Delete confirmation dialog
        if (showDeleteDialog && currentData != null) {
            DeletePlaylistDialog(
                playlistName = currentData.playlist.name ?: "this playlist",
                onConfirm = {
                    libraryViewModel.deleteLibraryItem(currentData.playlist, true)
                    showDeleteDialog = false
                    onBackClick()
                },
                onDismiss = { showDeleteDialog = false }
            )
        }

        // Remove song confirmation
        songToRemove?.let { music ->
            RemoveSongDialog(
                songTitle = music.title ?: "this song",
                onConfirm = {
                    libraryViewModel.removeMusicFromLibraryItem(playlistId, music.id, true)
                    songToRemove = null
                    scope.launch {
                        snackbarHostState.showSnackbar("Removed from playlist")
                    }
                },
                onDismiss = { songToRemove = null }
            )
        }

        // Add song bottom sheet
        if (showAddSongSheet && currentData != null) {
            AddSongBottomSheet(
                allMusic = allMusic,
                currentSongIds = currentData.musicList.map { it.id }.toSet(),
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onAddSong = { music ->
                    scope.launch {
                        libraryViewModel.addMusicToLibraryItem(playlistId, music, true)
                        snackbarHostState.showSnackbar("Added \"${music.title}\" to playlist")
                    }
                },
                onDismiss = {
                    showAddSongSheet = false
                    searchQuery = ""
                }
            )
        }
    }
}

// ─── Top Bar ────────────────────────────────────────────────────────────────────

@Composable
private fun PlaylistTopBar(
    onBackClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(44.dp)
                .background(White.copy(alpha = 0.08f), CircleShape)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
        }

        Text(
            "Playlist",
            style = MaterialTheme.typography.titleMedium.copy(
                color = White,
                fontWeight = FontWeight.Bold
            )
        )

        IconButton(
            onClick = onOptionsClick,
            modifier = Modifier
                .size(44.dp)
                .background(White.copy(alpha = 0.08f), CircleShape)
        ) {
            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = White)
        }
    }
}

// ─── Hero Header ────────────────────────────────────────────────────────────────

@Composable
private fun PlaylistHeroHeader(
    playlistName: String,
    description: String,
    coverArtUrl: String?,
    songCount: Int,
    totalDuration: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cover art with shadow
        Surface(
            modifier = Modifier
                .size(220.dp)
                .shadow(40.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = DarkSurface
        ) {
            Box {
                if (!coverArtUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = coverArtUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Beautiful gradient placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        RivoPurple.copy(alpha = 0.6f),
                                        RivoPink.copy(alpha = 0.4f),
                                        RivoBlue.copy(alpha = 0.3f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.QueueMusic,
                            contentDescription = null,
                            tint = White.copy(alpha = 0.7f),
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }

                // Subtle overlay shine
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(White.copy(alpha = 0.08f), Color.Transparent)
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Playlist name
        Text(
            text = playlistName,
            style = MaterialTheme.typography.headlineMedium.copy(
                color = White,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            ),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Description
        if (description.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = LightGray,
                    lineHeight = 20.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Stats pill
        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = White.copy(alpha = 0.06f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = RivoPink,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "$songCount track${if (songCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = RivoPink,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text("•", color = LightGray.copy(alpha = 0.5f))
                Text(
                    totalDuration,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = LightGray
                    )
                )
            }
        }
    }
}

// ─── Action Row ─────────────────────────────────────────────────────────────────

@Composable
private fun PlaylistActionRow(
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onAddSong: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play All button — gradient
        Button(
            onClick = onPlayAll,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(listOf(RivoPurple, RivoPink)),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play All", color = White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Shuffle button
        IconButton(
            onClick = onShuffle,
            modifier = Modifier
                .size(52.dp)
                .background(White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
        ) {
            Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = White)
        }

        // Add song button
        IconButton(
            onClick = onAddSong,
            modifier = Modifier
                .size(52.dp)
                .background(
                    Brush.linearGradient(listOf(RivoPink.copy(alpha = 0.3f), RivoPurple.copy(alpha = 0.3f))),
                    RoundedCornerShape(16.dp)
                )
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Song", tint = RivoPink)
        }
    }
}

// ─── Song Item ──────────────────────────────────────────────────────────────────

@Composable
private fun PlaylistSongItem(
    music: Music,
    index: Int,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track number
        Text(
            text = "$index",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = LightGray.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Cover art
        Surface(
            modifier = Modifier.size(52.dp),
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

        Spacer(modifier = Modifier.width(14.dp))

        // Song info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = music.title ?: "Unknown",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = White,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = music.artist ?: "Unknown Artist",
                style = MaterialTheme.typography.bodySmall.copy(color = LightGray),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Duration label
        Text(
            text = formatDuration(music.duration),
            style = MaterialTheme.typography.labelSmall.copy(color = LightGray),
            modifier = Modifier.padding(end = 4.dp)
        )

        // More options
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = LightGray)
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                containerColor = DarkSurface
            ) {
                DropdownMenuItem(
                    text = { Text("Remove from playlist", color = White) },
                    onClick = {
                        showMenu = false
                        onRemove()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = ErrorRed)
                    }
                )
            }
        }
    }
}

// ─── Empty State ────────────────────────────────────────────────────────────────

@Composable
private fun EmptyPlaylistContent(onAddSong: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated empty icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    Brush.radialGradient(
                        listOf(RivoPurple.copy(alpha = 0.15f), Color.Transparent)
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.QueueMusic,
                contentDescription = null,
                tint = RivoPurple.copy(alpha = 0.7f),
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "No songs yet",
            style = MaterialTheme.typography.titleLarge.copy(
                color = White,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Add your favorite tracks to\nstart building this playlist",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = LightGray,
                lineHeight = 22.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onAddSong,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.height(52.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(listOf(RivoPurple, RivoPink)),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Songs", color = White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── Add Song Bottom Sheet ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSongBottomSheet(
    allMusic: List<Music>,
    currentSongIds: Set<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAddSong: (Music) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val (notAdded, alreadyAdded) = remember(allMusic, searchQuery, currentSongIds) {
        val filtered = allMusic.filter { music ->
            searchQuery.isBlank() ||
                    (music.title?.contains(searchQuery, ignoreCase = true) == true) ||
                    (music.artist?.contains(searchQuery, ignoreCase = true) == true)
        }
        filtered.partition { !currentSongIds.contains(it.id) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        dragHandle = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(White.copy(alpha = 0.2f))
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Add Songs",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = White,
                        letterSpacing = (-0.5).sp
                    )
                )
                
                if (allMusic.isNotEmpty()) {
                    Text(
                        "${allMusic.size} available",
                        style = MaterialTheme.typography.labelMedium.copy(color = LightGray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Premium Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp)
                    .background(White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .border(1.dp, White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = RivoPink, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = White),
                        cursorBrush = SolidColor(RivoPink),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text("Search songs or artists…", color = LightGray.copy(alpha = 0.5f))
                            }
                            innerTextField()
                        },
                        singleLine = true
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = LightGray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content
            if (allMusic.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = RivoPink, modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Looking for music...", color = LightGray)
                    }
                }
            } else if (notAdded.isEmpty() && alreadyAdded.isEmpty()) {
                // No search results
                Box(
                    modifier = Modifier.fillMaxSize().padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = LightGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No songs match \"$searchQuery\"", color = LightGray, textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 40.dp)
                ) {
                    // Not added section
                    if (notAdded.isNotEmpty()) {
                        item {
                            Text(
                                if (searchQuery.isBlank()) "Suggestions" else "Results",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = RivoPink,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }
                        items(notAdded) { music ->
                            AddSongRow(
                                music = music,
                                isAlreadyAdded = false,
                                onAdd = { onAddSong(music) }
                            )
                        }
                    }

                    // Separation or Already Added header
                    if (alreadyAdded.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Already in Playlist",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = LightGray.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }
                        items(alreadyAdded) { music ->
                            AddSongRow(
                                music = music,
                                isAlreadyAdded = true,
                                onAdd = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddSongRow(
    music: Music,
    isAlreadyAdded: Boolean,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isAlreadyAdded, onClick = onAdd)
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .then(
                if (isAlreadyAdded) Modifier.background(Color.Transparent)
                else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cover art
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(10.dp),
            color = DarkSurfaceVariant
        ) {
            AsyncImage(
                model = music.artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = music.title ?: "Unknown",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = if (isAlreadyAdded) LightGray.copy(alpha = 0.5f) else White,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = music.artist ?: "Unknown Artist",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (isAlreadyAdded) LightGray.copy(alpha = 0.3f) else LightGray
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isAlreadyAdded) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SuccessGreen.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Added",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        } else {
            IconButton(onClick = onAdd) {
                Icon(
                    Icons.Default.AddCircleOutline,
                    contentDescription = "Add",
                    tint = RivoPink
                )
            }
        }
    }
}

// ─── Dialogs ────────────────────────────────────────────────────────────────────

@Composable
private fun DeletePlaylistDialog(
    playlistName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(28.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(ErrorRed.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(28.dp))
            }
        },
        title = {
            Text(
                "Delete Playlist?",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = White,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Text(
                "\"$playlistName\" will be permanently deleted. This cannot be undone.",
                style = MaterialTheme.typography.bodyMedium.copy(color = LightGray),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Delete", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = LightGray)
            }
        }
    )
}

@Composable
private fun RemoveSongDialog(
    songTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(28.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(WarningYellow.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = WarningYellow, modifier = Modifier.size(28.dp))
            }
        },
        title = {
            Text(
                "Remove Song?",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = White,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Text(
                "Remove \"$songTitle\" from this playlist?",
                style = MaterialTheme.typography.bodyMedium.copy(color = LightGray),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Remove", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = LightGray)
            }
        }
    )
}

// ─── Options Menu ───────────────────────────────────────────────────────────────

@Composable
private fun PlaylistOptionsMenu(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onAddSongs: () -> Unit
) {
    // Full-screen clickable overlay to dismiss
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp)
                .width(220.dp),
            shape = RoundedCornerShape(20.dp),
            color = DarkSurfaceVariant,
            shadowElevation = 16.dp
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                DropdownMenuItem(
                    text = { Text("Add Songs", color = White) },
                    onClick = onAddSongs,
                    leadingIcon = {
                        Icon(Icons.Default.Add, contentDescription = null, tint = RivoPink)
                    }
                )
                HorizontalDivider(color = White.copy(alpha = 0.06f))
                DropdownMenuItem(
                    text = { Text("Delete Playlist", color = ErrorRed) },
                    onClick = onDelete,
                    leadingIcon = {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed)
                    }
                )
            }
        }
    }
}

// ─── Helpers ────────────────────────────────────────────────────────────────────

private fun formatTotalDuration(musicList: List<Music>): String {
    val totalMs = musicList.sumOf { it.duration }
    val totalSeconds = totalMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60

    return when {
        hours > 0 -> "$hours hr $minutes min"
        minutes > 0 -> "$minutes min"
        else -> "0 min"
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
