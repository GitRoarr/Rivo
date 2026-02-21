package com.rivo.app.ui.screens.admin

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
import com.rivo.app.data.model.User
import com.rivo.app.data.model.UserType
import com.rivo.app.ui.theme.Primary

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
    var showDropdownMenu by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search users...", color = Color.Gray) },
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
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Table header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Name",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "Email",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.weight(1.5f)
            )

            Text(
                text = "Role",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.weight(0.8f)
            )

            Text(
                text = "Status",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.weight(0.8f)
            )

            Text(
                text = "Actions",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.weight(0.5f)
            )
        }

        Divider(
            color = Color.DarkGray,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // User list
        LazyColumn {
            val filteredUsers = if (searchQuery.isBlank()) {
                users
            } else {
                users.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.email.contains(searchQuery, ignoreCase = true)
                }
            }

            items(filteredUsers) { user ->
                UserListItem(
                    user = user,
                    onClick = { onUserClick(user) },
                    onMenuClick = { showDropdownMenu = user.id },
                    showMenu = showDropdownMenu == user.id,
                    onDismissMenu = { showDropdownMenu = null },
                    onSuspendUser = { onSuspendUser(user.id) },
                    onMakeAdmin = { onMakeAdmin(user.id) },
                    onMakeArtist = { onMakeArtist(user.id) },
                    onFeatureArtist = { onFeatureArtist(user) }
                )

                Divider(color = Color.DarkGray.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun UserListItem(
    user: User,
    onClick: () -> Unit,
    onMenuClick: () -> Unit,
    showMenu: Boolean,
    onDismissMenu: () -> Unit,
    onSuspendUser: () -> Unit,
    onMakeAdmin: () -> Unit,
    onMakeArtist: () -> Unit,
    onFeatureArtist: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User avatar and name
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
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
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = user.name,
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Email
        Text(
            text = user.email,
            color = Color.White,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.5f)
        )

        // Role
        Text(
            text = when (user.userType) {
                UserType.ADMIN -> "admin"
                UserType.ARTIST -> "artist"
                UserType.LISTENER -> "listener"
                UserType.GUEST -> "guest"
            },
            color = when (user.userType) {
                UserType.ADMIN -> Color.Red
                UserType.ARTIST -> Primary
                UserType.LISTENER -> Color.Gray
                UserType.GUEST -> Color.LightGray
            },
            fontSize = 14.sp,
            modifier = Modifier.weight(0.8f)
        )

        // Status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(0.8f)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (user.isActive) Primary else Color.Red)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = if (user.isActive) "Active" else "Suspended",
                color = Color.White,
                fontSize = 14.sp
            )
        }

        // Actions
        Box(
            modifier = Modifier.weight(0.5f)
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
                    text = { Text("View Profile", color = Color.White) },
                    onClick = {
                        onClick()
                        onDismissMenu()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Edit User", color = Color.White) },
                    onClick = {
                        // Navigate to edit user screen
                        onDismissMenu()
                    }
                )

                if (user.userType != UserType.ADMIN) {
                    DropdownMenuItem(
                        text = { Text("Make Admin", color = Color.White) },
                        onClick = {
                            onMakeAdmin()
                            onDismissMenu()
                        }
                    )
                }

                if (user.userType != UserType.ARTIST) {
                    DropdownMenuItem(
                        text = { Text("Make Artist", color = Color.White) },
                        onClick = {
                            onMakeArtist()
                            onDismissMenu()
                        }
                    )
                }

                if (user.userType == UserType.ARTIST && user.isVerified) {
                    DropdownMenuItem(
                        text = { Text("Feature on Homepage", color = Color(0xFFFFD700)) },
                        onClick = {
                            onFeatureArtist()
                            onDismissMenu()
                        }
                    )
                }

                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (user.isActive) "Suspend User" else "Activate User",
                            color = if (user.isActive) Color.Red else Primary
                        )
                    },
                    onClick = {
                        onSuspendUser()
                        onDismissMenu()
                    }
                )
            }
        }
    }
}
