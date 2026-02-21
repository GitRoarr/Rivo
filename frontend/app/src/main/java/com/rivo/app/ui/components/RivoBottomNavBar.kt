package com.rivo.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rivo.app.R
import com.rivo.app.data.model.UserType
import com.rivo.app.ui.navigation.RivoScreens
import com.rivo.app.ui.theme.*
import kotlinx.coroutines.launch

data class BottomNavItem(
    val screen: RivoScreens,
    val icon: Int,
    val label: String
)

@Composable
fun RivoBottomNavBar(
    navController: NavController,
    userType: UserType,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(RivoScreens.Home, R.drawable.ic_home, "Home"),
        BottomNavItem(RivoScreens.Explore, R.drawable.ic_artist, "Explore"),
        BottomNavItem(RivoScreens.Search, R.drawable.ic_search, "Search"),
        BottomNavItem(RivoScreens.Library, R.drawable.ic_library, "Library"),
        BottomNavItem(RivoScreens.Profile, R.drawable.ic_profile, "Profile")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = Color(0xFF1E1E1E).copy(alpha = 0.85f),
            shape = RoundedCornerShape(32.dp),
            tonalElevation = 8.dp,
            shadowElevation = 20.dp,
            modifier = Modifier
                .height(72.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.screen.name } == true
                    
                    val iconSize by animateDpAsState(
                        targetValue = if (selected) 28.dp else 24.dp,
                        animationSpec = spring(dampingRatio = 0.5f, stiffness = 200f)
                    )
                    
                    val color by animateColorAsState(
                        targetValue = if (selected) RivoPink else Color.Gray,
                        animationSpec = spring()
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                val isGuest = userType == UserType.GUEST
                                val isRestricted = item.screen == RivoScreens.Library || item.screen == RivoScreens.Profile

                                if (!selected) {
                                    if (isGuest && isRestricted) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                "Register to access ${item.label}"
                                            )
                                        }
                                        navController.navigate(RivoScreens.Register.name)
                                    } else {
                                        navController.navigate(item.screen.name) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) RivoPurple.copy(alpha = 0.15f) else Color.Transparent
                                )
                        ) {
                            Icon(
                                painter = painterResource(id = item.icon),
                                contentDescription = item.label,
                                tint = color,
                                modifier = Modifier.size(iconSize)
                            )
                        }
                        
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.horizontalGradient(BrandGradient)
                                    )
                            )
                        } else {
                            Text(
                                text = item.label,
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(bottom = 80.dp)
        )
    }
}
