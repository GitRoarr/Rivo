package com.rivo.app.ui.screens.artist
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
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
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.Session
import com.rivo.app.data.repository.SessionManager
import com.rivo.app.ui.theme.Primary
import com.rivo.app.ui.viewmodel.ArtistViewModel
import com.rivo.app.ui.viewmodel.FollowViewModel
import com.rivo.app.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artistId: String,
    onBackClick: () -> Unit,
    onMusicClick: (Music) -> Unit,
    artistViewModel: ArtistViewModel,
    musicViewModel: MusicViewModel,
    followViewModel: FollowViewModel,
    sessionManager: SessionManager
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentArtist by artistViewModel.currentArtist.collectAsState()
    val artistMusic by artistViewModel.artistMusic.collectAsState()
    val isFollowing by followViewModel.isFollowing.collectAsState()
    val followersCount by followViewModel.getFollowersCount.collectAsState()

    var currentSession by remember { mutableStateOf<Session?>(null) }

    LaunchedEffect(Unit) {
        currentSession = sessionManager.getCurrentUser()
    }

    LaunchedEffect(artistId) {
        artistViewModel.loadArtistData(artistId)
        followViewModel.loadFollowersCount(artistId)
        followViewModel.checkFollowStatus(artistId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentArtist?.name ?: "Artist", color = Color.White) },
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
        containerColor = Color.Black
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentArtist != null) {
                            when {
                                currentArtist?.profileImageUrl?.startsWith("/") == true -> {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(File(currentArtist!!.profileImageUrl!!))
                                            .crossfade(true)
                                            .diskCachePolicy(CachePolicy.ENABLED)
                                            .memoryCachePolicy(CachePolicy.ENABLED)
                                            .build(),
                                        contentDescription = currentArtist?.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                !currentArtist?.profileImageUrl.isNullOrEmpty() -> {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(currentArtist?.profileImageUrl)
                                            .crossfade(true)
                                            .diskCachePolicy(CachePolicy.ENABLED)
                                            .memoryCachePolicy(CachePolicy.ENABLED)
                                            .build(),
                                        contentDescription = currentArtist?.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(60.dp)
                                    )
                                }
                            }
                        } else {
                            CircularProgressIndicator(color = Primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = currentArtist?.name ?: "Artist Name",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formatFollowersCount(followersCount),
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    if (currentSession != null && currentSession?.userId != artistId) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    followViewModel.toggleFollow(artistId)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) Color.DarkGray else Primary
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.width(120.dp)
                        ) {
                            Text(
                                text = if (isFollowing) "Following" else "Follow",
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Songs",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            items(artistMusic) { music ->
                SongItem(
                    music = music,
                    onPlayClick = { onMusicClick(music) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SongItem(
    music: Music,
    onPlayClick: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Song Cover Image
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            if (!music.artworkUri.isNullOrEmpty()) {
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
                    contentDescription = music.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Song Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = music.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = music.artist,
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Duration
        Text(
            text = formatDuration(music.duration),
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Play Button
        IconButton(
            onClick = onPlayClick,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Primary)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) -
            TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%d:%02d", minutes, seconds)
}

private fun formatFollowersCount(count: Int): String {
    return when {
        count < 1000 -> "$count followers"
        count < 1000000 -> String.format("%.1fK followers", count / 1000f)
        else -> String.format("%.1fM followers", count / 1000000f)
    }
}
