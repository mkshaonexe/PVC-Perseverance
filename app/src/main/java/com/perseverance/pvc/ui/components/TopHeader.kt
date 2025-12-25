package com.perseverance.pvc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    onHamburgerClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    showBackButton: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
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
                    .width(18.dp)
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.onBackground)
            )
            Spacer(modifier = Modifier.height(3.dp))
            // Second line
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.onBackground)
            )
            Spacer(modifier = Modifier.height(3.dp))
            // Third line
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.onBackground)
            )
        }
        
        // Right side icons - either back button or settings/insights
        if (showBackButton) {
            // Back button when on settings or insights page
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onBackClick() }
            )
        } else {
            // Icons removed as per user request
        }
        }
    }
