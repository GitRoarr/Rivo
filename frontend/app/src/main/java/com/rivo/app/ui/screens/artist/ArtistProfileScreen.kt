package com.rivo.app.ui.screens.artist

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rivo.app.data.model.User
import com.rivo.app.ui.theme.*
import com.rivo.app.ui.viewmodel.ArtistViewModel
import com.rivo.app.ui.viewmodel.AuthViewModel
import com.rivo.app.ui.viewmodel.FollowViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistProfileScreen(
    onBackClick: () -> Unit,
    onDashboardClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onGetVerifiedClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit,
    artistViewModel: ArtistViewModel,
    authViewModel: AuthViewModel,
    followViewModel: FollowViewModel,
    currentUser: User? = null
) {
    val context = LocalContext.current
    val artistAnalytics by artistViewModel.artistAnalytics.collectAsState()
    val user = currentUser ?: authViewModel.currentUser.collectAsState().value

    val artistMusic by artistViewModel.artistMusic.collectAsState()
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
        // 1. Artist Banner (Cover Image)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .graphicsLayer {
                    alpha = 1f - (scrollState.value.toFloat() / 1200f).coerceIn(0f, 1f)
                    translationY = -scrollState.value.toFloat() * 0.4f
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
                                listOf(RivoPurple, RivoBlue)
                            )
                        )
                )
            }
            
            // Premium gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f),
                                DarkBackground
                            )
                        )
                    )
            )

            // Edit Banner Button
            IconButton(
                onClick = { coverPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = "Edit Banner", tint = White)
            }
        }

        // 2. Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(240.dp))
            
            // Artist Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image with Verified Border
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(FullBrandGradient))
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
                                    modifier = Modifier.size(70.dp)
                                )
                            }
                        }
                    }
                    
                    // Edit Overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-10).dp, y = (-10).dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Primary)
                            .border(3.dp, DarkBackground, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = White, modifier = Modifier.size(18.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Artist Name and Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user?.fullName ?: "Rivo Artist",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = White,
                            letterSpacing = (-1).sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Filled.Verified,
                        contentDescription = "Verified Artist",
                        tint = RivoBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Text(
                    text = "${followersCount} Monthly Listeners",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = LightGray,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))
                
                // Dashboard Action Button
                Button(
                    onClick = onDashboardClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Icon(Icons.Default.SpaceDashboard, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Artist Dashboard",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                // Stats Row
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.03f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ArtistStatItem(count = artistMusic.size.toString(), label = "Songs")
                        ArtistDivider(Modifier.align(Alignment.CenterVertically))
                        ArtistStatItem(count = artistAnalytics?.totalPlays?.toString() ?: "0", label = "Plays")
                        ArtistDivider(Modifier.align(Alignment.CenterVertically))
                        ArtistStatItem(count = followersCount.toString(), label = "Followers")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // About & Social Section
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        DarkSurface,
                        RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                    )
                    .padding(24.dp)
            ) {
                Text(
                    text = "Profile Settings",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                
                ArtistMenuItem(
                    icon = Icons.Default.EditNote,
                    title = "Edit Bio & Details",
                    onClick = onEditProfileClick
                )
                
                ArtistMenuItem(
                    icon = Icons.Default.VerifiedUser,
                    title = "Verification Status",
                    onClick = onGetVerifiedClick
                )

                ArtistMenuItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "Earnings & Payouts",
                    onClick = {}
                )
                
                ArtistMenuItem(
                    icon = Icons.Default.Settings,
                    title = "Settings",
                    onClick = onSettingsClick
                )
                
                ArtistMenuItem(
                    icon = Icons.Default.Info,
                    title = "About Rivo",
                    onClick = onAboutClick
                )

                Spacer(modifier = Modifier.height(40.dp))
                
                // Logout Button
                OutlinedButton(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Sign Out",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
        
        // Custom Sticky Header
        ArtistTopBar(onBackClick, scrollState, user?.fullName ?: "Artist")
    }
}

@Composable
fun ArtistStatItem(count: String, label: String) {
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
            style = MaterialTheme.typography.labelLarge.copy(
                color = LightGray
            )
        )
    }
}

@Composable
private fun ArtistDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(40.dp)
            .width(1.dp)
            .background(Color.White.copy(alpha = 0.1f))
    )
}

@Composable
fun ArtistMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = White, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = White,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = LightGray)
        }
    }
}

@Composable
fun ArtistTopBar(onBackClick: () -> Unit, scrollState: ScrollState, title: String) {
    val alpha = (scrollState.value.toFloat() / 600f).coerceIn(0f, 1f)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(DarkBackground.copy(alpha = alpha))
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
