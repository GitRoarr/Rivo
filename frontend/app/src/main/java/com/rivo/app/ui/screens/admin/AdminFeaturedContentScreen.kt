package com.rivo.app.ui.screens.admin
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.rivo.app.data.model.FeaturedContent
import com.rivo.app.data.model.FeaturedType
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.User
import com.rivo.app.data.model.UserType
import androidx.compose.material.icons.outlined.*
import com.rivo.app.ui.navigation.RivoScreens
import com.rivo.app.ui.theme.*
import com.rivo.app.ui.screens.home.formatCount
import com.rivo.app.ui.viewmodel.AdminViewModel
import com.rivo.app.ui.viewmodel.MusicViewModel
import com.rivo.app.utils.ImagePickerHelper
import kotlinx.coroutines.launch
import java.io.File

enum class FeaturedTab {
    SONGS, ARTISTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFeaturedContentScreen(
    onBackClick: () -> Unit,
    navController: NavController? = null,
    adminViewModel: AdminViewModel = hiltViewModel(),
    musicViewModel: MusicViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentAdmin by adminViewModel.currentAdmin.collectAsState()
    val allMusic by adminViewModel.allMusic.collectAsState()
    val allUsers by adminViewModel.allUsers.collectAsState()
    val featuredContent by adminViewModel.featuredContent.collectAsState()
    val operationStatus by adminViewModel.operationStatus.collectAsState()

    var selectedTab by remember { mutableStateOf(FeaturedTab.SONGS) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<FeaturedContent?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(operationStatus) {
        operationStatus?.let {
            snackbarHostState.showSnackbar(it)
            adminViewModel.clearOperationStatus()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        // Ambient glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(RivoPurple.copy(alpha = 0.12f), DarkBackground)
                    )
                )
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Feature Management",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black, color = White, letterSpacing = (-0.5).sp
                                )
                            )
                            Text(
                                "Curate the Explore screen",
                                style = MaterialTheme.typography.bodySmall.copy(color = LightGray)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(CircleShape)
                                .background(White.copy(0.06f))
                        ) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = White)
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { adminViewModel.refreshAllData() },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(CircleShape)
                                .background(White.copy(0.06f))
                        ) {
                            Icon(Icons.Default.Refresh, "Refresh", tint = White)
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Transparent)
        ) {
            // Premium Tab Row
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(White.copy(0.05f), RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {
                FeaturedTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    val bgColor by animateColorAsState(
                        if (isSelected) RivoPurple.copy(0.2f) else Color.Transparent,
                        label = "tab_bg"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .background(bgColor)
                            .clickable { selectedTab = tab },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.name.lowercase().capitalize(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) White else LightGray,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            // Animated Content based on selected tab
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(tween(300)) + slideInHorizontally { it / 4 } togetherWith
                    fadeOut(tween(250)) + slideOutHorizontally { -it / 4 }
                },
                label = "featured_tab_anim"
            ) { tab ->
                when (tab) {
                    FeaturedTab.SONGS -> {
                        val featuredSongs = featuredContent.filter { it.type == FeaturedType.SONG }
                        val isLoading by adminViewModel.isLoading.collectAsState()
                        FeaturedSongsTab(
                            featuredSongs = featuredSongs,
                            allMusic = allMusic,
                            isLoading = isLoading,
                            onPinClick = { music -> adminViewModel.featureMusic(music) },
                            onUnpinClick = { fc -> adminViewModel.removeFeaturedContent(fc.id) },
                            onPlayClick = { musicId ->
                                val music = allMusic.find { it.id == musicId }
                                music?.let { playMusicAndNavigate(it, musicViewModel, navController) }
                            }
                        )
                    }
                    FeaturedTab.ARTISTS -> {
                        val featuredArtists = featuredContent.filter { it.type == FeaturedType.ARTIST }
                        val isLoading by adminViewModel.isLoading.collectAsState()
                        FeaturedArtistsTab(
                            featuredArtists = featuredArtists,
                            allUsers = allUsers.filter { it.userType == UserType.ARTIST },
                            isLoading = isLoading,
                            onPinClick = { artist -> adminViewModel.featureArtist(artist) },
                            onUnpinClick = { fc -> adminViewModel.removeFeaturedContent(fc.id) }
                        )
                    }
                }
            }
        }
    }

        showDeleteConfirmDialog?.let { content ->
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = null },
                title = { Text("Confirm Removal", color = Color.White) },
                text = { Text("Are you sure you want to remove this featured content?", color = Color.White) },
                confirmButton = {
                    Button(
                        onClick = {
                            adminViewModel.removeFeaturedContent(content.id)
                            showDeleteConfirmDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text("Remove")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirmDialog = null }
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E1E1E),
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
    }
}

// Helper function to play music and navigate to player screen
private fun playMusicAndNavigate(music: Music, musicViewModel: MusicViewModel, navController: NavController?) {
    musicViewModel.playMusic(music)
    navController?.navigate(RivoScreens.Player.name)
}

@Composable
fun FeaturedBannersTab(
    banners: List<FeaturedContent>,
    onDeleteClick: (FeaturedContent) -> Unit,
    onBannerClick: (FeaturedContent) -> Unit
) {
    if (banners.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.PhotoLibrary,
                    null,
                    tint = White.copy(0.1f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "No featured banners yet",
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Add one using the + button to make the home screen look amazing.",
                    color = LightGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(banners) { banner ->
                BannerItem(
                    banner = banner,
                    onDeleteClick = { onDeleteClick(banner) },
                    onBannerClick = { onBannerClick(banner) }
                )
            }
        }
    }
}

@Composable
fun BannerItem(
    banner: FeaturedContent,
    onDeleteClick: () -> Unit,
    onBannerClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBannerClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            // Banner image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFF2A2A2A))
            ) {
                if (banner.imageUrl != null) {
                    // Handle both file paths and URLs
                    val imageModel = when {
                        banner.imageUrl.startsWith("/") -> {
                            // Local file path
                            File(banner.imageUrl)
                        }
                        banner.imageUrl.startsWith("file://") -> {
                            // File URI
                            File(banner.imageUrl.removePrefix("file://"))
                        }
                        else -> {
                            // Regular URL
                            banner.imageUrl
                        }
                    }

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageModel)
                            .crossfade(true)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Play button overlay if it's linked to music
                if (banner.contentId != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { onBannerClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Delete button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.8f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Banner info
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = banner.title ?: "",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = banner.description ?: "",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun FeaturedSongsTab(
    featuredSongs: List<FeaturedContent>,
    allMusic: List<Music>,
    isLoading: Boolean,
    onPinClick: (Music) -> Unit,
    onUnpinClick: (FeaturedContent) -> Unit,
    onPlayClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val pinnedIds = remember(featuredSongs) { featuredSongs.mapNotNull { it.contentId }.toSet() }
    val filtered = remember(searchQuery, allMusic) {
        if (searchQuery.isBlank()) allMusic
        else allMusic.filter {
            (it.title ?: "").contains(searchQuery, ignoreCase = true) ||
            (it.artist ?: "").contains(searchQuery, ignoreCase = true) ||
            (it.album ?: "").contains(searchQuery, ignoreCase = true)
        }
    }
    val pinnedSongs = remember(filtered, pinnedIds) { filtered.filter { it.id in pinnedIds } }
    val unpinnedSongs = remember(filtered, pinnedIds) { filtered.filter { it.id !in pinnedIds } }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search songs to pin...", color = LightGray) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = RivoPurple) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, null, tint = LightGray)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RivoPurple,
                unfocusedBorderColor = White.copy(0.1f),
                focusedTextColor = White,
                unfocusedTextColor = White,
                cursorColor = RivoPurple,
                focusedContainerColor = White.copy(0.04f),
                unfocusedContainerColor = White.copy(0.04f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        )
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No songs found.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pinnedSongs.isNotEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PushPin, null, tint = Primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("PINNED TO EXPLORE", color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    items(pinnedSongs, key = { it.id }) { song ->
                        val fc = featuredSongs.find { it.contentId == song.id }
                        SongPinCard(
                            song = song, isPinned = true,
                            onPlayClick = { onPlayClick(song.id) },
                            onPinToggle = { fc?.let { onUnpinClick(it) } }
                        )
                    }
                }
                if (unpinnedSongs.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text("ALL SONGS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    items(unpinnedSongs, key = { it.id }) { song ->
                        SongPinCard(
                            song = song, isPinned = false,
                            onPlayClick = { onPlayClick(song.id) },
                            onPinToggle = { onPinClick(song) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SongPinCard(
    song: Music,
    isPinned: Boolean,
    onPlayClick: () -> Unit,
    onPinToggle: () -> Unit
) {
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPinned) 0.15f else 0f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "pin_glow"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isPinned) RivoPurple.copy(0.08f) else White.copy(0.03f)
        ),
        border = BorderStroke(
            1.dp,
            if (isPinned) RivoPurple.copy(0.3f) else White.copy(0.06f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(White.copy(0.06f))
                    .clickable { onPlayClick() },
                contentAlignment = Alignment.Center
            ) {
                if (!song.artworkUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = song.artworkUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.MusicNote, null, tint = LightGray, modifier = Modifier.size(22.dp))
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                Text(song.title ?: "", color = White, fontSize = 14.sp,
                    fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist ?: "", color = LightGray, fontSize = 12.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (isPinned) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PushPin, null, tint = RivoPurple, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("Pinned to explore", color = RivoPurple, fontSize = 10.sp)
                    }
                } else {
                    Text("${formatPlayCount(song.playCount)} · ${song.genre ?: ""}",
                        color = LightGray.copy(0.6f), fontSize = 10.sp, maxLines = 1)
                }
            }
            IconButton(onClick = onPlayClick) {
                Icon(Icons.Default.PlayCircle, null, tint = LightGray, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onPinToggle) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = if (isPinned) "Unpin" else "Pin to Explore",
                    tint = if (isPinned) RivoPurple else LightGray,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun FeaturedSongItem(
    featuredSong: FeaturedContent,
    music: Music,
    onDeleteClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Song artwork with play overlay
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF2A2A2A))
                    .clickable { onPlayClick() },
                contentAlignment = Alignment.Center
            ) {
                if (music.artworkUri != null) {
                    // Handle different types of artwork paths
                    val imageModel = when {
                        music.artworkUri.startsWith("/") -> File(music.artworkUri)
                        music.artworkUri.startsWith("file://") -> File(music.artworkUri.removePrefix("file://"))
                        else -> music.artworkUri
                    }

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageModel)
                            .crossfade(true)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Play icon overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Song info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = music.title ?: "",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = music.artist ?: "",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )

                    Text(
                        text = formatPlayCount(music.playCount),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            // Play button
            IconButton(
                onClick = onPlayClick
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Play",
                    tint = Primary
                )
            }

            // Delete button
            IconButton(
                onClick = onDeleteClick
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
fun FeaturedArtistsTab(
    featuredArtists: List<FeaturedContent>,
    allUsers: List<User>,
    isLoading: Boolean,
    onPinClick: (User) -> Unit,
    onUnpinClick: (FeaturedContent) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val pinnedIds = remember(featuredArtists) { featuredArtists.mapNotNull { it.contentId }.toSet() }
    val filtered = remember(searchQuery, allUsers) {
        if (searchQuery.isBlank()) allUsers
        else allUsers.filter {
            (it.name ?: "").contains(searchQuery, ignoreCase = true) ||
            (it.fullName ?: "").contains(searchQuery, ignoreCase = true)
        }
    }
    val pinnedArtists = remember(filtered, pinnedIds) { filtered.filter { it.id in pinnedIds } }
    val unpinnedArtists = remember(filtered, pinnedIds) { filtered.filter { it.id !in pinnedIds } }
    val context = LocalContext.current

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search artists to pin...", color = LightGray) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = RivoPink) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, null, tint = LightGray)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RivoPink,
                unfocusedBorderColor = White.copy(0.1f),
                focusedTextColor = White,
                unfocusedTextColor = White,
                cursorColor = RivoPink,
                focusedContainerColor = White.copy(0.04f),
                unfocusedContainerColor = White.copy(0.04f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        )
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No artists found.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pinnedArtists.isNotEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PushPin, null, tint = Primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("PINNED TO EXPLORE", color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    items(pinnedArtists, key = { it.id }) { artist ->
                        val fc = featuredArtists.find { it.contentId == artist.id }
                        ArtistPinCard(
                            artist = artist, isPinned = true,
                            onPinToggle = { fc?.let { onUnpinClick(it) } }
                        )
                    }
                }
                if (unpinnedArtists.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text("ALL ARTISTS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    items(unpinnedArtists, key = { it.id }) { artist ->
                        ArtistPinCard(
                            artist = artist, isPinned = false,
                            onPinToggle = { onPinClick(artist) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistPinCard(
    artist: User,
    isPinned: Boolean,
    onPinToggle: () -> Unit
) {
    val context = LocalContext.current
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPinned) 0.15f else 0f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "pin_glow_art"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isPinned) RivoPink.copy(0.08f) else White.copy(0.03f)
        ),
        border = BorderStroke(
            1.dp,
            if (isPinned) RivoPink.copy(0.3f) else White.copy(0.06f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(White.copy(0.06f)),
                contentAlignment = Alignment.Center
            ) {
                if (!artist.profileImageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(artist.profileImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = artist.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        (artist.name ?: "?").take(1).uppercase(),
                        color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                Text(artist.name ?: "", color = White, fontSize = 14.sp,
                    fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(artist.fullName ?: "", color = LightGray, fontSize = 12.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (isPinned) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PushPin, null, tint = RivoPink, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("Pinned to explore", color = RivoPink, fontSize = 10.sp)
                    }
                } else {
                    Text("${formatCount(artist.followerCount ?: 0)} fans",
                        color = LightGray.copy(0.6f), fontSize = 10.sp)
                }
            }
            IconButton(onClick = onPinToggle) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = if (isPinned) "Unpin" else "Pin to Explore",
                    tint = if (isPinned) RivoPink else LightGray,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun FeaturedArtistItem(
    featuredArtist: FeaturedContent,
    artist: User,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artist image
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2A2A)),
                contentAlignment = Alignment.Center
            ) {
                // Handle different types of profile image paths
                when {
                    // Case 1: Check if it's a local file path
                    artist.profileImageUrl?.startsWith("/") == true -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(artist.profileImageUrl))
                                .crossfade(true)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = artist.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    // Case 2: Check if it's a URL or URI string
                    !artist.profileImageUrl.isNullOrEmpty() -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(artist.profileImageUrl)
                                .crossfade(true)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = artist.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    // Case 3: Fallback to initial letter
                    else -> {
                        Text(
                            text = (artist.name ?: "").take(1).uppercase(),
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Artist info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = artist.name ?: "",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "${artist.followerCount ?: 0} followers",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                if (featuredArtist.description != null) {
                    Text(
                        text = featuredArtist.description ?: "",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Delete button
            IconButton(
                onClick = onDeleteClick
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBannerDialog(
    onDismiss: () -> Unit,
    onAddBanner: (title: String, description: String, imageUri: Uri?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isImageLoading by remember { mutableStateOf(false) }

    // Use the modern photo picker with PickVisualMedia
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedImageUri = uri
        isImageLoading = uri != null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Featured Banner", color = Color.White) },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A2A2A),
                        unfocusedContainerColor = Color(0xFF2A2A2A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A2A2A),
                        unfocusedContainerColor = Color(0xFF2A2A2A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Image selection
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedImageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2A2A2A))
                        ) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Show loading indicator while image is being processed
                            if (isImageLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                    color = Primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedImageUri == null) Primary else Color.Gray
                        )
                    ) {
                        Text(if (selectedImageUri == null) "Select Image" else "Change Image")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAddBanner(title, description, selectedImageUri) },
                enabled = title.isNotBlank() && selectedImageUri != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = Primary.copy(alpha = 0.5f)
                )
            ) {
                Text("Add Banner")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        },
        containerColor = Color(0xFF1E1E1E),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeaturedSongDialog(
    songs: List<Music>,
    onDismiss: () -> Unit,
    onAddFeaturedSong: (music: Music, description: String) -> Unit,
    onPlayFeaturedSong: (music: Music) -> Unit
) {
    var selectedSong by remember { mutableStateOf<Music?>(null) }
    var description by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val filteredSongs = if (searchQuery.isBlank()) {
        songs
    } else {
        songs.filter {
            (it.title ?: "").contains(searchQuery, ignoreCase = true) ||
                    (it.artist ?: "").contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Featured Song", color = Color.White) },
        text = {
            Column {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Songs") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A2A2A),
                        unfocusedContainerColor = Color(0xFF2A2A2A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Select a song:",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    items(filteredSongs) { song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSong = song
                                }
                                .padding(8.dp)
                                .background(
                                    if (selectedSong?.id == song.id) Color(0xFF2A2A2A)
                                    else Color.Transparent
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Song artwork
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF2A2A2A)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (song.artworkUri != null) {
                                    // Handle different types of artwork paths
                                    val imageModel = when {
                                        song.artworkUri.startsWith("/") -> File(song.artworkUri)
                                        song.artworkUri.startsWith("file://") -> File(song.artworkUri.removePrefix("file://"))
                                        else -> song.artworkUri
                                    }

                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(imageModel)
                                            .crossfade(true)
                                            .diskCachePolicy(CachePolicy.ENABLED)
                                            .memoryCachePolicy(CachePolicy.ENABLED)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            ) {
                                Text(
                                    text = song.title ?: "",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )

                                Text(
                                    text = song.artist ?: "",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }

                            // Play button
                            IconButton(
                                onClick = { onPlayFeaturedSong(song) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = "Play",
                                    tint = Primary
                                )
                            }
                        }

                        Divider(color = Color.DarkGray.copy(alpha = 0.5f))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A2A2A),
                        unfocusedContainerColor = Color(0xFF2A2A2A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedSong?.let {
                        onAddFeaturedSong(it, description)
                    }
                },
                enabled = selectedSong != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = Primary.copy(alpha = 0.5f)
                )
            ) {
                Text("Add Featured Song")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        },
        containerColor = Color(0xFF1E1E1E),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeaturedArtistDialog(
    artists: List<User>,
    onDismiss: () -> Unit,
    onAddFeaturedArtist: (artist: User, description: String) -> Unit
) {
    var selectedArtist by remember { mutableStateOf<User?>(null) }
    var description by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val filteredArtists = if (searchQuery.isBlank()) {
        artists
    } else {
        artists.filter { (it.name ?: "").contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Featured Artist", color = Color.White) },
        text = {
            Column {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Artists") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A2A2A),
                        unfocusedContainerColor = Color(0xFF2A2A2A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Select an artist:",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    items(filteredArtists) { artist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedArtist = artist }
                                .padding(8.dp)
                                .background(
                                    if (selectedArtist?.id == artist.id) Color(0xFF2A2A2A)
                                    else Color.Transparent
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Artist image
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2A2A2A)),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    // Case 1: Check if it's a local file path
                                    artist.profileImageUrl?.startsWith("/") == true -> {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(File(artist.profileImageUrl))
                                                .crossfade(true)
                                                .diskCachePolicy(CachePolicy.ENABLED)
                                                .memoryCachePolicy(CachePolicy.ENABLED)
                                                .build(),
                                            contentDescription = artist.name ?: "",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    // Case 2: Check if it's a URL or URI string
                                    !artist.profileImageUrl.isNullOrEmpty() -> {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(artist.profileImageUrl)
                                                .crossfade(true)
                                                .diskCachePolicy(CachePolicy.ENABLED)
                                                .memoryCachePolicy(CachePolicy.ENABLED)
                                                .build(),
                                            contentDescription = artist.name ?: "",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    // Case 3: Fallback to initial letter
                                    else -> {
                                        Text(
                                            text = (artist.name ?: "").take(1).uppercase(),
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            ) {
                                Text(
                                    text = artist.name ?: "",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )

                                Text(
                                    text = "${artist.followerCount ?: 0} followers",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Divider(color = Color.DarkGray.copy(alpha = 0.5f))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A2A2A),
                        unfocusedContainerColor = Color(0xFF2A2A2A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedArtist?.let { onAddFeaturedArtist(it, description) }
                },
                enabled = selectedArtist != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = Primary.copy(alpha = 0.5f)
                )
            ) {
                Text("Add Featured Artist")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        },
        containerColor = Color(0xFF1E1E1E),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

// Format play count to display as "950K plays" or "1.2M plays"
private fun formatPlayCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "${count / 1_000_000}M plays"
        count >= 1_000 -> "${count / 1_000}K plays"
        else -> "$count plays"
    }
}
