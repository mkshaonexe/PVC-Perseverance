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
    val menuItems = listOf(
        MenuItem(
            title = "Dashboard",
            icon = Icons.Filled.Dashboard,
            route = "dashboard",
            description = "View your study statistics"
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
            title = "Profile",
            icon = Icons.Filled.Person,
            route = "profile",
            description = "Edit your personal information"
        ),
        MenuItem(
            title = "Settings",
            icon = Icons.Filled.Settings,
            route = "settings",
            description = "App preferences and configuration"
        ),
        MenuItem(
            title = "Bug Report",
            icon = Icons.Filled.BugReport,
            route = "bug_report",
            description = "Report issues or bugs"
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
            showBackButton = true,
            title = "Menu"
        )

        val context = androidx.compose.ui.platform.LocalContext.current

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
                        if (item.route == "bug_report") {
                            // Gather device info
                            val packageManager = context.packageManager
                            val packageName = context.packageName
                            var versionName = "Unknown"
                            var versionCode = "Unknown"
                            
                            try {
                                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                                versionName = packageInfo.versionName ?: "Unknown"
                                versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                    packageInfo.longVersionCode.toString()
                                } else {
                                    @Suppress("DEPRECATION")
                                    packageInfo.versionCode.toString()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            
                            val deviceName = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL
                            val androidVersion = android.os.Build.VERSION.RELEASE
                            
                            val emailBody = """
                                what issu are you facing write here :


                                ____________________________________________________________
                                App version  $versionName   app vision code $versionCode 
                                device $deviceName            andoin version $androidVersion 

                                all permisio given or s the app mission permsion 
                                ________________________________________________________

                                thank you 
                            """.trimIndent()

                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf("mkshaonnew31@gmail.com"))
                                putExtra(Intent.EXTRA_SUBJECT, "PVC-Study app bug reort/issu")
                                putExtra(Intent.EXTRA_TEXT, emailBody)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Handle case where no email app is available
                            }
                        } else {
                            onNavigate(item.route)
                        }
                    }
                )
            }
            
            // App version and developer info
            item {
                AppVersionInfo()
            }
        }
    }
    

}

@Composable
fun MenuItemCard(
    item: MenuItem,
    onClick: () -> Unit
) {
    val isLight = isLightTheme()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = glassBorder(isLight),
        elevation = glassElevation(isLight)
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
            
            // Arrow icon
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Navigate to ${item.title}",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
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
                text = "v 0.7.6",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = versionColor,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Last Update: 28 Dec",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = versionColor.copy(alpha = 0.8f),
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
            
            // Gmail button with glassmorphism style
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:mkshaon2024@gmail.com")
                        }
                        context.startActivity(intent)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isLight)
                        versionColor.copy(alpha = 0.1f)
                    else
                        versionColor.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    versionColor.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = "Email",
                        tint = versionColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "mkshaon2024@gmail.com",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = versionColor
                    )
                }
            }
        }
    }
}
