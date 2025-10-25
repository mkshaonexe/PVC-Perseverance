package com.perseverance.pvc.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

/**
 * Creates a glassmorphism effect for cards in light theme
 * Returns appropriate elevation for the current theme
 */
@Composable
fun glassElevation(isLightTheme: Boolean): CardElevation {
    return CardDefaults.cardElevation(
        defaultElevation = if (isLightTheme) 4.dp else 0.dp,
        pressedElevation = if (isLightTheme) 2.dp else 0.dp,
        focusedElevation = if (isLightTheme) 6.dp else 0.dp,
        hoveredElevation = if (isLightTheme) 6.dp else 0.dp
    )
}

/**
 * Creates a subtle border for glassmorphism effect in light theme
 * Returns null for dark theme to maintain original appearance
 */
@Composable
fun glassBorder(isLightTheme: Boolean): BorderStroke? {
    return if (isLightTheme) {
        BorderStroke(
            width = 1.dp,
            color = Color(0xFFE8ECEF).copy(alpha = 0.6f)
        )
    } else {
        null
    }
}

/**
 * Standard rounded corner shape for glass cards
 */
val glassCornerRadius = 16.dp

/**
 * Creates a glassmorphism card shape
 */
fun glassShape(cornerRadius: Dp = glassCornerRadius) = RoundedCornerShape(cornerRadius)

/**
 * Extension function to check if current theme is light
 */
@Composable
fun isLightTheme(): Boolean {
    return MaterialTheme.colorScheme.background.luminance() >= 0.5f
}

