package com.rivo.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rivo.app.ui.components.RivoTopBar
import com.rivo.app.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            RivoTopBar(
                title = "About Rivo",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo Placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFF111111), shape = RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎵",
                    fontSize = 48.sp,
                    color = Primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Name
            Text(
                text = "Rivo",
                color = Primary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // Version
            Text(
                text = "Version 1.0.0",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // About Text
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF111111)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "About",
                        color = Primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Rivo is a platform dedicated to helping artists grow their fanbase and share their music with the world. Our mission is to connect musicians with listeners globally while providing tools for artists to manage their careers.",
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Features
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF111111)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Key Features",
                        color = Primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    FeatureItem(
                        title = "Music Streaming",
                        description = "Stream high-quality music anytime, anywhere"
                    )

                    FeatureItem(
                        title = "Artist Dashboard",
                        description = "Tools for artists to manage their music and view analytics"
                    )

                    FeatureItem(
                        title = "Offline Listening",
                        description = "Download music to listen without an internet connection"
                    )

                    FeatureItem(
                        title = "Artist Verification",
                        description = "Get verified to build trust with your audience"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Team
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF111111)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Our Team",
                        color = Primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Rivo was created by a team of passionate music lovers and tech enthusiasts. Our goal is to showcase musical talent to the world.",
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contact
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF111111)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Contact",
                        color = Primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Email: info@rivo.com",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Text(
                        text = "Website: www.rivo.com",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Text(
                        text = "Address: Global Office",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Copyright
            Text(
                text = "© 2023 Rivo. All rights reserved.",
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FeatureItem(
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Bullet point
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Primary, shape = CircleShape)
                .align(Alignment.Top)
                .padding(top = 6.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = description,
                color = Color.Gray,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
        }
    }
}
