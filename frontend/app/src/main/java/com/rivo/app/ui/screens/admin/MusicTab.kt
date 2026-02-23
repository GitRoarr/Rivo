package com.rivo.app.ui.screens.admin

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.MusicApprovalStatus
import com.rivo.app.ui.theme.*
import com.rivo.app.ui.viewmodel.AdminViewModel

@Composable
fun MusicTab(
    pendingMusicApprovals: List<Music>,
    allMusic: List<Music>,
    featuredMusic: List<Music>,
    onMusicClick: (String) -> Unit,
    onApproveMusicClick: (String) -> Unit,
    onRejectMusicClick: (String) -> Unit,
    onFeatureMusicClick: (Music) -> Unit,
    onRemoveFromFeaturedClick: (String) -> Unit,
    onDeleteMusicClick: (String) -> Unit,
    onEditMusicClick: (String) -> Unit,
    adminViewModel: AdminViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredMusic = remember(allMusic, featuredMusic, searchQuery, selectedFilter) {
        val base = when (selectedFilter) {
            "Pending" -> allMusic.filter { it.approvalStatus == MusicApprovalStatus.PENDING }
            "Approved" -> allMusic.filter { it.approvalStatus == MusicApprovalStatus.APPROVED }
            "Featured" -> allMusic.filter { m -> featuredMusic.any { it.id == m.id } }
            else -> allMusic
        }
        if (searchQuery.isBlank()) base
        else base.filter { 
            (it.title ?: "").contains(searchQuery, ignoreCase = true) || 
            (it.artist ?: "").contains(searchQuery, ignoreCase = true) 
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(DarkBackground),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Music Moderation",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = White,
                        fontWeight = FontWeight.Black
                    )
                )
                Text(
                    "${allMusic.size} tracks in library",
                    style = MaterialTheme.typography.bodySmall.copy(color = LightGray)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Search and Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = White.copy(alpha = 0.04f),
                        border = BorderStroke(1.dp, White.copy(alpha = 0.06f))
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search...", color = DarkGray, style = MaterialTheme.typography.bodySmall) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = White,
                                unfocusedTextColor = White
                            )
                        )
                    }

                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        Surface(
                            modifier = Modifier.height(48.dp).clickable { expanded = true },
                            shape = RoundedCornerShape(12.dp),
                            color = RivoBlue.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, RivoBlue.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedFilter, color = RivoBlue, style = MaterialTheme.typography.labelMedium)
                                Icon(Icons.Default.FilterList, null, tint = RivoBlue, modifier = Modifier.size(16.dp).padding(start = 4.dp))
                            }
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color(0xFF252525))
                        ) {
                            listOf("All", "Pending", "Approved", "Featured").forEach { filter ->
                                DropdownMenuItem(
                                    text = { Text(filter, color = White) },
                                    onClick = { selectedFilter = filter; expanded = false }
                                )
                            }
                        }
                    }
                }
            }
        }

        items(filteredMusic, key = { it.id }) { music ->
            AdminMusicCard(
                music = music,
                isFeatured = featuredMusic.any { it.id == music.id },
                onClick = { onMusicClick(music.id) },
                onApprove = { onApproveMusicClick(music.id) },
                onReject = { onRejectMusicClick(music.id) },
                onFeature = { onFeatureMusicClick(music) },
                onRemoveFeatured = { onRemoveFromFeaturedClick(music.id) },
                onDelete = { onDeleteMusicClick(music.id) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun AdminMusicCard(
    music: Music,
    isFeatured: Boolean,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onFeature: () -> Unit,
    onRemoveFeatured: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = White.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(White.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                if (!music.artworkUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = music.artworkUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.MusicNote, null, tint = RivoBlue, modifier = Modifier.size(24.dp))
                }
                
                if (isFeatured) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, null, tint = DarkBackground, modifier = Modifier.size(10.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    music.title ?: "",
                    style = MaterialTheme.typography.titleSmall.copy(color = White, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    music.artist ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(color = LightGray),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(modifier = Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (music.approvalStatus) {
                                    MusicApprovalStatus.PENDING -> WarningYellow.copy(alpha = 0.1f)
                                    MusicApprovalStatus.APPROVED -> SuccessGreen.copy(alpha = 0.1f)
                                    else -> Color.Red.copy(alpha = 0.1f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            music.approvalStatus.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = when (music.approvalStatus) {
                                    MusicApprovalStatus.PENDING -> WarningYellow
                                    MusicApprovalStatus.APPROVED -> SuccessGreen
                                    else -> Color.Red
                                },
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        music.genre ?: "General",
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkGray)
                    )
                }
            }

            // Moderation Actions or Menu
            if (music.approvalStatus == MusicApprovalStatus.PENDING) {
                Row {
                    IconButton(onClick = onReject) {
                        Icon(Icons.Default.Close, null, tint = Color.Red.copy(alpha = 0.7f))
                    }
                    IconButton(onClick = onApprove) {
                        Icon(Icons.Default.Check, null, tint = SuccessGreen)
                    }
                }
            } else {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, null, tint = LightGray)
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF252525))
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Details", color = White) },
                            leadingIcon = { Icon(Icons.Default.Visibility, null, tint = LightGray) },
                            onClick = { onClick(); showMenu = false }
                        )
                        
                        if (isFeatured) {
                            DropdownMenuItem(
                                text = { Text("Remove Featured", color = White) },
                                leadingIcon = { Icon(Icons.Default.StarOutline, null, tint = LightGray) },
                                onClick = { onRemoveFeatured(); showMenu = false }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Feature Song", color = Color(0xFFF59E0B)) },
                                leadingIcon = { Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B)) },
                                onClick = { onFeature(); showMenu = false }
                            )
                        }

                        Divider(color = White.copy(alpha = 0.05f))

                        DropdownMenuItem(
                            text = { Text("Delete Track", color = Color.Red) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) },
                            onClick = { onDelete(); showMenu = false }
                        )
                    }
                }
            }
        }
    }
}
