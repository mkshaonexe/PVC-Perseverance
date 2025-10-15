package com.perseverance.pvc.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring_animation")
    
    // Animate the rings when playing
    val ring1Rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1"
    )
    
    val ring2Rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2"
    )
    
    val ring3Rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring3"
    )
    
    // Scale animation when clicked
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    // Color transition
    val buttonColor = if (isPlaying) Color(0xFF4CAF50) else Color(0xFF5C6BC0)
    val animatedColor by animateColorAsState(
        targetValue = buttonColor,
        animationSpec = tween(300),
        label = "color"
    )
    
    val coroutineScope = rememberCoroutineScope()
    
    Box(
        modifier = modifier
            .size(200.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
                // Reset pressed state after a delay
                coroutineScope.launch {
                    delay(150)
                    isPressed = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Draw animated rings
        Canvas(modifier = Modifier.size(200.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            
            if (isPlaying) {
                // Outer ring (dashed)
                drawCircle(
                    color = animatedColor.copy(alpha = 0.3f),
                    radius = 90.dp.toPx() * scale,
                    center = center,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(10f, 10f),
                            ring1Rotation
                        )
                    )
                )
                
                // Middle ring (dashed)
                drawCircle(
                    color = animatedColor.copy(alpha = 0.5f),
                    radius = 70.dp.toPx() * scale,
                    center = center,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(8f, 12f),
                            ring2Rotation
                        )
                    )
                )
                
                // Inner ring (dashed)
                drawCircle(
                    color = animatedColor.copy(alpha = 0.7f),
                    radius = 50.dp.toPx() * scale,
                    center = center,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(6f, 8f),
                            ring3Rotation
                        )
                    )
                )
            } else {
                // Static rings when paused
                drawCircle(
                    color = animatedColor.copy(alpha = 0.2f),
                    radius = 90.dp.toPx() * scale,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
                
                drawCircle(
                    color = animatedColor.copy(alpha = 0.3f),
                    radius = 70.dp.toPx() * scale,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
                
                drawCircle(
                    color = animatedColor.copy(alpha = 0.4f),
                    radius = 50.dp.toPx() * scale,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            
            // Center button circle
            drawCircle(
                color = animatedColor,
                radius = 32.dp.toPx() * scale,
                center = center
            )
        }
        
        // Play/Pause icon
        Text(
            text = if (isPlaying) "⏸" else "▶",
            fontSize = 32.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

