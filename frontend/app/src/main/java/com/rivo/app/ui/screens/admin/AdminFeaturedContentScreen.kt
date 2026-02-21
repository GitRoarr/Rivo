package com.rivo.app.ui.screens.admin

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
import com.rivo.app.ui.navigation.RivoScreens
import com.rivo.app.ui.theme.Primary
import com.rivo.app.ui.viewmodel.AdminViewModel
import com.rivo.app.ui.viewmodel.MusicViewModel
import com.rivo.app.utils.ImagePickerHelper
import kotlinx.coroutines.launch
import java.io.File

enum class FeaturedTab {
    BANNERS, SONGS, ARTISTS
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

    var selectedTab by remember { mutableStateOf(FeaturedTab.BANNERS) }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Featured Content", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Featured Content")
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = Color(0xFF2A2A2A),
                    contentColor = Color.White,
                    actionColor = Primary,
                    snackbarData = data
                )
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            // Tab selector
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = Color(0xFF1E1E1E),
                contentColor = Primary
            ) {
                Tab(
                    selected = selectedTab == FeaturedTab.BANNERS,
                    onClick = { selectedTab = FeaturedTab.BANNERS },
                    text = { Text("Banners", color = if (selectedTab == FeaturedTab.BANNERS) Primary else Color.White) }
                )

                Tab(
                    selected = selectedTab == FeaturedTab.SONGS,
                    onClick = { selectedTab = FeaturedTab.SONGS },
                    text = { Text("Songs", color = if (selectedTab == FeaturedTab.SONGS) Primary else Color.White) }
                )

                Tab(
                    selected = selectedTab == FeaturedTab.ARTISTS,
                    onClick = { selectedTab = FeaturedTab.ARTISTS },
                    text = { Text("Artists", color = if (selectedTab == FeaturedTab.ARTISTS) Primary else Color.White) }
                )
            }

            // Content based on selected tab
            when (selectedTab) {
                FeaturedTab.BANNERS -> {
                    val banners = featuredContent.filter { it.type == FeaturedType.BANNER }
                    FeaturedBannersTab(
                        banners = banners,
                        onDeleteClick = { showDeleteConfirmDialog = it },
                        onBannerClick = { banner ->
                            // Find the music associated with the banner if it has a contentId
                            banner.contentId?.let { musicId ->
                                val music = allMusic.find { it.id == musicId }
                                music?.let {
                                    playMusicAndNavigate(music, musicViewModel, navController)
                                }
                            }
                        }
                    )
                }
                FeaturedTab.SONGS -> {
                    val featuredSongs = featuredContent.filter { it.type == FeaturedType.SONG }
                    FeaturedSongsTab(
                        featuredSongs = featuredSongs,
                        allMusic = allMusic,
                        onDeleteClick = { showDeleteConfirmDialog = it },
                        onPlayClick = { musicId ->
                            val music = allMusic.find { it.id == musicId }
                            music?.let {
                                playMusicAndNavigate(it, musicViewModel, navController)
                            }
                        }
                    )
                }
                FeaturedTab.ARTISTS -> {
                    val featuredArtists = featuredContent.filter { it.type == FeaturedType.ARTIST }
                    FeaturedArtistsTab(
                        featuredArtists = featuredArtists,
                        allUsers = allUsers,
                        onDeleteClick = { showDeleteConfirmDialog = it }
                    )
                }
            }
        }

        // Add dialog
        if (showAddDialog) {
            when (selectedTab) {
                FeaturedTab.BANNERS -> {
                    AddBannerDialog(
                        onDismiss = { showAddDialog = false },
                        onAddBanner = { title, description, imageUri ->
                            currentAdmin?.id?.let { adminId ->
                                // Use ImagePickerHelper to save the image
                                if (imageUri != null) {
                                    // Save image to internal storage asynchronously
                                    val fileName = "banner_${System.currentTimeMillis()}.jpg"
                                    ImagePickerHelper.saveImageToInternalStorageAsync(
                                        context,
                                        imageUri,
                                        fileName
                                    ) { localPath ->
                                        if (localPath != null) {
                                            // Create featured banner with local path
                                            scope.launch {
                                                adminViewModel.createFeaturedBanner(
                                                    title = title,
                                                    description = description,
                                                    imageUrl = localPath,
                                                    adminId = adminId
                                                )
                                            }
                                        } else {
                                            // Show error if image saving failed
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Failed to save banner image")
                                            }
                                        }
                                    }
                                } else {
                                    // Create banner without image
                                    scope.launch {
                                        adminViewModel.createFeaturedBanner(
                                            title = title,
                                            description = description,
                                            imageUrl = "",
                                            adminId = adminId
                                        )
                                    }
                                }
                            }
                            showAddDialog = false
                        }
                    )
                }
                FeaturedTab.SONGS -> {
                    AddFeaturedSongDialog(
                        songs = allMusic,
                        onDismiss = { showAddDialog = false },
                        onAddFeaturedSong = { music, description ->
                            currentAdmin?.id?.let { adminId ->
                                adminViewModel.featureMusic(music)
                            }
                            showAddDialog = false
                        },
                        onPlayFeaturedSong = { music ->
                            playMusicAndNavigate(music, musicViewModel, navController)
                        }
                    )
                }
                FeaturedTab.ARTISTS -> {
                    AddFeaturedArtistDialog(
                        artists = allUsers.filter { it.userType == UserType.ARTIST },
                        onDismiss = { showAddDialog = false },
                        onAddFeaturedArtist = { artist, description ->
                            currentAdmin?.id?.let { adminId ->
                                adminViewModel.featureArtist(artist)
                            }
                            showAddDialog = false
                        }
                    )
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
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No featured banners yet.\nAdd one using the + button.",
                color = Color.White,
                textAlign = TextAlign.Center
            )
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
                    text = banner.title,
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
    onDeleteClick: (FeaturedContent) -> Unit,
    onPlayClick: (String) -> Unit
) {
    if (featuredSongs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No featured songs yet.\nAdd one using the + button.",
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(featuredSongs) { featuredSong ->
                val music = allMusic.find { it.id == featuredSong.contentId }
                if (music != null) {
                    FeaturedSongItem(
                        featuredSong = featuredSong,
                        music = music,
                        onDeleteClick = { onDeleteClick(featuredSong) },
                        onPlayClick = { onPlayClick(music.id) }
                    )
                }
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
                    text = music.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = music.artist,
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
    onDeleteClick: (FeaturedContent) -> Unit
) {
    if (featuredArtists.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No featured artists yet.\nAdd one using the + button.",
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(featuredArtists) { featuredArtist ->
                val artist = allUsers.find { it.id == featuredArtist.contentId }
                if (artist != null) {
                    FeaturedArtistItem(
                        featuredArtist = featuredArtist,
                        artist = artist,
                        onDeleteClick = { onDeleteClick(featuredArtist) }
                    )
                }
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
                            text = artist.name.take(1).uppercase(),
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
                    text = artist.name,
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
                        text = featuredArtist.description,
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
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true)
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
                                    text = song.title,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )

                                Text(
                                    text = song.artist,
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
        artists.filter { it.name.contains(searchQuery, ignoreCase = true) }
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
                                            text = artist.name.take(1).uppercase(),
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
                                    text = artist.name,
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
