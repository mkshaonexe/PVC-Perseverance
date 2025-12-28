package com.perseverance.pvc.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Utility functions for responsive text sizing based on screen size and density
 */

@Composable
fun getResponsiveFontSize(
    small: Float,
    medium: Float,
    large: Float
): Float {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val density = LocalDensity.current.density
    
    return when {
        screenWidth < 360 -> small // Small phones
        screenWidth < 600 -> medium // Regular phones
        else -> large // Large phones, tablets, foldables
    }
}

@Composable
fun getResponsiveSpacing(
    small: Dp,
    medium: Dp,
    large: Dp
): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    
    return when {
        screenWidth < 360 -> small
        screenWidth < 600 -> medium
        else -> large
    }
}

@Composable
fun getResponsivePadding(
    small: Dp,
    medium: Dp,
    large: Dp
): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    
    return when {
        screenWidth < 360 -> small
        screenWidth < 600 -> medium
        else -> large
    }
}

// Predefined responsive text sizes for common use cases
object ResponsiveTextSizes {
    @Composable
    fun timerDisplay(): Float = getResponsiveFontSize(36f, 48f, 56f)
    
    @Composable
    fun totalStudyTime(): Float = getResponsiveFontSize(24f, 32f, 40f)
    
    @Composable
    fun buttonText(): Float = getResponsiveFontSize(12f, 14f, 16f)
    
    @Composable
    fun labelText(): Float = getResponsiveFontSize(10f, 12f, 14f)
    
    @Composable
    fun subjectText(): Float = getResponsiveFontSize(14f, 16f, 18f)
    
    @Composable
    fun dialogTitle(): Float = getResponsiveFontSize(16f, 20f, 24f)
    
    @Composable
    fun dialogText(): Float = getResponsiveFontSize(14f, 16f, 18f)
}

// Predefined responsive spacing
object ResponsiveSpacing {
    @Composable
    fun small(): Dp = getResponsiveSpacing(4.dp, 8.dp, 12.dp)
    
    @Composable
    fun medium(): Dp = getResponsiveSpacing(8.dp, 16.dp, 24.dp)
    
    @Composable
    fun large(): Dp = getResponsiveSpacing(16.dp, 24.dp, 32.dp)
    
    @Composable
    fun extraLarge(): Dp = getResponsiveSpacing(24.dp, 40.dp, 60.dp)
}

// Predefined responsive padding
object ResponsivePadding {
    @Composable
    fun screen(): Dp = getResponsivePadding(16.dp, 24.dp, 32.dp)
    
    @Composable
    fun button(): Dp = getResponsivePadding(12.dp, 16.dp, 20.dp)
    
    @Composable
    fun dialog(): Dp = getResponsivePadding(16.dp, 24.dp, 32.dp)
}
