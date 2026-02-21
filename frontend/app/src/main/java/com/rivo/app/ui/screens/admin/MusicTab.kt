package com.rivo.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.rivo.app.data.model.FeaturedType
import com.rivo.app.data.model.Music
import com.rivo.app.data.model.MusicApprovalStatus
import com.rivo.app.ui.theme.Primary
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
    var showDropdownMenu by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("All") }

    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search music...", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Primary,
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E)
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            // Filter dropdown
            Box {
                var expanded by remember { mutableStateOf(false) }

                OutlinedButton(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(selectedFilter)
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Filter"
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF1E1E1E))
                ) {
                    listOf("All", "Pending", "Approved", "Featured").forEach { filter ->
                        DropdownMenuItem(
                            text = { Text(filter, color = Color.White) },
                            onClick = {
                                selectedFilter = filter
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Table header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Title",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.width(180.dp).padding(horizontal = 8.dp)
            )

            Text(
                text = "Artist",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.width(120.dp).padding(horizontal = 8.dp)
            )

            Text(
                text = "Genre",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.width(100.dp).padding(horizontal = 8.dp)
            )

            Text(
                text = "Duration",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.width(80.dp).padding(horizontal = 8.dp)
            )

            Text(
                text = "Status",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.width(100.dp).padding(horizontal = 8.dp)
            )

            Text(
                text = "Actions",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.width(80.dp).padding(horizontal = 8.dp)
            )
        }

        Divider(
            color = Color.DarkGray,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn {
            val filteredMusic = when (selectedFilter) {
                "Pending" -> allMusic.filter { it.approvalStatus == MusicApprovalStatus.PENDING }
                "Approved" -> allMusic.filter { it.approvalStatus == MusicApprovalStatus.APPROVED && !featuredMusic.contains(it) }
                "Featured" -> allMusic.filter { featuredMusic.contains(it) }
                else -> allMusic
            }.filter {
                if (searchQuery.isBlank()) true
                else it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true)
            }

            items(filteredMusic) { music ->
                MusicListItem(
                    music = music,
                    isFeatured = featuredMusic.contains(music),
                    onClick = { onMusicClick(music.id) },
                    onMenuClick = { showDropdownMenu = music.id },
                    showMenu = showDropdownMenu == music.id,
                    onDismissMenu = { showDropdownMenu = null },
                    onApproveClick = { onApproveMusicClick(music.id) },
                    onRejectClick = { onRejectMusicClick(music.id) },
                    onFeatureClick = { onFeatureMusicClick(music) } ,
                    onRemoveFromFeaturedClick = { onRemoveFromFeaturedClick(music.id) },
                    onDeleteClick = { onDeleteMusicClick(music.id) },
                    onEditClick = { onEditMusicClick(music.id) },
                    horizontalScrollState = horizontalScrollState
                )

                Divider(color = Color.DarkGray.copy(alpha = 0.5f))
            }
        }


    }
}

@Composable
fun MusicListItem(
    music: Music,
    isFeatured: Boolean,
    onClick: () -> Unit,
    onMenuClick: () -> Unit,
    showMenu: Boolean,
    onDismissMenu: () -> Unit,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit,
    onFeatureClick: () -> Unit,
    onRemoveFromFeaturedClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    horizontalScrollState: androidx.compose.foundation.ScrollState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
            .horizontalScroll(horizontalScrollState),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title with album art
        Row(
            modifier = Modifier.width(180.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.DarkGray)
            ) {
                if (music.artworkUri != null) {
                    AsyncImage(
                        model = music.artworkUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = music.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (music.album != null) {
                    Text(
                        text = music.album,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Artist
        Text(
            text = music.artist,
            color = Color.White,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(120.dp).padding(horizontal = 8.dp)
        )

        // Genre
        Text(
            text = music.genre ?: "Unknown",
            color = Color.White,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(100.dp).padding(horizontal = 8.dp)
        )

        // Duration
        Text(
            text = formatDuration(music.duration.toInt()),
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.width(80.dp).padding(horizontal = 8.dp)
        )

        // Status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(100.dp).padding(horizontal = 8.dp)
        ) {
            val statusColor = when {
                music.approvalStatus == MusicApprovalStatus.PENDING -> Color(0xFFFF9800) // Orange for pending
                isFeatured -> Color(0xFFFFD700) // Gold for featured
                music.approvalStatus == MusicApprovalStatus.APPROVED -> Primary // Primary for approved
                else -> Color.Red // Red for rejected
            }

            val statusText = when {
                music.approvalStatus == MusicApprovalStatus.PENDING -> "Pending"
                isFeatured -> "Featured"
                music.approvalStatus == MusicApprovalStatus.APPROVED -> "Approved"
                else -> "Rejected"
            }

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = statusText,
                color = Color.White,
                fontSize = 14.sp
            )
        }

        // Actions
        Box(
            modifier = Modifier.width(80.dp).padding(horizontal = 8.dp)
        ) {
            IconButton(
                onClick = onMenuClick
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = Color.White
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = onDismissMenu,
                modifier = Modifier.background(Color(0xFF1E1E1E))
            ) {
                DropdownMenuItem(
                    text = { Text("View Details", color = Color.White) },
                    onClick = {
                        onClick()
                        onDismissMenu()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Edit Track", color = Color.White) },
                    onClick = {
                        onEditClick()
                        onDismissMenu()
                    }
                )

                if (music.approvalStatus == MusicApprovalStatus.PENDING) {
                    DropdownMenuItem(
                        text = { Text("Approve", color = Primary) },
                        onClick = {
                            onApproveClick()
                            onDismissMenu()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Reject", color = Color.Red) },
                        onClick = {
                            onRejectClick()
                            onDismissMenu()
                        }
                    )
                } else if (music.approvalStatus == MusicApprovalStatus.APPROVED) {
                    if (isFeatured) {
                        DropdownMenuItem(
                            text = { Text("Remove from Featured", color = Color(0xFFFF9800)) },
                            onClick = {
                                onRemoveFromFeaturedClick()
                                onDismissMenu()
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Add to Featured", color = Color(0xFFFFD700)) },
                            onClick = {
                                onFeatureClick()
                                onDismissMenu()
                            }
                        )
                    }
                }

                DropdownMenuItem(
                    text = { Text("Delete Track", color = Color.Red) },
                    onClick = {
                        onDeleteClick()
                        onDismissMenu()
                    }
                )
            }
        }
    }
}

// Helper function to format duration in seconds to mm:ss
private fun formatDuration(durationInSeconds: Int): String {
    val minutes = durationInSeconds / 60
    val seconds = durationInSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
