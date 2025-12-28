package com.perseverance.pvc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.luminance
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset

@Composable
fun TopHeader(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onHamburgerClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    showBackButton: Boolean = false,
    streak: Int = 0,
    title: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp)
    ) {
        // Center Title
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Left and Right Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hamburger menu (3 lines) in top left
            Column(
                modifier = Modifier.clickable { onHamburgerClick() }
            ) {
                // First line
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.onBackground)
                )
                Spacer(modifier = Modifier.height(3.dp))
                // Second line
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.onBackground)
                )
                Spacer(modifier = Modifier.height(3.dp))
                // Third line
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.onBackground)
                )
            }
            
            // Right side icons - either back button or streak
            if (showBackButton) {
                // Back button when on settings or insights page
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onBackClick() }
                )
            } else {
                // Right side icons: Insights + (Optional) Streak
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Insights Icon
                    IconButton(
                        onClick = onNavigateToInsights,
                        modifier = Modifier.size(32.dp) // Slightly smaller touch target to fit well
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Insights,
                            contentDescription = "Insights",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    if (streak > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Gamified Streak Indicator - Smaller Version
                        
                        // Animation for streak change "pop" effect
                        var isStreakChanged by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                        val scale by animateFloatAsState(
                            targetValue = if (isStreakChanged) 1.2f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            finishedListener = { if (isStreakChanged) isStreakChanged = false }
                        )

                        // Trigger animation when streak changes
                        androidx.compose.runtime.LaunchedEffect(streak) {
                            isStreakChanged = true
                            kotlinx.coroutines.delay(300) // Hold the scale up briefly
                            isStreakChanged = false
                        }

                        // Infinite pulsing animation for flame
                        val infiniteTransition = rememberInfiniteTransition(label = "flame")
                        val flameAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.7f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "flameAlpha"
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .scale(scale)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFE65100), // Dark Orange
                                            Color(0xFFFF9800)  // Light Orange
                                        )
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFFFD700).copy(alpha = 0.5f), // Gold border
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp) // Reduced padding
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = Color.White.copy(alpha = flameAlpha),
                                modifier = Modifier.size(16.dp) // Reduced size
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$streak",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp, // Reduced font size
                                style = MaterialTheme.typography.titleMedium.copy(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.3f),
                                        offset = Offset(1f, 1f),
                                        blurRadius = 2f
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
