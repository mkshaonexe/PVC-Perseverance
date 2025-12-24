package com.perseverance.pvc.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", Icons.Filled.Dashboard)
    object Home : Screen("home", Icons.Filled.Home)
    object Group : Screen("group", Icons.Filled.Group)
    object Settings : Screen("settings", Icons.Filled.Settings)
    object Insights : Screen("insights", Icons.Filled.Insights)
    object Menu : Screen("menu", Icons.Filled.Menu)
    object Developer : Screen("developer", Icons.Filled.Build)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Home,
    Screen.Group,
    Screen.Settings
)

