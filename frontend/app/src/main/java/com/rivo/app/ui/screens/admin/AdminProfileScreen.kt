package com.rivo.app.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rivo.app.data.model.User
import com.rivo.app.ui.theme.Primary
import com.rivo.app.ui.viewmodel.AuthViewModel
import com.rivo.app.utils.ImagePickerHelper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    onAdminPanelClick: () -> Unit,
    onManageFeaturedClick: () -> Unit,
    onEditProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit,
    onHelpAndSupportClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onLogoutClick: () -> Unit,
    currentUser: User? = null,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current

    var tempProfileImageUri by remember { mutableStateOf<Uri?>(null) }
    var isImageLoading by remember { mutableStateOf(false) }
    var localImagePath by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            tempProfileImageUri = it
            isImageLoading = true

            ImagePickerHelper.saveImageToInternalStorageAsync(
                context = context,
                uri = it,
                fileName = "profile_admin_${System.currentTimeMillis()}.jpg"
            ) { path ->
                isImageLoading = false
                if (path != null) {
                    localImagePath = path
                    // Update the profile in ViewModel/Database
                    authViewModel.updateProfileImage(context, Uri.parse("file://$path"))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Profile", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray.copy(alpha = 0.5f))
                        .border(1.dp, Color.Gray, CircleShape)
                        .clickable {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {

                    when {
                        tempProfileImageUri != null -> {
                            AsyncImage(
                                model = tempProfileImageUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        localImagePath != null -> {
                            AsyncImage(
                                model = File(localImagePath!!),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        currentUser?.profileImageUrl != null -> {
                            // Handle both URI strings and file paths
                            val imageModel = if (currentUser.profileImageUrl.startsWith("/") ||
                                currentUser.profileImageUrl.startsWith("file:")) {
                                try {
                                    File(currentUser.profileImageUrl.replace("file://", ""))
                                } catch (e: Exception) {
                                    currentUser.profileImageUrl
                                }
                            } else {
                                currentUser.profileImageUrl
                            }

                            AsyncImage(
                                model = imageModel,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Primary)
                            .clickable {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile Picture",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (isImageLoading) {
                        CircularProgressIndicator(
                            color = Primary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentUser?.fullName ?: "Admin User",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Administrator",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onAdminPanelClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Dashboard,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Admin Dashboard",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Manage Featured Content button
                Button(
                    onClick = onManageFeaturedClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EA)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Manage Featured Content",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Rest of the UI remains unchanged
            Spacer(modifier = Modifier.height(16.dp))

            // Menu items
            AdminMenuItem(
                icon = Icons.Default.Edit,
                title = "Edit Profile",
                onClick = onEditProfileClick
            )

            AdminMenuItem(
                icon = Icons.Default.Settings,
                title = "Settings",
                onClick = onSettingsClick
            )

            AdminMenuItem(
                icon = Icons.Default.Help,
                title = "Help & Support",
                onClick = onHelpAndSupportClick
            )

            AdminMenuItem(
                icon = Icons.Default.Info,
                title = "About",
                onClick = onAboutClick
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Logout button
            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Logout",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AdminMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }

        Divider(
            color = Color.DarkGray.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 56.dp)
        )
    }
}
