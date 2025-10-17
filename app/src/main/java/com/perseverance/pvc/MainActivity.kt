package com.perseverance.pvc

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.perseverance.pvc.navigation.Screen
import com.perseverance.pvc.ui.components.BottomNavigationBar
import com.perseverance.pvc.ui.screens.*
import com.perseverance.pvc.ui.theme.PerseverancePVCTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable full screen mode
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Hide status bar
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        
        setContent {
            PerseverancePVCTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var currentRoute by remember { mutableStateOf(Screen.Home.route) }
    var showBottomBar by remember { mutableStateOf(true) }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route -> currentRoute = route }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (showBottomBar) paddingValues else PaddingValues(0.dp))
        ) {
            when (currentRoute) {
                Screen.Dashboard.route -> Page2Screen(
                    onNavigateToSettings = { currentRoute = Screen.Settings.route },
                    onNavigateToInsights = { currentRoute = Screen.Insights.route }
                ) // Dashboard = Page2Screen (study stats)
                Screen.Home.route -> PomodoroScreen(
                    onNavigateToSettings = { currentRoute = Screen.Settings.route },
                    onNavigateToInsights = { currentRoute = Screen.Insights.route },
                    onTimerStateChanged = { isPlaying -> 
                        showBottomBar = !isPlaying 
                    }
                ) // Home = Pomodoro timer
                Screen.Group.route -> GroupScreen(
                    onNavigateToSettings = { currentRoute = Screen.Settings.route },
                    onNavigateToInsights = { currentRoute = Screen.Insights.route }
                ) // Group = GroupScreen (Study Groups)
                Screen.Settings.route -> SettingsScreen(
                    onNavigateToSettings = { currentRoute = Screen.Settings.route },
                    onNavigateToInsights = { currentRoute = Screen.Insights.route }
                )
                Screen.Insights.route -> Page1Screen(
                    onNavigateToSettings = { currentRoute = Screen.Settings.route },
                    onNavigateToInsights = { currentRoute = Screen.Insights.route }
                ) // Insights = Page1Screen (accessible via top icon)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavigationPreview() {
    PerseverancePVCTheme {
        AppNavigation()
    }
}