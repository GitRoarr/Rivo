package com.rivo.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.rivo.app.ui.theme.RivoGradient
import com.rivo.app.ui.theme.RivoCyan
import com.rivo.app.ui.theme.RivoBlue

@Composable
fun RivoLogo(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    showText: Boolean = true,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoAnimation")
    
    // Wave animation
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    // Gradient rotation animation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradientRotation"
    )

    // Floating animation
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )

    Column(
        modifier = modifier.offset(y = if (animated) floatOffset.dp else 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(size / 8)) {
                val canvasWidth = this.size.width
                val canvasHeight = this.size.height
                val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
                val centerY = canvasHeight / 2f

                // 1. Draw Background Glow
                if (animated) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(RivoBlue.copy(alpha = 0.2f), Color.Transparent),
                            center = center,
                            radius = canvasWidth * 0.8f
                        )
                    )
                }

                // 2. Draw the Stylized Play Button (Double layer)
                val trianglePath = Path().apply {
                    val startX = canvasWidth * 0.15f
                    val topY = canvasHeight * 0.15f
                    val bottomY = canvasHeight * 0.85f
                    val tipX = canvasWidth * 0.75f

                    moveTo(startX, topY)
                    lineTo(startX, bottomY)
                    lineTo(tipX, centerY)
                    close()
                }

                // Inner cutout look
                val innerCutoutPath = Path().apply {
                    val startX = canvasWidth * 0.35f
                    val topY = canvasHeight * 0.45f
                    val bottomY = canvasHeight * 0.55f
                    val tipX = canvasWidth * 0.5f

                    moveTo(startX, topY)
                    lineTo(startX, bottomY)
                    lineTo(tipX, centerY)
                    close()
                }

                rotate(degrees = rotation, pivot = center) {
                    val mainBrush = Brush.sweepGradient(
                        colors = RivoGradient + RivoGradient.first(),
                        center = center
                    )
                    
                    // Main Triangle
                    drawPath(
                        path = trianglePath,
                        brush = mainBrush
                    )
                }

                // Overlay the cutout to give depth
                drawPath(
                    path = innerCutoutPath,
                    color = Color.Black.copy(alpha = 0.4f),
                    blendMode = BlendMode.SrcOver
                )

                // 3. Animated Sound Waves
                val waveBrush = Brush.linearGradient(
                    colors = listOf(RivoCyan, RivoBlue),
                    start = Offset(0f, 0f),
                    end = Offset(canvasWidth, canvasHeight)
                )

                for (i in 0..1) {
                    val progress = (waveOffset + i * 0.5f) % 1f
                    val waveAlpha = (1f - progress).coerceIn(0f, 1f)
                    val waveRadius = canvasWidth * (0.3f + progress * 0.2f)
                    
                    drawArc(
                        brush = waveBrush,
                        startAngle = -50f,
                        sweepAngle = 100f,
                        useCenter = false,
                        topLeft = Offset(canvasWidth * 0.45f, centerY - waveRadius),
                        size = Size(waveRadius * 2, waveRadius * 2),
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
                        alpha = waveAlpha
                    )
                }
            }
        }

        if (showText) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Rivo",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp,
                    shadow = Shadow(
                        color = RivoBlue.copy(alpha = 0.5f),
                        offset = Offset(0f, 4f),
                        blurRadius = 8f
                    )
                )
            )
            Text(
                text = "FEEL THE RHYTHM",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 6.sp
                )
            )
        }
    }
}
