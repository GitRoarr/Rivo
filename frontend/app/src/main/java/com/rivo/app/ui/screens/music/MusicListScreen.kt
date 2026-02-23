package com.rivo.app.ui.screens.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rivo.app.data.model.Music
import com.rivo.app.ui.components.RivoTopBar
import com.rivo.app.ui.theme.Primary
import com.rivo.app.ui.viewmodel.MusicViewModel
import coil.compose.AsyncImage
import java.util.concurrent.TimeUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicListScreen(
    listType: String,
    musicViewModel: MusicViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onMusicClick: (Music) -> Unit
) {
    // select which stream to show
    val musicList by when (listType) {
        "trending" -> musicViewModel.trendingMusic.collectAsState(emptyList())
        "new"      -> musicViewModel.newReleases.collectAsState(emptyList())
        else       -> musicViewModel.allMusic.collectAsState(emptyList())
    }

    val title = when (listType) {
        "trending" -> "Trending Now"
        "new"      -> "New Releases"
        else       -> "All Songs"
    }

    Scaffold(
        topBar = {
            RivoTopBar(
                title = title,
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, "Search", tint = Color.White)
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { padding ->
        if (musicList.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(musicList) { track ->
                    MusicRow(track, onClick = { onMusicClick(track) })
                }
            }
        }
    }
}

@Composable
private fun MusicRow(
    music: Music,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = music.artworkUri,
            contentDescription = music.title ?: "",
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFF333333), shape = MaterialTheme.shapes.small),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = music.title ?: "",
                color = Color.White,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = music.artist ?: "",
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = formatDuration(music.duration),
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

private fun formatDuration(ms: Long): String {
    val m = TimeUnit.MILLISECONDS.toMinutes(ms)
    val s = TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(m)
    return String.format(Locale.getDefault(), "%d:%02d", m, s)
}
