package com.perseverance.pvc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.perseverance.pvc.ui.components.TopHeader

@Composable
fun SettingsScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {}
) {
    var darkMode by remember { mutableStateOf("Dark") }
    var useTimerInBackground by remember { mutableStateOf(true) }
    var resetSessionEveryDay by remember { mutableStateOf(false) }
    var hideNavigationBar by remember { mutableStateOf(false) }
    var hideStatusBarDuringFocus by remember { mutableStateOf(true) }
    var followSystemFontSettings by remember { mutableStateOf(false) }
    var dayStartTime by remember { mutableStateOf("12:00 AM") }
    var language by remember { mutableStateOf("English") }
    var useDoNotDisturbDuringFocus by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Video background
        com.perseverance.pvc.ui.components.VideoBackground(
            isPlaying = false,
            modifier = Modifier.fillMaxSize()
        )
        
        // Semi-transparent overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Top header with hamburger menu and settings/insights icons
            TopHeader(
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToInsights = onNavigateToInsights,
                onHamburgerClick = { /* Handle hamburger menu click */ }
            )
            
            // Main content with scroll
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Settings title
                Text(
                    text = "App Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

            // Settings Items
            Column(
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                // General App Settings Section
                SettingsSection(
                    title = "General App Settings",
                    items = {
                        SettingsItem(
                            icon = Icons.Filled.Security,
                            title = "Manage Permissions",
                            action = { /* Navigate to permissions */ }
                        )

                        SettingsDropdownItem(
                            icon = Icons.Filled.DarkMode,
                            title = "Dark Mode",
                            value = darkMode,
                            onValueChange = { darkMode = it }
                        )

                        SettingsItem(
                            icon = Icons.Filled.Palette,
                            title = "Theme Settings",
                            action = { /* Navigate to theme settings */ }
                        )
                    }
                )

                // Timer & Display Section
                SettingsSection(
                    title = "Timer & Display",
                    items = {
                        SettingsToggleItem(
                            icon = Icons.Filled.Timer,
                            title = "Use Timer in Background",
                            checked = useTimerInBackground,
                            onCheckedChange = { useTimerInBackground = it }
                        )

                        SettingsToggleItem(
                            icon = Icons.Filled.Refresh,
                            title = "Reset Session Every Day",
                            checked = resetSessionEveryDay,
                            onCheckedChange = { resetSessionEveryDay = it }
                        )

                        SettingsToggleItem(
                            icon = Icons.Filled.VisibilityOff,
                            title = "Hide Navigation Bar",
                            checked = hideNavigationBar,
                            onCheckedChange = { hideNavigationBar = it }
                        )

                        SettingsToggleItem(
                            icon = Icons.Filled.VisibilityOff,
                            title = "Hide status bar during focus",
                            checked = hideStatusBarDuringFocus,
                            onCheckedChange = { hideStatusBarDuringFocus = it }
                        )
                    }
                )

                // Localization & Time Section
                SettingsSection(
                    title = "Localization & Time",
                    items = {
                        SettingsToggleItem(
                            icon = Icons.Filled.TextFields,
                            title = "Follow system font settings",
                            checked = followSystemFontSettings,
                            onCheckedChange = { followSystemFontSettings = it }
                        )

                        SettingsDropdownItem(
                            icon = Icons.Filled.Schedule,
                            title = "Day Start Time",
                            value = dayStartTime,
                            onValueChange = { dayStartTime = it }
                        )

                        SettingsDropdownItem(
                            icon = Icons.Filled.Language,
                            title = "Language",
                            value = language,
                            onValueChange = { language = it }
                        )
                    }
                )

                // Focus Mode Section
                SettingsSection(
                    title = "Focus Mode",
                    items = {
                        SettingsToggleItem(
                            icon = Icons.Filled.DoNotDisturb,
                            title = "Use Do Not Disturb During Focus",
                            checked = useDoNotDisturbDuringFocus,
                            onCheckedChange = { useDoNotDisturbDuringFocus = it }
                        )
                    }
                )

                // Account Section
                SettingsSection(
                    title = "Account",
                    items = {
                        // Account items would go here
                    }
                )

                Spacer(modifier = Modifier.height(80.dp)) // Space for bottom navigation
            }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    items: @Composable () -> Unit
) {
    Column {
        // Section Header
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(
                top = 16.dp,
                bottom = 8.dp,
                start = 4.dp
            )
        )
        
        // Section Items Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2C2C2C).copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                items()
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    action: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { action() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.8f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4CAF50),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF666666)
            )
        )
    }
}

@Composable
fun SettingsDropdownItem(
    icon: ImageVector,
    title: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )
    }
    
    // Dropdown menu would be implemented here
    if (expanded) {
        // Implementation for dropdown menu
    }
}

