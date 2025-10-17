package com.perseverance.pvc.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = PomodoroYellow,
    secondary = PomodoroGreen,
    tertiary = PomodoroGrey,
    background = PomodoroWhite,
    surface = PomodoroWhite,
    onPrimary = PomodoroBlack,
    onSecondary = PomodoroBlack,
    onTertiary = PomodoroBlack,
    onBackground = PomodoroBlack,
    onSurface = PomodoroBlack
)

@Composable
fun PerseverancePVCTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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