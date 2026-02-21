package com.rivo.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.rivo.app.data.model.FeaturedType
import com.rivo.app.data.model.User
import com.rivo.app.data.model.UserType
import com.rivo.app.data.model.VerificationStatus
import com.rivo.app.ui.theme.Primary
import com.rivo.app.ui.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VerificationTab(
    pendingVerifications: List<User>,
    featuredArtists: List<User>, // Change from List<String> to List<User>
    onVerificationClick: (String) -> Unit,
    onApproveVerificationClick: (String) -> Unit,
    onRejectVerificationClick: (String) -> Unit,
    onFeatureArtistClick: (User) -> Unit, // Changed to accept User
    onRemoveFromFeaturedClick: (String) -> Unit,
    adminViewModel: AdminViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var showDropdownMenu by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("Pending") }

    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search and filter row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search verifications...", color = Color.Gray) },
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
                    listOf("All", "Pending", "Approved", "Rejected").forEach { filter ->
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Artist",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.width(180.dp).padding(horizontal = 8.dp)
            )

            Text(
                text = "Email",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.width(180.dp).padding(horizontal = 8.dp)
            )

            Text(
                text = "Submitted",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.width(120.dp).padding(horizontal = 8.dp)
            )

            Text(
                text = "Location",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.width(120.dp).padding(horizontal = 8.dp)
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

        // Verification list
        LazyColumn {
            val filteredVerifications = pendingVerifications.filter {
                when (selectedFilter) {
                    "Pending" -> it.verificationStatus == VerificationStatus.PENDING
                    "Approved" -> it.verificationStatus == VerificationStatus.VERIFIED
                    "Rejected" -> it.verificationStatus == VerificationStatus.REJECTED
                    else -> it.verificationStatus != VerificationStatus.UNVERIFIED
                }
            }.filter {
                if (searchQuery.isBlank()) true
                else it.name.contains(searchQuery, ignoreCase = true) ||
                        it.email.contains(searchQuery, ignoreCase = true)
            }

            items(filteredVerifications) { user ->
                VerificationListItem(
                    user = user,
                    isFeatured = featuredArtists.any { it.id == user.id },
                    onClick = { onVerificationClick(user.verificationRequestId ?: user.id) },
                    onMenuClick = { showDropdownMenu = user.id },
                    showMenu = showDropdownMenu == user.id,
                    onDismissMenu = { showDropdownMenu = null },
                    onApproveClick = { onApproveVerificationClick(user.id) },
                    onRejectClick = { onRejectVerificationClick(user.id) },
                    onFeatureClick = { onFeatureArtistClick(user) }, // Updated here
                    onRemoveFromFeaturedClick = { onRemoveFromFeaturedClick(user.id) },
                    horizontalScrollState = horizontalScrollState
                )

                Divider(color = Color.DarkGray.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun VerificationListItem(
    user: User,
    isFeatured: Boolean,
    onClick: () -> Unit,
    onMenuClick: () -> Unit,
    showMenu: Boolean,
    onDismissMenu: () -> Unit,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit,
    onFeatureClick: () -> Unit,
    onRemoveFromFeaturedClick: () -> Unit,
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
        // Artist name and profile pic
        Row(
            modifier = Modifier.width(180.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                if (user.profilePictureUrl != null) {
                    AsyncImage(
                        model = user.profilePictureUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = user.name.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = user.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (user.isVerified) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(14.dp)
                        )

                        Spacer(modifier = Modifier.width(2.dp))

                        Text(
                            text = "Verified",
                            color = Primary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Text(
            text = user.email,
            color = Color.White,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(180.dp).padding(horizontal = 8.dp)
        )

        Text(
            text = user.verificationRequestDate?.let { formatDate(it.toString()) } ?: "-",
            color = Color.White,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(120.dp).padding(horizontal = 8.dp)
        )

        Text(
            text = user.location ?: "Unknown",
            color = Color.White,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(120.dp).padding(horizontal = 8.dp)
        )

        // Status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(100.dp).padding(horizontal = 8.dp)
        ) {
            val statusColor = when (user.verificationStatus) {
                VerificationStatus.PENDING -> Color(0xFFFF9800) // Orange for pending
                VerificationStatus.VERIFIED -> Primary
                VerificationStatus.REJECTED -> Color.Red
                else -> Color.Gray
            }

            Text(
                text = user.verificationStatus.name,
                color = statusColor,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 4.dp)
            )
        }

        // Actions
        Box(
            modifier = Modifier.width(80.dp).padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.White
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = onDismissMenu,
                modifier = Modifier.background(Color(0xFF1E1E1E))
            ) {
                DropdownMenuItem(
                    onClick = { onApproveClick() },
                    text = { Text("Approve", color = Color.White) }
                )
                DropdownMenuItem(
                    onClick = { onRejectClick() },
                    text = { Text("Reject", color = Color.White) }
                )
                DropdownMenuItem(
                    onClick = {
                        if (!isFeatured) onFeatureClick() else onRemoveFromFeaturedClick()
                    },
                    text = {
                        Text(
                            if (isFeatured) "Remove from Featured" else "Feature Artist",
                            color = Color.White
                        )
                    }
                )
            }
        }
    }
}

fun formatDate(date: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val parsedDate = inputFormat.parse(date)
    return if (parsedDate != null) {
        outputFormat.format(parsedDate)
    } else {
        "-"
    }
}
