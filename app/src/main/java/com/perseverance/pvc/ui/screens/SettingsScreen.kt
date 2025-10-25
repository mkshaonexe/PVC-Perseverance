package com.perseverance.pvc.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.perseverance.pvc.ui.components.TopHeader
import com.perseverance.pvc.ui.theme.glassBorder
import com.perseverance.pvc.ui.theme.glassElevation
import com.perseverance.pvc.ui.theme.isLightTheme
import com.perseverance.pvc.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {},
    onBackClick: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Collect settings from ViewModel
    val darkMode by viewModel.darkMode.collectAsState()
    val useTimerInBackground by viewModel.useTimerInBackground.collectAsState()
    val resetSessionEveryDay by viewModel.resetSessionEveryDay.collectAsState()
    val hideNavigationBar by viewModel.hideNavigationBar.collectAsState()
    val hideStatusBarDuringFocus by viewModel.hideStatusBarDuringFocus.collectAsState()
    val followSystemFontSettings by viewModel.followSystemFontSettings.collectAsState()
    val dayStartTime by viewModel.dayStartTime.collectAsState()
    val language by viewModel.language.collectAsState()
    val useDoNotDisturbDuringFocus by viewModel.useDNDDuringFocus.collectAsState()
    val timerDuration by viewModel.timerDuration.collectAsState()
    val breakDuration by viewModel.breakDuration.collectAsState()
    val enableTimerNotifications by viewModel.enableTimerNotifications.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Simple background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
        
        // Semi-transparent overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
                        Color.Black.copy(alpha = 0.5f)
                    else
                        Color.Transparent
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Top header with hamburger menu and settings/insights icons
            TopHeader(
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToInsights = onNavigateToInsights,
                onHamburgerClick = onNavigateToMenu,
                onBackClick = onBackClick,
                showBackButton = true
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
                    color = MaterialTheme.colorScheme.onBackground,
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
                            action = { 
                                // Open app settings page
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            }
                        )

                        SettingsDropdownItem(
                            icon = Icons.Filled.DarkMode,
                            title = "Dark Mode",
                            value = darkMode,
                            options = listOf("Light", "Dark", "System"),
                            onValueChange = { viewModel.updateDarkMode(it) }
                        )

                        SettingsItem(
                            icon = Icons.Filled.Palette,
                            title = "Theme Settings",
                            action = { 
                                Toast.makeText(context, "Theme customization coming soon!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )

                // Timer Section
                SettingsSection(
                    title = "Timer",
                    items = {
                        SettingsDropdownItem(
                            icon = Icons.Filled.Timer,
                            title = "Pomodoro Timer Duration",
                            value = if (timerDuration == "Custom") "Custom" else "${timerDuration} min",
                            options = generateTimerDurationOptions(),
                            onValueChange = { viewModel.updateTimerDuration(it) }
                        )

                        SettingsDropdownItem(
                            icon = Icons.Filled.FreeBreakfast,
                            title = "Break Duration",
                            value = if (breakDuration == "Custom") "Custom" else "${breakDuration} min",
                            options = generateBreakDurationOptions(),
                            onValueChange = { viewModel.updateBreakDuration(it) }
                        )

                        SettingsToggleItem(
                            icon = Icons.Filled.Timer,
                            title = "Use Timer in Background",
                            checked = useTimerInBackground,
                            onCheckedChange = { viewModel.updateUseTimerInBackground(it) }
                        )

                        SettingsToggleItem(
                            icon = Icons.Filled.Refresh,
                            title = "Reset Session Every Day",
                            checked = resetSessionEveryDay,
                            onCheckedChange = { viewModel.updateResetSessionEveryDay(it) }
                        )

                        SettingsToggleItem(
                            icon = Icons.Filled.Notifications,
                            title = "Timer Notifications",
                            checked = enableTimerNotifications,
                            onCheckedChange = { viewModel.updateEnableTimerNotifications(it) }
                        )
                    }
                )

                // Display Section
                SettingsSection(
                    title = "Display",
                    items = {
                        SettingsToggleItem(
                            icon = Icons.Filled.VisibilityOff,
                            title = "Hide Navigation Bar",
                            checked = hideNavigationBar,
                            onCheckedChange = { viewModel.updateHideNavigationBar(it) }
                        )

                        SettingsToggleItem(
                            icon = Icons.Filled.VisibilityOff,
                            title = "Hide status bar during focus",
                            checked = hideStatusBarDuringFocus,
                            onCheckedChange = { viewModel.updateHideStatusBarDuringFocus(it) }
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
                            onCheckedChange = { viewModel.updateFollowSystemFontSettings(it) }
                        )

                        SettingsDropdownItem(
                            icon = Icons.Filled.Schedule,
                            title = "Day Start Time",
                            value = dayStartTime,
                            options = generateTimeOptions(),
                            onValueChange = { viewModel.updateDayStartTime(it) }
                        )

                        SettingsDropdownItem(
                            icon = Icons.Filled.Language,
                            title = "Language",
                            value = language,
                            options = listOf("English", "Spanish", "French", "German", "Chinese", "Japanese", "Korean", "Hindi", "Arabic"),
                            onValueChange = { viewModel.updateLanguage(it) }
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
                            onCheckedChange = { viewModel.updateUseDNDDuringFocus(it) }
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

                // App Info Section
                SettingsSection(
                    title = "App Information",
                    items = {
                        SettingsInfoItem(
                            icon = Icons.Filled.Info,
                            title = "App Version",
                            value = "0.4.5"
                        )
                        
                        SettingsInfoItem(
                            icon = Icons.Filled.Update,
                            title = "Last Update",
                            value = "25 Oct 9:01 PM"
                        )
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
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() >= 0.5f
    
    Column {
        // Section Header
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
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
                containerColor = if (isLightTheme)
                    MaterialTheme.colorScheme.surface
                else
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp),
            border = glassBorder(isLightTheme),
            elevation = glassElevation(isLightTheme)
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
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = if (MaterialTheme.colorScheme.background.luminance() >= 0.5f) 
                Color(0xFF6B7280) else Color(0xFF4CAF50),
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
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.8f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (MaterialTheme.colorScheme.background.luminance() >= 0.5f) 
                    Color.White else MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = if (MaterialTheme.colorScheme.background.luminance() >= 0.5f) 
                    Color(0xFF6B7280) else Color(0xFF4CAF50),
                uncheckedThumbColor = if (MaterialTheme.colorScheme.background.luminance() >= 0.5f) 
                    Color(0xFFE5E7EB) else MaterialTheme.colorScheme.onSurface,
                uncheckedTrackColor = if (MaterialTheme.colorScheme.background.luminance() >= 0.5f) 
                    Color(0xFFD1D5DB) else Color(0xFF666666)
            )
        )
    }
}

@Composable
fun SettingsDropdownItem(
    icon: ImageVector,
    title: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )
    }
    
    // Dialog with options
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            val isLight = isLightTheme()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                border = glassBorder(isLight),
                elevation = glassElevation(isLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        options.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (option == "Custom") {
                                            showDialog = false
                                            showCustomDialog = true
                                        } else {
                                            onValueChange(option)
                                            showDialog = false
                                        }
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = option == value,
                                    onClick = {
                                        if (option == "Custom") {
                                            showDialog = false
                                            showCustomDialog = true
                                        } else {
                                            onValueChange(option)
                                            showDialog = false
                                        }
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = if (MaterialTheme.colorScheme.background.luminance() >= 0.5f) 
                                            Color(0xFF6B7280) else Color(0xFF4CAF50),
                                        unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (option == "Custom") option else "${option} min",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Custom Timer Input Dialog
    if (showCustomDialog) {
        CustomTimerInputDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { customMinutes ->
                onValueChange(customMinutes)
                showCustomDialog = false
            }
        )
    }
}

@Composable
fun CustomTimerInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var customMinutes by remember { mutableStateOf("") }
    var isValid by remember { mutableStateOf(false) }
    
    // Validate input
    LaunchedEffect(customMinutes) {
        val minutes = customMinutes.toIntOrNull()
        isValid = minutes != null && minutes > 0 && minutes <= 999
    }
    
    Dialog(onDismissRequest = onDismiss) {
        val isLight = isLightTheme()
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            border = glassBorder(isLight),
            elevation = glassElevation(isLight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Custom Timer Duration",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "Enter timer duration in minutes (1-999):",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = customMinutes,
                    onValueChange = { 
                        // Only allow numbers
                        if (it.all { char -> char.isDigit() }) {
                            customMinutes = it
                        }
                    },
                    label = { Text("Minutes") },
                    placeholder = { Text("e.g., 75") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (MaterialTheme.colorScheme.background.luminance() >= 0.5f) 
                                Color(0xFF6B7280) else Color(0xFF4CAF50),
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { onConfirm(customMinutes) },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (MaterialTheme.colorScheme.background.luminance() >= 0.5f) 
                                Color(0xFF6B7280) else Color(0xFF4CAF50),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Set Timer")
                    }
                }
            }
        }
    }
}

// Helper function to generate time options
private fun generateTimeOptions(): List<String> {
    val times = mutableListOf<String>()
    for (hour in 0..23) {
        for (minute in listOf(0, 30)) {
            val period = if (hour < 12) "AM" else "PM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            times.add(String.format("%d:%02d %s", displayHour, minute, period))
        }
    }
    return times
}

// Helper function to generate timer duration options
private fun generateTimerDurationOptions(): List<String> {
    return listOf("10", "50", "60", "Custom")
}

// Helper function to generate break duration options
private fun generateBreakDurationOptions(): List<String> {
    return listOf("5", "10", "15", "20", "Custom")
}

@Composable
fun SettingsInfoItem(
    icon: ImageVector,
    title: String,
    value: String
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
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

