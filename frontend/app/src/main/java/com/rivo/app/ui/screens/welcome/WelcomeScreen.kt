package com.rivo.app.ui.screens.welcome

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rivo.app.ui.components.RivoLogo
import com.rivo.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGuestClick: () -> Unit
) {
    // Staggered entrance animations
    var logoVisible by remember { mutableStateOf(false) }
    var subtitleVisible by remember { mutableStateOf(false) }
    var buttonsVisible by remember { mutableStateOf(false) }
    var guestVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        logoVisible = true
        delay(400)
        subtitleVisible = true
        delay(300)
        buttonsVisible = true
        delay(200)
        guestVisible = true
    }

    // Ambient background animations
    val infiniteTransition = rememberInfiniteTransition(label = "welcome_bg")

    val orbAngle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)),
        label = "orb_angle"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            tween(3000, easing = EaseInOutCubic), RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            tween(2500, easing = EaseInOutSine), RepeatMode.Reverse
        ),
        label = "glow"
    )

    val shimmer by infiniteTransition.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(
            tween(3000, easing = LinearEasing)
        ),
        label = "shimmer"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // ── Animated background orbs ──────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val radians = orbAngle * (PI / 180f).toFloat()

            // Purple orb — orbiting top-left area
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        RivoPurple.copy(alpha = glowAlpha),
                        RivoPurple.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = Offset(
                        w * 0.25f + cos(radians) * w * 0.12f,
                        h * 0.18f + sin(radians) * h * 0.06f
                    ),
                    radius = w * 0.55f * pulseScale
                )
            )

            // Pink orb — orbiting bottom-right area
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        RivoPink.copy(alpha = glowAlpha * 0.7f),
                        RivoPink.copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    center = Offset(
                        w * 0.8f + cos(radians + PI.toFloat()) * w * 0.1f,
                        h * 0.75f + sin(radians + PI.toFloat()) * h * 0.08f
                    ),
                    radius = w * 0.5f * pulseScale
                )
            )

            // Blue orb — subtle accent near center
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        RivoBlue.copy(alpha = glowAlpha * 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(
                        w * 0.5f + sin(radians * 0.7f) * w * 0.15f,
                        h * 0.5f + cos(radians * 0.7f) * h * 0.1f
                    ),
                    radius = w * 0.35f
                )
            )

            // Floating particles
            for (i in 0..7) {
                val particleAngle = radians * (0.3f + i * 0.15f) + i * 45f * (PI / 180f).toFloat()
                val px = w * (0.15f + i * 0.1f) + cos(particleAngle) * w * 0.05f
                val py = h * (0.1f + i * 0.1f) + sin(particleAngle) * h * 0.03f
                val particleAlpha = ((sin(radians * 2f + i.toFloat()) + 1f) / 2f) * 0.4f

                drawCircle(
                    color = if (i % 2 == 0) RivoPurple.copy(alpha = particleAlpha)
                    else RivoPink.copy(alpha = particleAlpha),
                    radius = (2f + i % 3) * 1.5f,
                    center = Offset(px, py)
                )
            }
        }

        // ── Main content ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.15f))

            // ── Animated Logo ─────────────────────────────────────────────────
            AnimatedVisibility(
                visible = logoVisible,
                enter = scaleIn(
                    initialScale = 0.5f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(animationSpec = tween(800))
            ) {
                RivoLogo(
                    size = 180.dp,
                    showText = false,
                    animated = true
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── App Name with gradient ────────────────────────────────────────
            AnimatedVisibility(
                visible = logoVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(
                        dampingRatio = 0.7f,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(tween(600))
            ) {
                Text(
                    text = "Rivo",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp,
                        brush = Brush.linearGradient(
                            colors = listOf(White, RivoPurple.copy(alpha = 0.9f), RivoPink),
                            start = Offset(shimmer * 200f, 0f),
                            end = Offset(shimmer * 200f + 300f, 100f)
                        ),
                        shadow = Shadow(
                            color = RivoPurple.copy(alpha = 0.4f),
                            offset = Offset(0f, 6f),
                            blurRadius = 20f
                        )
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Tagline ───────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = subtitleVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(600, easing = EaseOutCubic)
                ) + fadeIn(tween(600))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FEEL THE RHYTHM",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = RivoPurple.copy(alpha = 0.8f),
                            letterSpacing = 8.sp,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Premium Music Experience",
                        fontSize = 15.sp,
                        color = LightGray.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.2f))

            // ── Buttons ───────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = buttonsVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(700, easing = EaseOutBack)
                ) + fadeIn(tween(500))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // ── Login Button (gradient filled) ────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(RivoPurple, RivoPink)
                                )
                            )
                            .clickable(onClick = onLoginClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Get Started",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = White,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = White.copy(alpha = 0.9f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Register Button (glass-morphic) ───────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(White.copy(alpha = 0.06f))
                            .border(
                                width = 1.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        RivoPurple.copy(alpha = 0.4f),
                                        RivoPink.copy(alpha = 0.4f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable(onClick = onRegisterClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Create Account",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = White.copy(alpha = 0.9f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Guest Link ────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = guestVisible,
                enter = fadeIn(tween(800))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onGuestClick)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Continue as Guest",
                        fontSize = 14.sp,
                        color = LightGray.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = LightGray.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // ── Bottom branding ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = guestVisible,
                enter = fadeIn(tween(1000, delayMillis = 200))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    // Decorative gradient divider
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        RivoPurple.copy(alpha = 0.5f),
                                        RivoPink.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Made with ♪ in Ethiopia",
                        fontSize = 11.sp,
                        color = DarkGray.copy(alpha = 0.8f),
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
