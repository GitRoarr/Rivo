package com.rivo.app.ui.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rivo.app.ui.components.RivoLogo

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGuestClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Rivo Logo
            RivoLogo(
                size = 180.dp,
                showText = true,
                animated = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Tagline is already in RivoLogo, but the WelcomeScreen had some extra text
            // "Ethiopian Music Streaming"
            Text(
                text = "Premium Music Experience",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Login button
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Login",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Register", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Continue as Guest",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .clickable { onGuestClick() }
                    .padding(8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
