package com.rivo.app.ui.screens.player
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.rivo.app.R
import com.rivo.app.ui.theme.Primary
import com.rivo.app.ui.viewmodel.MusicViewModel
import com.rivo.app.utils.SimpleMediaAccessHelper
import kotlinx.coroutines.delay
import androidx.core.net.toUri
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    musicViewModel: MusicViewModel,
    musicId: String,
    onBackClick: () -> Unit,
    onArtistClick: (String) -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val currentMusic by musicViewModel.currentMusic.collectAsState()
    val isPlaying by musicViewModel.isPlaying.collectAsState()
    val currentPosition by musicViewModel.currentPosition.collectAsState()
    val duration by musicViewModel.duration.collectAsState()
    val repeatMode by musicViewModel.repeatMode.collectAsState()
    val isShuffleEnabled by musicViewModel.isShuffleEnabled.collectAsState()
    val isFavorite by musicViewModel.isFavorite.collectAsState()
    val error by musicViewModel.error.collectAsState()
    val isLoading by musicViewModel.isLoading.collectAsState()
    val debugInfo by musicViewModel.debugInfo.collectAsState()
    val context = LocalContext.current

    val backgroundColor = Color.Black

    LaunchedEffect(musicId) {
        Log.d("PlayerScreen", "LaunchedEffect triggered with musicId: $musicId")
        musicViewModel.loadMusic(musicId)
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            musicViewModel.updateCurrentPosition()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            musicViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        com.rivo.app.ui.components.RivoLogo(
                            size = 32.dp,
                            showText = false,
                            animated = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rivo", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else Color.White
                        )
                    }
                    IconButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Black,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(com.rivo.app.ui.theme.RivoPurple.copy(alpha = 0.3f), Color.Black)
                    )
                )
                .padding(paddingValues)
        ) {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Album Art with glowing effect
                Box(
                    modifier = Modifier
                        .size(310.dp)
                        .padding(10.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = com.rivo.app.ui.theme.RivoGradient
                            ),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .padding(3.dp), // Glowing border thickness
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(21.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        currentMusic?.artworkUri?.let { artworkUri ->
                            val imageUri = remember(artworkUri) {
                                SimpleMediaAccessHelper.getPlayableUri(context, artworkUri.toString())
                                    ?: artworkUri.toString().toUri()
                            }

                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageUri)
                                    .crossfade(true)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                    .error(R.drawable.ic_music_note)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } ?: Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(120.dp)
                        )

                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Song Info
                Text(
                    text = currentMusic?.title ?: "Song Title",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = currentMusic?.artist ?: "Artist",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable {
                        currentMusic?.artistId?.let { onArtistClick(it) }
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )


                Spacer(modifier = Modifier.height(24.dp))

                // Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = currentPosition.toFloat(),
                        onValueChange = { musicViewModel.seekTo(it.toLong()) },
                        valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = com.rivo.app.ui.theme.RivoCyan,
                            activeTrackColor = com.rivo.app.ui.theme.RivoCyan,
                            inactiveTrackColor = Color.DarkGray
                        ),
                        enabled = !isLoading && duration > 0
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = formatTime(currentPosition), color = Color.Gray, fontSize = 12.sp)
                        Text(text = formatTime(duration), color = Color.Gray, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Playback Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { musicViewModel.toggleShuffle() },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffleEnabled) com.rivo.app.ui.theme.RivoCyan else Color.Gray
                        )
                    }

                    IconButton(
                        onClick = { musicViewModel.skipToPrevious() },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = com.rivo.app.ui.theme.RivoGradient
                                )
                            )
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(Color.Black) // Ring effect
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = com.rivo.app.ui.theme.RivoGradient
                                )
                            )
                            .clickable(enabled = !isLoading) {
                                if (isPlaying) musicViewModel.pauseMusic()
                                else musicViewModel.resumeMusic()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { musicViewModel.skipToNext() },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = { musicViewModel.toggleRepeatMode() },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = when (repeatMode) {
                                1 -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = "Repeat",
                            tint = if (repeatMode > 0) com.rivo.app.ui.theme.RivoCyan else Color.Gray
                        )
                    }
                }
            }

            if (error != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF331111)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = error ?: "Unknown error",
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { musicViewModel.clearError() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
