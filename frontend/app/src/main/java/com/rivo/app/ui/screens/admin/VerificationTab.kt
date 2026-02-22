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
import androidx.compose.material.icons.outlined.*
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
import com.rivo.app.data.model.User
import com.rivo.app.data.model.VerificationStatus
import com.rivo.app.ui.theme.*
import com.rivo.app.ui.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VerificationTab(
    pendingVerifications: List<User>,
    featuredArtists: List<User>,
    onVerificationClick: (String) -> Unit,
    onApproveVerificationClick: (String) -> Unit,
    onRejectVerificationClick: (String) -> Unit,
    onFeatureArtistClick: (User) -> Unit,
    onRemoveFromFeaturedClick: (String) -> Unit,
    adminViewModel: AdminViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Pending") }

    val filteredRequests = remember(pendingVerifications, searchQuery, selectedFilter) {
        val base = when (selectedFilter) {
            "Pending" -> pendingVerifications.filter { it.verificationStatus == VerificationStatus.PENDING }
            "Approved" -> pendingVerifications.filter { it.verificationStatus == VerificationStatus.VERIFIED }
            "Rejected" -> pendingVerifications.filter { it.verificationStatus == VerificationStatus.REJECTED }
            else -> pendingVerifications
        }
        if (searchQuery.isBlank()) base
        else base.filter { 
            it.name.contains(searchQuery, ignoreCase = true) || 
            it.email.contains(searchQuery, ignoreCase = true) 
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(DarkBackground),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Artist Verification",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = White,
                        fontWeight = FontWeight.Black
                    )
                )
                Text(
                    "${pendingVerifications.size} requests found",
                    style = MaterialTheme.typography.bodySmall.copy(color = LightGray)
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                            placeholder = { Text("Search artists...", color = DarkGray, style = MaterialTheme.typography.bodySmall) },
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
                            color = RivoPurple.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, RivoPurple.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedFilter, color = RivoPurple, style = MaterialTheme.typography.labelMedium)
                                Icon(Icons.Default.FilterList, null, tint = RivoPurple, modifier = Modifier.size(16.dp).padding(start = 4.dp))
                            }
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color(0xFF252525))
                        ) {
                            listOf("All", "Pending", "Approved", "Rejected").forEach { filter ->
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

        items(filteredRequests, key = { it.id }) { user ->
            AdminVerificationCard(
                user = user,
                isFeatured = featuredArtists.any { it.id == user.id },
                onClick = { onVerificationClick(user.verificationRequestId ?: user.id) },
                onApprove = { onApproveVerificationClick(user.id) },
                onReject = { onRejectVerificationClick(user.id) },
                onFeature = { onFeatureArtistClick(user) },
                onRemoveFeatured = { onRemoveFromFeaturedClick(user.id) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun AdminVerificationCard(
    user: User,
    isFeatured: Boolean,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onFeature: () -> Unit,
    onRemoveFeatured: () -> Unit,
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
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                if (!user.profileImageUrl.isNullOrEmpty() || !user.profilePictureUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = user.profileImageUrl ?: user.profilePictureUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        user.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(color = White, fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.name,
                    style = MaterialTheme.typography.titleSmall.copy(color = White, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    user.email,
                    style = MaterialTheme.typography.bodySmall.copy(color = LightGray),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (user.verificationStatus) {
                                    VerificationStatus.PENDING -> WarningYellow.copy(alpha = 0.1f)
                                    VerificationStatus.VERIFIED -> SuccessGreen.copy(alpha = 0.1f)
                                    else -> Color.Red.copy(alpha = 0.1f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            user.verificationStatus.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = when (user.verificationStatus) {
                                    VerificationStatus.PENDING -> WarningYellow
                                    VerificationStatus.VERIFIED -> SuccessGreen
                                    else -> Color.Red
                                },
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        user.location ?: "Global",
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkGray)
                    )
                }
            }

            if (user.verificationStatus == VerificationStatus.PENDING) {
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
                            text = { Text("Review Request", color = White) },
                            leadingIcon = { Icon(Icons.Outlined.ContentPasteSearch, null, tint = LightGray) },
                            onClick = { onClick(); showMenu = false }
                        )

                        if (user.verificationStatus == VerificationStatus.VERIFIED) {
                            if (isFeatured) {
                                DropdownMenuItem(
                                    text = { Text("Remove from Home", color = White) },
                                    leadingIcon = { Icon(Icons.Default.StarOutline, null, tint = LightGray) },
                                    onClick = { onRemoveFeatured(); showMenu = false }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Feature Artist", color = Color(0xFFF59E0B)) },
                                    leadingIcon = { Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B)) },
                                    onClick = { onFeature(); showMenu = false }
                                )
                            }
                        }

                        Divider(color = White.copy(alpha = 0.05f))

                        if (user.verificationStatus == VerificationStatus.VERIFIED) {
                            DropdownMenuItem(
                                text = { Text("Revoke Verification", color = Color.Red) },
                                leadingIcon = { Icon(Icons.Default.Undo, null, tint = Color.Red) },
                                onClick = { onReject(); showMenu = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
