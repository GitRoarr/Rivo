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
    onFeatureArtist: (User) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users
        else users.filter { 
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

        items(filteredUsers, key = { it.id }) { user ->
            AdminUserCard(
                user = user,
                onClick = { onUserClick(user) },
                onSuspendUser = { onSuspendUser(user.id) },
                onMakeAdmin = { onMakeAdmin(user.id) },
                onMakeArtist = { onMakeArtist(user.id) },
                onFeatureArtist = { onFeatureArtist(user) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun AdminUserCard(
    user: User,
    onClick: () -> Unit,
    onSuspendUser: () -> Unit,
    onMakeAdmin: () -> Unit,
    onMakeArtist: () -> Unit,
    onFeatureArtist: () -> Unit,
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

                // Role indicator
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
                        Text(
                            "SUSPENDED",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Red, fontWeight = FontWeight.Black)
                        )
                    }
                }
            }

            // Quick Actions or Menu
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, null, tint = LightGray)
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFF252525))
                ) {
                    DropdownMenuItem(
                        text = { Text("View Profile", color = White) },
                        leadingIcon = { Icon(Icons.Default.Visibility, null, tint = LightGray) },
                        onClick = { onClick(); showMenu = false }
                    )
                    
                    if (user.userType != UserType.ADMIN) {
                        DropdownMenuItem(
                            text = { Text("Make Admin", color = White) },
                            leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null, tint = LightGray) },
                            onClick = { onMakeAdmin(); showMenu = false }
                        )
                    }
                    
                    if (user.userType != UserType.ARTIST) {
                        DropdownMenuItem(
                            text = { Text("Promote to Artist", color = White) },
                            leadingIcon = { Icon(Icons.Default.Mic, null, tint = LightGray) },
                            onClick = { onMakeArtist(); showMenu = false }
                        )
                    }

                    if (user.userType == UserType.ARTIST) {
                        DropdownMenuItem(
                            text = { Text("Feature Artist", color = RivoBlue) },
                            leadingIcon = { Icon(Icons.Default.Star, null, tint = RivoBlue) },
                            onClick = { onFeatureArtist(); showMenu = false }
                        )
                    }

                    Divider(color = White.copy(alpha = 0.05f))

                    DropdownMenuItem(
                        text = { 
                            Text(
                                if (user.isSuspended) "Unsuspend User" else "Suspend User", 
                                color = if (user.isSuspended) SuccessGreen else Color.Red 
                            ) 
                        },
                        leadingIcon = { 
                            Icon(
                                if (user.isSuspended) Icons.Default.CheckCircle else Icons.Default.Block, 
                                null, 
                                tint = if (user.isSuspended) SuccessGreen else Color.Red
                            ) 
                        },
                        onClick = { onSuspendUser(); showMenu = false }
                    )
                }
            }
        }
    }
}
