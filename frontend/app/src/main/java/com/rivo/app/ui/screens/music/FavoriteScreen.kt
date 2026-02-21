package com.rivo.app.ui.screens.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rivo.app.data.model.Music
import com.rivo.app.ui.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    musicViewModel: MusicViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onMusicClick: (String) -> Unit,
    onRemoveFromFavoritesClick: (String) -> Unit
) {
    val favoriteMusic by musicViewModel.favoriteMusic.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites", color = Color.White) },
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
        if (favoriteMusic.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No favorite songs yet",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(favoriteMusic) { music ->
                    FavoriteMusicItem(
                        music = music,
                        onClick = { onMusicClick(music.id) },
                        onFavoriteClick = {
                            musicViewModel.toggleFavorite(music.id)
                            onRemoveFromFavoritesClick(music.id)
                        },
                        onMenuItemClick = { action ->
                            when (action) {
                                "playlist" -> {
                                    // Handle add to playlist
                                }
                                "share" -> {
                                    // Handle share
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteMusicItem(
    music: Music,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onMenuItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A1A))
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Music artwork
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.DarkGray)
        ) {
            AsyncImage(
                model = music.artworkUri,
                contentDescription = music.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Music info
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = music.title,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = music.artist,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Play count
            Text(
                text = formatPlayCount(music.playCount),
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Favorite button
        IconButton(
            onClick = onFavoriteClick
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Remove from favorites",
                tint = Color.Red
            )
        }

        // Menu button
        Box {
            var showMenu by remember { mutableStateOf(false) }

            IconButton(
                onClick = { showMenu = true }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.White
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(Color(0xFF1A1A1A))
            ) {
                DropdownMenuItem(
                    text = { Text("Add to Playlist", color = Color.White) },
                    onClick = {
                        onMenuItemClick("playlist")
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                )

                DropdownMenuItem(
                    text = { Text("Share", color = Color.White) },
                    onClick = {
                        onMenuItemClick("share")
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                )
            }
        }
    }
}

private fun formatPlayCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "${count / 1_000_000}M plays"
        count >= 1_000 -> "${count / 1_000}K plays"
        else -> "$count plays"
    }
}