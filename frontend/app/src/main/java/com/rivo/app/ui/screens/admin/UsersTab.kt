package com.rivo.app.ui.screens.admin

import androidx.compose.foundation.*
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.rivo.app.data.model.User
import com.rivo.app.data.model.UserType
import com.rivo.app.ui.theme.*

@Composable
fun UsersTab(
    users: List<User>,
    onUserClick: (User) -> Unit,
    onSuspendUser: (String) -> Unit,
    onMakeAdmin: (String) -> Unit,
    onMakeArtist: (String) -> Unit,
    onFeatureArtist: (User) -> Unit,
    onDeleteUser: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var confirmDialog by remember { mutableStateOf<ConfirmAction?>(null) }

    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users
        else users.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.email.contains(searchQuery, ignoreCase = true)
        }
    }

    // Confirmation Dialog
    confirmDialog?.let { action ->
        ConfirmActionDialog(
            title = action.title,
            message = action.message,
            confirmText = action.confirmText,
            confirmColor = action.confirmColor,
            onConfirm = {
                action.onConfirm()
                confirmDialog = null
            },
            onDismiss = { confirmDialog = null }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(DarkBackground),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "User Management",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = White,
                        fontWeight = FontWeight.Black
                    )
                )
                Text(
                    "${users.size} registered accounts",
                    style = MaterialTheme.typography.bodySmall.copy(color = LightGray)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Modern Search Bar
                Surface(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = White.copy(alpha = 0.04f),
                    border = BorderStroke(1.dp, White.copy(alpha = 0.06f))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search by name or email...", color = DarkGray, style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = LightGray, modifier = Modifier.size(20.dp)) },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, null, tint = LightGray, modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = White,
                                unfocusedTextColor = White
                            ),
                            singleLine = true
                        )
                    }
                }
            }
        }

        if (filteredUsers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, null, tint = LightGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            if (searchQuery.isBlank()) "No users found" else "No results for \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium.copy(color = LightGray)
                        )
                    }
                }
            }
        }

        items(filteredUsers, key = { it.id }) { user ->
            AdminUserCard(
                user = user,
                onClick = { onUserClick(user) },
                onSuspendUser = {
                    if (user.isSuspended) {
                        // Unsuspend — no confirmation needed
                        onSuspendUser(user.id)
                    } else {
                        confirmDialog = ConfirmAction(
                            title = "Suspend User",
                            message = "Are you sure you want to suspend ${user.name}? They will lose access to the platform.",
                            confirmText = "Suspend",
                            confirmColor = Color.Red,
                            onConfirm = { onSuspendUser(user.id) }
                        )
                    }
                },
                onMakeAdmin = {
                    confirmDialog = ConfirmAction(
                        title = "Make Admin",
                        message = "Promote ${user.name} to Admin? They will have full platform control.",
                        confirmText = "Promote",
                        confirmColor = RivoBlue,
                        onConfirm = { onMakeAdmin(user.id) }
                    )
                },
                onMakeArtist = {
                    confirmDialog = ConfirmAction(
                        title = "Promote to Artist",
                        message = "Promote ${user.name} to Artist? They will be able to upload music.",
                        confirmText = "Promote",
                        confirmColor = RivoBlue,
                        onConfirm = { onMakeArtist(user.id) }
                    )
                },
                onFeatureArtist = { onFeatureArtist(user) },
                onDeleteUser = {
                    confirmDialog = ConfirmAction(
                        title = "Delete User",
                        message = "Permanently delete ${user.name}? This action cannot be undone.",
                        confirmText = "Delete",
                        confirmColor = Color.Red,
                        onConfirm = { onDeleteUser(user.id) }
                    )
                },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }
    }
}

// ── Data class for confirmation dialog state ──────────────────────────────────
private data class ConfirmAction(
    val title: String,
    val message: String,
    val confirmText: String,
    val confirmColor: Color,
    val onConfirm: () -> Unit
)

// ── Reusable confirmation dialog ──────────────────────────────────────────────
@Composable
private fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmText: String,
    confirmColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E1E2E),
            border = BorderStroke(1.dp, White.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium.copy(color = LightGray)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, White.copy(alpha = 0.12f))
                    ) {
                        Text("Cancel", color = LightGray)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
                    ) {
                        Text(confirmText, color = White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ── User card ─────────────────────────────────────────────────────────────────
@Composable
private fun AdminUserCard(
    user: User,
    onClick: () -> Unit,
    onSuspendUser: () -> Unit,
    onMakeAdmin: () -> Unit,
    onMakeArtist: () -> Unit,
    onFeatureArtist: () -> Unit,
    onDeleteUser: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (user.isSuspended) Color.Red.copy(alpha = 0.05f) else White.copy(alpha = 0.04f),
        border = BorderStroke(
            1.dp,
            if (user.isSuspended) Color.Red.copy(alpha = 0.15f) else White.copy(alpha = 0.06f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
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

                // Role indicator dot
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(
                            when (user.userType) {
                                UserType.ADMIN -> Color.Red
                                UserType.ARTIST -> RivoBlue
                                else -> SuccessGreen
                            }
                        )
                        .border(2.dp, DarkBackground, CircleShape)
                        .align(Alignment.BottomEnd)
                )
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
                    // Role badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (user.userType) {
                                    UserType.ADMIN -> Color.Red.copy(alpha = 0.1f)
                                    UserType.ARTIST -> RivoBlue.copy(alpha = 0.1f)
                                    else -> White.copy(alpha = 0.06f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            user.userType.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = when (user.userType) {
                                    UserType.ADMIN -> Color.Red
                                    UserType.ARTIST -> RivoBlue
                                    else -> LightGray
                                },
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    if (user.isSuspended) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Red.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "SUSPENDED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.Red,
                                    fontWeight = FontWeight.Black
                                )
                            )
                        }
                    }
                }
            }

            // Context menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, null, tint = LightGray)
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFF252535))
                ) {
                    // View Profile
                    DropdownMenuItem(
                        text = { Text("View Profile", color = White) },
                        leadingIcon = { Icon(Icons.Default.Visibility, null, tint = LightGray) },
                        onClick = { onClick(); showMenu = false }
                    )

                    HorizontalDivider(color = White.copy(alpha = 0.06f))

                    // Make Admin (only if not already admin)
                    if (user.userType != UserType.ADMIN) {
                        DropdownMenuItem(
                            text = { Text("Make Admin", color = White) },
                            leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null, tint = LightGray) },
                            onClick = { onMakeAdmin(); showMenu = false }
                        )
                    }

                    // Promote to Artist (only if not already artist or admin)
                    if (user.userType == UserType.LISTENER) {
                        DropdownMenuItem(
                            text = { Text("Promote to Artist", color = White) },
                            leadingIcon = { Icon(Icons.Default.Mic, null, tint = LightGray) },
                            onClick = { onMakeArtist(); showMenu = false }
                        )
                    }

                    // Feature Artist (only for artists)
                    if (user.userType == UserType.ARTIST) {
                        DropdownMenuItem(
                            text = { Text("Feature Artist", color = RivoBlue) },
                            leadingIcon = { Icon(Icons.Default.Star, null, tint = RivoBlue) },
                            onClick = { onFeatureArtist(); showMenu = false }
                        )
                    }

                    HorizontalDivider(color = White.copy(alpha = 0.06f))

                    // Suspend / Unsuspend
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (user.isSuspended) "Unsuspend User" else "Suspend User",
                                color = if (user.isSuspended) SuccessGreen else WarningYellow
                            )
                        },
                        leadingIcon = {
                            Icon(
                                if (user.isSuspended) Icons.Default.CheckCircle else Icons.Default.Block,
                                null,
                                tint = if (user.isSuspended) SuccessGreen else WarningYellow
                            )
                        },
                        onClick = { onSuspendUser(); showMenu = false }
                    )

                    // Delete User (red, destructive)
                    DropdownMenuItem(
                        text = { Text("Delete User", color = Color.Red) },
                        leadingIcon = { Icon(Icons.Default.DeleteForever, null, tint = Color.Red) },
                        onClick = { onDeleteUser(); showMenu = false }
                    )
                }
            }
        }
    }
}
