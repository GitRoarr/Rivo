package com.rivo.app.ui.screens.artist

import android.net.Uri
import android.util.Log
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.fragment.app.FragmentActivity
import coil.compose.AsyncImage
import com.rivo.app.data.model.ArtistAnalytics
import com.rivo.app.data.model.Music
import com.rivo.app.data.remote.MusicCategory
import com.rivo.app.ui.theme.*
import com.rivo.app.ui.viewmodel.ArtistDashboardTab
import com.rivo.app.ui.viewmodel.ArtistViewModel
import com.rivo.app.ui.viewmodel.FollowViewModel
import com.rivo.app.utils.ImagePickerHelper
import com.rivo.app.utils.MediaAccessHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDashboardScreen(
    onBackClick: () -> Unit,
    onEditTrackClick: (Music) -> Unit,
    onDeleteTrackClick: (Music) -> Unit,
    onNotificationClick: () -> Unit,
    artistViewModel: ArtistViewModel,
    followViewModel: FollowViewModel
) {
    val artistMusic by artistViewModel.artistMusic.collectAsState()
    val artistAnalytics by artistViewModel.artistAnalytics.collectAsState()
    val followers by followViewModel.followers.collectAsState()
    val isUploading by artistViewModel.isUploading.collectAsState()
    val uploadProgress by artistViewModel.uploadProgress.collectAsState()
    val operationStatus by artistViewModel.operationStatus.collectAsState()
    val selectedTab by artistViewModel.selectedTab.collectAsState()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(operationStatus) {
        operationStatus?.let {
            snackbarHostState.showSnackbar(it)
            artistViewModel.clearOperationStatus()
        }
    }

    LaunchedEffect(Unit) {
        artistViewModel.currentArtist.value?.id?.let { id ->
            followViewModel.loadFollowers(id)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "dash_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f, targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(4000, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "glow"
    )

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        // Ambient glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(RivoPurple.copy(alpha = glowAlpha), DarkBackground)
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 44.dp, start = 20.dp, end = 20.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.06f))
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = White, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Artist Studio",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black, color = White, letterSpacing = (-0.5).sp
                        )
                    )
                    Text(
                        when (selectedTab) {
                            ArtistDashboardTab.UPLOAD_MUSIC -> "Share your talent"
                            ArtistDashboardTab.MY_MUSIC -> "Your creative catalog"
                            ArtistDashboardTab.ANALYTICS -> "Audience insights"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(color = LightGray)
                    )
                }
                
                // Active indicators (e.g., pending count)
                if ((artistAnalytics?.pendingCount ?: 0) > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.2f))
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassBottom, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${artistAnalytics?.pendingCount}", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // Tab Switcher
            DashboardTabSwitcher(
                selectedTab = selectedTab,
                onTabSelected = { artistViewModel.setSelectedTab(it) }
            )

            // Content
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        slideInHorizontally { it / 2 } + fadeIn() togetherWith
                        slideOutHorizontally { -it / 2 } + fadeOut()
                    },
                    label = "tab_content"
                ) { tab ->
                    when (tab) {
                        ArtistDashboardTab.UPLOAD_MUSIC -> UploadTab(
                            isUploading = isUploading,
                            uploadProgress = uploadProgress,
                            categories = artistViewModel.categories.collectAsState().value,
                            onUploadMusic = { title, genre, description, audioUri, coverImageUri ->
                                if (audioUri != null) {
                                    artistViewModel.uploadMusic(
                                        context = context,
                                        title = title,
                                        genre = genre,
                                        description = description,
                                        audioUri = audioUri,
                                        coverImageUri = coverImageUri
                                    )
                                }
                            }
                        )
                        ArtistDashboardTab.MY_MUSIC -> {
                            // Ensure the artist's catalog is refreshed from backend
                            LaunchedEffect("artist_library_refresh") {
                                artistViewModel.currentArtist.value?.id?.let { id ->
                                    artistViewModel.loadArtistData(id)
                                }
                            }

                            MyMusicTab(
                                artistMusic = artistMusic.sortedByDescending { it.uploadDate },
                                onEditTrackClick = onEditTrackClick,
                                onDeleteTrackClick = { artistViewModel.deleteMusic(it.id) },
                                onUploadNewClick = { artistViewModel.setSelectedTab(ArtistDashboardTab.UPLOAD_MUSIC) }
                            )
                        }
                        ArtistDashboardTab.ANALYTICS -> {
                            var showFollowersSheet by remember { mutableStateOf(false) }
                            
                            AnalyticsTab(
                                artistAnalytics = artistAnalytics,
                                artistMusic = artistMusic,
                                onShowFollowers = { showFollowersSheet = true },
                                onNotificationClick = onNotificationClick
                            )

                            if (showFollowersSheet) {
                                ModalBottomSheet(
                                    onDismissRequest = { showFollowersSheet = false },
                                    containerColor = DarkSurface,
                                    scrimColor = Color.Black.copy(alpha = 0.5f)
                                ) {
                                    FollowersListSheet(
                                        followers = followers,
                                        onClose = { showFollowersSheet = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
        ) { data ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1A1A1A),
                border = BorderStroke(1.dp, White.copy(alpha = 0.1f)),
                shadowElevation = 8.dp
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = RivoPurple, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(data.visuals.message, color = White, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun DashboardTabSwitcher(selectedTab: ArtistDashboardTab, onTabSelected: (ArtistDashboardTab) -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .fillMaxWidth()
            .height(56.dp)
            .background(White.copy(alpha = 0.04f), RoundedCornerShape(28.dp))
            .padding(4.dp)
    ) {
        val tabs = listOf(
            Triple(ArtistDashboardTab.UPLOAD_MUSIC, Icons.Outlined.CloudUpload, "Publish"),
            Triple(ArtistDashboardTab.MY_MUSIC, Icons.Outlined.LibraryMusic, "Library"),
            Triple(ArtistDashboardTab.ANALYTICS, Icons.Outlined.ShowChart, "Insights")
        )

        tabs.forEach { (tab, icon, label) ->
            val isSelected = selectedTab == tab
            val bgColor by animateColorAsState(if (isSelected) RivoPurple.copy(alpha = 0.15f) else Color.Transparent)
            val iconTint by animateColorAsState(if (isSelected) RivoPurple else LightGray)
            val textColor by animateColorAsState(if (isSelected) White else LightGray)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .background(bgColor)
                    .clickable { onTabSelected(tab) },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(label, color = textColor, style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium))
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// TABS
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun UploadTab(
    isUploading: Boolean,
    uploadProgress: Float?,
    categories: List<MusicCategory>,
    onUploadMusic: (String, String, String, Uri?, Uri?) -> Unit
) {
    val context = LocalContext.current
    var trackTitle by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var audioUri by remember { mutableStateOf<Uri?>(null) }
    var coverImageUri by remember { mutableStateOf<Uri?>(null) }
    var coverImageLocalPath by remember { mutableStateOf<String?>(null) }
    var audioFileName by remember { mutableStateOf<String?>(null) }

    val mediaAccessHelper = remember { if (context is FragmentActivity) MediaAccessHelper(context) else null }

    val audioPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            audioUri = uri
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex("_display_name")
                    if (idx != -1) audioFileName = cursor.getString(idx)
                }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            coverImageUri = uri
            ImagePickerHelper.saveImageToInternalStorageAsync(context, uri, "temp_cover_${System.currentTimeMillis()}.jpg") { localPath ->
                coverImageLocalPath = localPath
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("Publish Track", style = MaterialTheme.typography.titleLarge.copy(color = White, fontWeight = FontWeight.Black))
                Text("Fill in the details below to share your creation", style = MaterialTheme.typography.bodySmall.copy(color = LightGray))
            }
        }

        item {
            UploadField(value = trackTitle, onValueChange = { trackTitle = it }, label = "Track Title", placeholder = "Title of your masterpiece")
        }

        item {
            Column {
                Text("Genre", style = MaterialTheme.typography.labelMedium.copy(color = LightGray, fontWeight = FontWeight.SemiBold))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val genreList = if (categories.isNotEmpty()) {
                        categories.map { c: MusicCategory -> c.title }
                    } else {
                        listOf("Pop", "Afrobeat", "Hip Hop", "R&B", "Electronic", "Ethiopian", "Rock", "Jazz")
                    }
                    
                    genreList.forEach { g ->
                        val selected = genre == g
                        Surface(
                            modifier = Modifier.clickable { genre = g },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) RivoPurple.copy(alpha = 0.2f) else White.copy(alpha = 0.04f),
                            border = BorderStroke(1.dp, if (selected) RivoPurple else White.copy(alpha = 0.1f))
                        ) {
                            Text(g, color = if (selected) RivoPurple else White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        item {
            UploadField(value = description, onValueChange = { description = it }, label = "Description", placeholder = "Tell your listeners about this track", maxLines = 3, minHeight = 100.dp)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Audio Selection
                Surface(
                    modifier = Modifier.weight(1f).height(140.dp).clickable {
                        if (mediaAccessHelper != null) mediaAccessHelper.pickAudioFile { uri ->
                            audioUri = uri
                            context.contentResolver.query(uri, null, null, null, null)?.use { c -> if (c.moveToFirst()) audioFileName = c.getString(c.getColumnIndexOrThrow("_display_name")) }
                        } else audioPickerLauncher.launch("audio/*")
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = White.copy(alpha = 0.04f),
                    border = BorderStroke(1.dp, if (audioUri != null) SuccessGreen.copy(alpha = 0.3f) else White.copy(alpha = 0.08f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (audioUri != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.QueueMusic, null, tint = SuccessGreen, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(audioFileName ?: "Audio Ready", color = White, fontSize = 12.sp, maxLines = 1, textAlign = TextAlign.Center)
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.MusicNote, null, tint = LightGray, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Add Audio", color = White, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                // Image Selection
                Surface(
                    modifier = Modifier.weight(1f).height(140.dp).clickable { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    shape = RoundedCornerShape(20.dp),
                    color = White.copy(alpha = 0.04f),
                    border = BorderStroke(1.dp, if (coverImageUri != null) SuccessGreen.copy(alpha = 0.3f) else White.copy(alpha = 0.08f))
                ) {
                    if (coverImageUri != null) {
                        AsyncImage(model = coverImageLocalPath ?: coverImageUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.Image, null, tint = LightGray, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Add Cover", color = White, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }

        item {
            if (isUploading && uploadProgress != null) {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("Uploading...", color = White, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("${(uploadProgress * 100).toInt()}%", color = RivoPurple, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { uploadProgress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = RivoPurple,
                        trackColor = White.copy(alpha = 0.05f)
                    )
                }
            } else {
                Button(
                    onClick = {
                        val finalCoverUri = if (coverImageLocalPath != null) Uri.parse("file://$coverImageLocalPath") else coverImageUri
                        onUploadMusic(trackTitle, genre, description, audioUri, finalCoverUri)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RivoPurple, disabledContainerColor = White.copy(alpha = 0.05f)),
                    enabled = !isUploading && trackTitle.isNotBlank() && genre.isNotBlank() && audioUri != null
                ) {
                    Text("Publish Masterpiece", fontWeight = FontWeight.Bold)
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
private fun UploadField(value: String, onValueChange: (String) -> Unit, label: String, placeholder: String, minHeight: androidx.compose.ui.unit.Dp = 56.dp, maxLines: Int = 1) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium.copy(color = LightGray, fontWeight = FontWeight.SemiBold))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = minHeight),
            placeholder = { Text(placeholder, color = DarkGray, style = MaterialTheme.typography.bodyMedium) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RivoPurple, unfocusedBorderColor = White.copy(alpha = 0.08f),
                focusedTextColor = White, unfocusedTextColor = White,
                focusedContainerColor = White.copy(alpha = 0.03f), unfocusedContainerColor = White.copy(alpha = 0.03f)
            ),
            shape = RoundedCornerShape(16.dp), maxLines = maxLines
        )
    }
}

@Composable
private fun MyMusicTab(artistMusic: List<Music>, onEditTrackClick: (Music) -> Unit, onDeleteTrackClick: (Music) -> Unit, onUploadNewClick: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(artistMusic, searchQuery) {
        if (searchQuery.isBlank()) artistMusic else artistMusic.filter { (it.title ?: "").contains(searchQuery, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                placeholder = { Text("Search your catalog...", color = DarkGray, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = DarkGray, modifier = Modifier.size(20.dp)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RivoPurple, unfocusedBorderColor = White.copy(alpha = 0.08f),
                    focusedTextColor = White, unfocusedTextColor = White,
                    focusedContainerColor = White.copy(alpha = 0.04f), unfocusedContainerColor = White.copy(alpha = 0.04f)
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
        }

        if (filtered.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.MusicOff, null, tint = DarkGray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Your library is empty", color = White, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onUploadNewClick) { Text("Start Uploading", color = RivoPurple) }
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { music ->
                TrackManagementCard(music, onEditClick = { onEditTrackClick(music) }, onDeleteClick = { onDeleteTrackClick(music) })
            }
        }
        
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
private fun TrackManagementCard(music: Music, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = White.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, White.copy(alpha = 0.06f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(60.dp), shape = RoundedCornerShape(12.dp), color = DarkSurface) {
                if (!music.artworkUri.isNullOrEmpty()) {
                    AsyncImage(model = music.artworkUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.MusicNote, null, tint = LightGray) }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(music.title ?: "", color = White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(music.genre ?: "General", color = RivoPurple, style = MaterialTheme.typography.labelSmall)
                Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, null, tint = DarkGray, modifier = Modifier.size(14.dp))
                    Text("${music.playCount} plays", color = DarkGray, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                }
            }
            
            var expanded by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { expanded = true }) { Icon(Icons.Default.MoreVert, null, tint = LightGray) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(DarkSurface)) {
                    DropdownMenuItem(text = { Text("Edit Track", color = White) }, onClick = { onEditClick(); expanded = false }, leadingIcon = { Icon(Icons.Default.Edit, null, tint = RivoPurple) })
                    DropdownMenuItem(text = { Text("Delete", color = Color.Red) }, onClick = { onDeleteClick(); expanded = false }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) })
                }
            }
        }
    }
}

@Composable
private fun AnalyticsTab(
    artistAnalytics: ArtistAnalytics?,
    artistMusic: List<Music>,
    onShowFollowers: () -> Unit,
    onNotificationClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Growth & Performance", style = MaterialTheme.typography.titleLarge.copy(color = White, fontWeight = FontWeight.Black))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Total Plays",
                    value = formatNumber(artistAnalytics?.totalPlays ?: 0),
                    icon = Icons.Default.ShowChart,
                    gradient = listOf(RivoPurple, RivoBlue),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Followers",
                    value = formatNumber(artistAnalytics?.newFollowers ?: 0),
                    icon = Icons.Default.Group,
                    gradient = listOf(RivoPink, RivoPurple),
                    modifier = Modifier.weight(1f).clickable { onShowFollowers() }
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Tracks",
                    value = (artistAnalytics?.totalSongs ?: artistMusic.size).toString(),
                    icon = Icons.Default.LibraryMusic,
                    gradient = listOf(RivoBlue, RivoCyan),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Pending",
                    value = (artistAnalytics?.pendingCount ?: 0).toString(),
                    icon = Icons.Default.Schedule,
                    gradient = listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        if ((artistAnalytics?.unreadNotifications ?: 0) > 0) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { onNotificationClick() },
                    shape = RoundedCornerShape(16.dp),
                    color = RivoPurple.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, RivoPurple.copy(alpha = 0.1f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, null, tint = RivoPurple)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("${artistAnalytics?.unreadNotifications} new updates await your review", color = White, fontSize = 14.sp)
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Text("Performance Trend", style = MaterialTheme.typography.titleMedium.copy(color = White, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = White.copy(alpha = 0.03f),
                    border = BorderStroke(1.dp, White.copy(alpha = 0.06f))
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.BottomCenter) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            val values = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 1.0f)
                            values.forEach { h ->
                                Box(
                                    modifier = Modifier
                                        .width(32.dp)
                                        .fillMaxHeight(h)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(Brush.verticalGradient(listOf(RivoPurple, RivoPurple.copy(alpha = 0.2f))))
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("Top Tracks", style = MaterialTheme.typography.titleMedium.copy(color = White, fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 8.dp))
        }

        val topTracks = artistMusic.sortedByDescending { it.playCount }.take(5)
        if (topTracks.isNotEmpty()) {
            items(topTracks) { music ->
                RankedTrackRow(music, rank = topTracks.indexOf(music) + 1)
            }
        } else {
            item { Text("Upload music to see performance", color = DarkGray, style = MaterialTheme.typography.bodySmall) }
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
private fun FollowersListSheet(followers: List<com.rivo.app.data.model.User>, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp, max = 600.dp)
            .padding(24.dp)
    ) {
        Text(
            text = "Your Followers",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black,
                color = White
            )
        )
        Text(
            text = "${followers.size} people following your journey",
            style = MaterialTheme.typography.bodySmall.copy(color = LightGray)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (followers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Group, null, tint = DarkGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No followers yet", color = DarkGray)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(followers) { follower ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = White.copy(alpha = 0.05f)
                        ) {
                            if (!follower.profileImageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = follower.profileImageUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, null, tint = LightGray)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = follower.fullName.ifBlank { follower.name },
                                color = White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "@${follower.name}",
                                color = LightGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = White.copy(alpha = 0.06f))
        ) {
            Text("Close", color = White, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun StatCard(title: String, value: String, icon: ImageVector, gradient: List<Color>, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = White.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, White.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(gradient.first().copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = gradient.first(), modifier = Modifier.size(20.dp)) }
            Spacer(modifier = Modifier.height(16.dp))
            Text(value, color = White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text(title, color = LightGray, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun RankedTrackRow(music: Music, rank: Int) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("$rank", color = if (rank <= 3) RivoPurple else DarkGray, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, modifier = Modifier.width(32.dp))
        Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(10.dp), color = DarkSurface) {
            AsyncImage(model = music.artworkUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(music.title ?: "", color = White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${formatNumber(music.playCount)} plays", color = DarkGray, style = MaterialTheme.typography.labelSmall)
        }
        Box(modifier = Modifier.height(24.dp).width(1.dp).background(White.copy(alpha = 0.05f)))
        Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = SuccessGreen.copy(alpha = 0.6f), modifier = Modifier.size(16.dp).padding(start = 12.dp))
    }
}

private fun formatNumber(number: Int): String {
    return when {
        number >= 1_000_000 -> String.format(Locale.US, "%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format(Locale.US, "%.1fK", number / 1_000.0)
        else -> number.toString()
    }
}

