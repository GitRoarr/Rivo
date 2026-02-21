package com.rivo.app.ui.screens.listener

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rivo.app.data.model.User
import com.rivo.app.data.model.UserType
import com.rivo.app.ui.theme.*
import com.rivo.app.ui.viewmodel.AuthViewModel
import com.rivo.app.ui.viewmodel.FollowViewModel
import com.rivo.app.utils.ImagePickerHelper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListenerProfileScreen(
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit,
    authViewModel: AuthViewModel,
    followViewModel: FollowViewModel,
    currentUser: User? = null
) {
    val context = LocalContext.current
    val user = currentUser ?: authViewModel.currentUser.collectAsState().value
    
    val followersCount by followViewModel.getFollowersCount.collectAsState(initial = 0)
    val followingCount by followViewModel.getFollowingCount.collectAsState(initial = 0)
    
    val scrollState = rememberScrollState()
    
    // Image handling
    val profilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { authViewModel.updateProfileImage(context, it) }
    }

    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { authViewModel.updateCoverImage(context, it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // 1. Cover Image with Parallax and Blur
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .graphicsLayer {
                    alpha = 1f - (scrollState.value.toFloat() / 1000f).coerceIn(0f, 1f)
                    translationY = -scrollState.value.toFloat() * 0.5f
                }
        ) {
            val coverImage = user?.coverImageUrl
            if (coverImage != null && coverImage.isNotEmpty()) {
                AsyncImage(
                    model = if (coverImage.startsWith("/")) File(coverImage) else coverImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(RivoPurple, RivoPink)
                            )
                        )
                )
            }
            
            // Dark overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, DarkBackground)
                        )
                    )
            )
            
            // Edit Cover Button
            IconButton(
                onClick = { coverPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Edit Cover", tint = White)
            }
        }

        // 2. Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(200.dp))
            
            // Profile Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image with Ring
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(RivoGradient))
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(DarkBackground)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(DarkSurface)
                                .clickable {
                                    profilePickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val profileImage = user?.profileImageUrl
                            if (profileImage != null && profileImage.isNotEmpty()) {
                                AsyncImage(
                                    model = if (profileImage.startsWith("/")) File(profileImage) else profileImage,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = LightGray,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }
                    }
                    
                    // Edit Profile Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-8).dp, y = (-8).dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(RivoPink)
                            .border(3.dp, DarkBackground, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = White, modifier = Modifier.size(16.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Name and Verification
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user?.fullName ?: "Rivo Listener",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = White
                        )
                    )
                    if (user?.isVerified == true) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Filled.Verified,
                            contentDescription = "Verified",
                            tint = RivoBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Text(
                    text = "@${user?.name ?: "listener"}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = LightGray,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))
                
                // Bio
                if (!user?.bio.isNullOrEmpty()) {
                    Text(
                        text = user?.bio!!,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = White.copy(alpha = 0.8f),
                            lineHeight = 20.sp
                        ),
                        modifier = Modifier.padding(horizontal = 24.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Stats Card (Glassmorphism)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatColumn(count = followersCount.toString(), label = "Followers")
                        Box(modifier = Modifier.height(40.dp).width(1.dp).background(Color.White.copy(alpha = 0.1f)))
                        ProfileStatColumn(count = followingCount.toString(), label = "Following")
                        Box(modifier = Modifier.height(40.dp).width(1.dp).background(Color.White.copy(alpha = 0.1f)))
                        ProfileStatColumn(count = "1.2k", label = "Plays") // Placeholder for now
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Location and Website
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (!user?.location.isNullOrEmpty()) {
                        IconText(icon = Icons.Default.LocationOn, text = user?.location!!)
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    if (!user?.website.isNullOrEmpty()) {
                        IconText(icon = Icons.Default.Link, text = user?.website!!)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Menu Section
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        DarkSurface,
                        RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
                    .padding(24.dp)
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                ModernMenuItem(
                    icon = Icons.Outlined.Person,
                    title = "Edit Profile",
                    subtitle = "Update your info and images",
                    onClick = onEditProfileClick
                )
                
                ModernMenuItem(
                    icon = Icons.Outlined.Settings,
                    title = "Preferences",
                    subtitle = "App appearance and behavior",
                    onClick = onSettingsClick
                )
                
                ModernMenuItem(
                    icon = Icons.Outlined.CloudUpload,
                    title = "Cloud Sync",
                    subtitle = "Backup your library and data",
                    onClick = {}
                )
                
                ModernMenuItem(
                    icon = Icons.Outlined.HelpOutline,
                    title = "Support",
                    subtitle = "Get help and report bugs",
                    onClick = onHelpClick
                )

                Spacer(modifier = Modifier.height(32.dp))
                
                // Logout Button
                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = ErrorRed)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Logout Account",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = ErrorRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
        
        // Top Bar (Glassmorphism)
        TopBar(
            onBackClick = onBackClick,
            scrollState = scrollState,
            title = user?.fullName ?: "Profile"
        )
    }
}

@Composable
fun ProfileStatColumn(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.headlineSmall.copy(
                color = White,
                fontWeight = FontWeight.Black
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = LightGray
            )
        )
    }
}

@Composable
fun IconText(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = RivoPink, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodySmall.copy(color = LightGray))
    }
}

@Composable
fun ModernMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = White, modifier = Modifier.size(24.dp))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = White,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = LightGray
                )
            )
        }
        
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = LightGray)
    }
}

@Composable
fun TopBar(onBackClick: () -> Unit, scrollState: ScrollState, title: String) {
    val alpha = (scrollState.value.toFloat() / 500f).coerceIn(0f, 1f)
    
    Surface(
        color = DarkBackground.copy(alpha = alpha),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .graphicsLayer {
                // Subtle blur if supported or just color change
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .alpha(alpha)
            )
        }
    }
}