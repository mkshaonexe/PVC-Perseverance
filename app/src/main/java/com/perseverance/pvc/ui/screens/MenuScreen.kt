package com.perseverance.pvc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColor
import android.content.Intent
import android.net.Uri
import com.perseverance.pvc.ui.components.TopHeader
import com.perseverance.pvc.ui.theme.glassBorder
import com.perseverance.pvc.ui.theme.glassElevation
import com.perseverance.pvc.ui.theme.isLightTheme

data class MenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val description: String = ""
)

@Composable
fun MenuScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    var developerClickCount by remember { mutableStateOf(0) }
    var isDeveloperModeUnlocked by remember { mutableStateOf(false) }
    var showDeveloperPopup by remember { mutableStateOf(false) }
    val menuItems = listOf(
        MenuItem(
            title = "Dashboard",
            icon = Icons.Filled.Dashboard,
            route = "dashboard",
            description = "View your study statistics"
        ),
        MenuItem(
            title = "Focus Timer",
            icon = Icons.Filled.Home,
            route = "home",
            description = "Start a focus session"
        ),
        MenuItem(
            title = "Study Groups",
            icon = Icons.Filled.Group,
            route = "group",
            description = "Join or create study groups"
        ),
        MenuItem(
            title = "Insights",
            icon = Icons.Filled.Insights,
            route = "insights",
            description = "View detailed analytics"
        ),
        MenuItem(
            title = "Settings",
            icon = Icons.Filled.Settings,
            route = "settings",
            description = "App preferences and configuration"
        ),
        MenuItem(
            title = "Developer Mode",
            icon = Icons.Filled.Build,
            route = "developer",
            description = "Add study time manually for testing"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top header with back button
        TopHeader(
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToInsights = onNavigateToInsights,
            onBackClick = onBackClick,
            showBackButton = true
        )

        // Menu title
        Text(
            text = "Menu",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        // Menu items list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(menuItems) { item ->
                MenuItemCard(
                    item = item,
                    onClick = { 
                        if (item.route == "developer") {
                            // Special handling for Developer Mode
                            developerClickCount++
                            if (developerClickCount >= 7) {
                                isDeveloperModeUnlocked = true
                                showDeveloperPopup = true
                                // Navigate after showing popup
                                onNavigate(item.route)
                            }
                            // Don't navigate on clicks 1-6, just count them
                        } else {
                            onNavigate(item.route)
                        }
                    },
                    isDeveloperMode = item.route == "developer",
                    isUnlocked = isDeveloperModeUnlocked
                )
            }
            
            // App version and developer info
            item {
                AppVersionInfo()
            }
        }
    }
    
    // Developer mode popup
    if (showDeveloperPopup) {
        AlertDialog(
            onDismissRequest = { showDeveloperPopup = false },
            title = {
                Text(
                    text = "🎉 Developer Mode Unlocked!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "You are now a developer!\n\nYou can now add study time manually for testing purposes.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDeveloperPopup = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Got it!")
                }
            }
        )
    }
}

@Composable
fun MenuItemCard(
    item: MenuItem,
    onClick: () -> Unit,
    isDeveloperMode: Boolean = false,
    isUnlocked: Boolean = false
) {
    val isLight = isLightTheme()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isDeveloperMode && !isUnlocked) {
                // Subtle color change for developer mode (always when not unlocked)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = glassBorder(isLight),
        elevation = if (isDeveloperMode && !isUnlocked) {
            CardDefaults.cardElevation(defaultElevation = 6.dp)
        } else {
            glassElevation(isLight)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                if (item.description.isNotEmpty()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // Arrow icon (only show for non-developer items or unlocked developer mode)
            if (!isDeveloperMode || isUnlocked) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Navigate to ${item.title}",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AppVersionInfo() {
    val isLight = isLightTheme()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Fixed turquoise color
    val versionColor = Color(0xFF66E0FF) // Turquoise
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLight)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = glassBorder(isLight),
        elevation = glassElevation(isLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "v 0.4.0",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = versionColor,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "MK Shaon",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = versionColor,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Gmail button
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:mkshaon2024@gmail.com")
                    }
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = versionColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = "Email",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "mkshaon2024@gmail.com",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
