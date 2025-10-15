package com.perseverance.pvc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.perseverance.pvc.navigation.Screen
import com.perseverance.pvc.navigation.bottomNavItems

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        containerColor = Color(0xFF1C1C1C),
        contentColor = Color.White
    ) {
        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.route,
                        modifier = Modifier.size(24.dp)
                    )
                },
                selected = currentRoute == screen.route,
                onClick = { onNavigate(screen.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFFD700),
                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                    indicatorColor = Color(0xFF2C2C2C)
                )
            )
        }
    }
}

