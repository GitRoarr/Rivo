package com.rivo.app.ui.screens.artist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.Session
import com.rivo.app.data.model.User
import com.rivo.app.data.repository.SessionManager
import com.rivo.app.ui.theme.Primary
import com.rivo.app.ui.viewmodel.ArtistViewModel
import com.rivo.app.ui.viewmodel.FollowViewModel
import com.rivo.app.ui.viewmodel.MusicViewModel
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

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
    LocalContext.current
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0C0C12), Color.Black)
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            item {
                ArtistHeroSection(
                    artist = currentArtist,
                    followersCount = followersCount,
                    tracksCount = artistMusic.size,
                    isFollowing = isFollowing,
                    isOwnProfile = currentSession?.userId == artistId,
                    onBackClick = onBackClick,
                    onFollowClick = {
                        coroutineScope.launch {
                            followViewModel.toggleFollow(artistId)
                        }
                    }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Popular Songs",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${artistMusic.size} tracks",
                            color = Color(0xFFB7B7B7),
                            fontSize = 13.sp
                        )
                    }

                    FloatingActionButton(
                        onClick = { if (artistMusic.isNotEmpty()) onMusicClick(artistMusic.first()) },
                        containerColor = Primary,
                        contentColor = Color.White,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play all",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            itemsIndexed(artistMusic) { index, music ->
                EnhancedSongItem(
                    rank = index + 1,
                    music = music,
                    onPlayClick = { onMusicClick(music) }
                )
            }

            currentArtist?.bio?.takeIf { it.isNotBlank() }?.let { bio ->
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF161621)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 22.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "About",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = bio,
                                color = Color(0xFFC7C7C7),
                                fontSize = 14.sp,
                                lineHeight = 21.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistHeroSection(
    artist: User?,
    followersCount: Int,
    tracksCount: Int,
    isFollowing: Boolean,
    isOwnProfile: Boolean,
    onBackClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
    ) {
        if (!artist?.coverImageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(artist.coverImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Primary.copy(alpha = 0.9f), Color.Black)
                        )
                    )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black
                        )
                    )
                )
        )

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 12.dp, top = 6.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f))
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            if (!artist?.profileImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = artist?.profileImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = artist?.fullName?.ifBlank { artist.name } ?: "Artist",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (artist?.isVerified == true) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Primary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "@${artist?.name ?: "artist"}",
                color = Color(0xFFE0E0E0),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailStatChip(text = formatFollowersCount(followersCount))
                DetailStatChip(text = "$tracksCount Tracks")
            }

            if (!isOwnProfile) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onFollowClick,
                    shape = RoundedCornerShape(12.dp),
                    border = if (isFollowing) BorderStroke(1.dp, Color(0xFF8A8A8A)) else null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowing) Color.Transparent else Primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (isFollowing) "Following" else "Follow Artist",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailStatChip(text: String) {
    Surface(
        color = Color(0x33121212),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, Color(0x44FFFFFF))
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun EnhancedSongItem(
    rank: Int,
    music: Music,
    onPlayClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF15151D)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onPlayClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = rank.toString(),
                color = Color(0xFF9B9BA1),
                fontSize = 13.sp,
                modifier = Modifier.width(20.dp)
            )

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.DarkGray)
            ) {
                if (!music.artworkUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(music.artworkUri)
                            .crossfade(true)
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
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = music.title ?: "Unknown title",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${music.artist ?: "Unknown artist"} • ${formatDuration(music.duration)}",
                    color = Color(0xFF9D9DA4),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Primary,
                modifier = Modifier.size(22.dp)
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
