package com.perseverance.pvc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.luminance

@Composable
fun TopHeader(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onHamburgerClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
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
                    .width(24.dp)
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.onBackground)
            )
            Spacer(modifier = Modifier.height(5.dp))
            // Second line
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.onBackground)
            )
            Spacer(modifier = Modifier.height(5.dp))
            // Third line
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.onBackground)
            )
        }
        
        // Settings and Insights icons in top right
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Insights icon
            Icon(
                imageVector = Icons.Filled.Insights,
                contentDescription = "Insights",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onNavigateToInsights() }
            )
            
            // Settings icon
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onNavigateToSettings() }
            )
        }
    }
}
