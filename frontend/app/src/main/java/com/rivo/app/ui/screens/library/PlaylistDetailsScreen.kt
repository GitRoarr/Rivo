package com.rivo.app.ui.screens.library

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rivo.app.data.local.PlaylistWithMusic
import com.rivo.app.data.model.Music
import com.rivo.app.ui.theme.*
import com.rivo.app.ui.viewmodel.LibraryViewModel
import com.rivo.app.ui.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailsScreen(
    playlistId: Long,
    libraryViewModel: LibraryViewModel,
    musicViewModel: MusicViewModel,
    onBackClick: () -> Unit,
    onMusicClick: (Music) -> Unit
) {
    val playlistWithMusic by libraryViewModel.getPlaylistWithMusic(playlistId).collectAsState(initial = null)
    val scrollState = rememberScrollState()
    
    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        // Hero Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(RivoPurple.copy(alpha = 0.3f), DarkBackground)
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.background(White.copy(alpha = 0.1f), CircleShape)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Playlist",
                    style = MaterialTheme.typography.titleMedium.copy(color = White, fontWeight = FontWeight.Bold)
                )
            }

            if (playlistWithMusic == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RivoPink)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item {
                        PlaylistHeader(playlistWithMusic!!)
                    }
                    
                    if (playlistWithMusic!!.musicList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No songs in this playlist yet.", color = LightGray)
                            }
                        }
                    } else {
                        items(playlistWithMusic!!.musicList) { music ->
                            AmazingMusicRow(music, onClick = { onMusicClick(music) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistHeader(pwm: PlaylistWithMusic) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(200.dp)
                .shadow(32.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface
        ) {
            if (pwm.playlist.coverArtUrl != null) {
                AsyncImage(
                    model = pwm.playlist.coverArtUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = LightGray, modifier = Modifier.size(64.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = pwm.playlist.name ?: "",
            style = MaterialTheme.typography.headlineMedium.copy(color = White, fontWeight = FontWeight.Black),
            textAlign = TextAlign.Center
        )
        
        if (!pwm.playlist.description.isNullOrEmpty()) {
            Text(
                text = pwm.playlist.description ?: "",
                style = MaterialTheme.typography.bodyMedium.copy(color = LightGray),
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }
        
        val totalDuration = formatTotalDuration(pwm.musicList)
        Text(
            text = "${pwm.musicList.size} tracks • $totalDuration",
            style = MaterialTheme.typography.labelMedium.copy(color = RivoPink, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 12.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {},
                modifier = Modifier.height(54.dp).weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Play All", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            IconButton(
                onClick = {},
                modifier = Modifier.size(54.dp).background(White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = null, tint = White)
            }
        }
    }
}

// Re-using AmazingMusicRow or defining a similar one here if needed
// For now I will use the one I'll define in a common place or just here.
// I'll define it in LibraryScreen.kt and just copy it here for simplicity or move to a common file.
// Let's just define it here to be safe.

// AmazingMusicRow is used from LibraryScreen.kt (same package)

// Helper for TextAlign
private val TextAlign = androidx.compose.ui.text.style.TextAlign

private fun formatTotalDuration(musicList: List<Music>): String {
    val totalMs = musicList.sumOf { it.duration }
    val totalSeconds = totalMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    
    return when {
        hours > 0 -> "$hours hr $minutes min"
        else -> "$minutes min"
    }
}
