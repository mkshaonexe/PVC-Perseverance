package com.perseverance.pvc.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PomodoroYellow,
    secondary = PomodoroGreen,
    tertiary = PomodoroGrey,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = PomodoroBlack,
    onSecondary = PomodoroBlack,
    onTertiary = PomodoroBlack,
    onBackground = LightGrey,
    onSurface = LightGrey,
    onSurfaceVariant = MediumGrey
)

private val LightColorScheme = lightColorScheme(
    primary = PomodoroYellow,
    secondary = PomodoroGreen,
    tertiary = PomodoroGrey,
    background = LightBackground, // Soft gradient background
    surface = GlassWhite, // Glass-like surface
    surfaceVariant = LightSurfaceVariant, // Slightly darker variant
    onPrimary = PomodoroBlack,
    onSecondary = PomodoroBlack,
    onTertiary = PomodoroBlack,
    onBackground = Color(0xFF1A1F36), // Darker text for better contrast
    onSurface = Color(0xFF1A1F36),
    onSurfaceVariant = Color(0xFF4A5568),
    outline = GlassBorder // Subtle borders
)

@Composable
fun PerseverancePVCTheme(
    darkTheme: Boolean = true, // Default to dark theme for new users
    content: @Composable () -> Unit
) {
    // Dynamic color disabled to ensure consistent reference light theme
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}