package com.perseverance.pvc

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.perseverance.pvc.navigation.Screen
import com.perseverance.pvc.ui.components.BottomNavigationBar
import com.perseverance.pvc.ui.screens.*
import com.perseverance.pvc.ui.theme.PerseverancePVCTheme
import com.perseverance.pvc.ui.viewmodel.SettingsViewModel
import com.perseverance.pvc.ui.viewmodel.PomodoroViewModel

class MainActivity : ComponentActivity() {
    private var pomodoroViewModel: PomodoroViewModel? = null
    
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
            // Observe user's theme preference from settings
            val context = LocalContext.current
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
                    context.applicationContext as android.app.Application
                )
            )
            val darkMode by settingsViewModel.darkMode.collectAsState()
            val useDarkTheme = when (darkMode) {
                "Dark" -> true
                "Light" -> false
                "System" -> isSystemInDarkTheme()
                else -> true // Default to dark theme for new users
            }

            PerseverancePVCTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(onPomodoroViewModelCreated = { viewModel ->
                        pomodoroViewModel = viewModel
                    })
                }
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Auto-save timer state when app goes to background
        pomodoroViewModel?.onAppGoingToBackground()
    }
    
    override fun onStop() {
        super.onStop()
        // Additional auto-save when app is stopped
        pomodoroViewModel?.onAppGoingToBackground()
    }
}

@Composable
fun AppNavigation(
    onPomodoroViewModelCreated: (PomodoroViewModel) -> Unit = {}
) {
    var currentRoute by remember { mutableStateOf(Screen.Home.route) }
    var previousRoute by remember { mutableStateOf(Screen.Home.route) }
    var showBottomBar by remember { mutableStateOf(true) }
    
    // Function to navigate to a route and remember the previous one
    fun navigateToRoute(route: String) {
        if (route != currentRoute) {
            previousRoute = currentRoute
            currentRoute = route
        }
    }
    
    // Function to go back to previous route
    fun goBack() {
        currentRoute = previousRoute
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route -> navigateToRoute(route) }
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
                    onNavigateToSettings = { navigateToRoute(Screen.Settings.route) },
                    onNavigateToInsights = { navigateToRoute(Screen.Insights.route) }
                ) // Dashboard = Page2Screen (study stats)
                Screen.Home.route -> PomodoroScreen(
                    onNavigateToSettings = { navigateToRoute(Screen.Settings.route) },
                    onNavigateToInsights = { navigateToRoute(Screen.Insights.route) },
                    onTimerStateChanged = { isPlaying -> 
                        showBottomBar = !isPlaying 
                    },
                    onViewModelCreated = onPomodoroViewModelCreated
                ) // Home = Pomodoro timer
                Screen.Group.route -> GroupScreen(
                    onNavigateToSettings = { navigateToRoute(Screen.Settings.route) },
                    onNavigateToInsights = { navigateToRoute(Screen.Insights.route) }
                ) // Group = GroupScreen (Study Groups)
                Screen.Settings.route -> SettingsScreen(
                    onNavigateToSettings = { navigateToRoute(Screen.Settings.route) },
                    onNavigateToInsights = { navigateToRoute(Screen.Insights.route) },
                    onBackClick = { goBack() }
                )
                Screen.Insights.route -> Page1Screen(
                    onNavigateToSettings = { navigateToRoute(Screen.Settings.route) },
                    onNavigateToInsights = { navigateToRoute(Screen.Insights.route) },
                    onBackClick = { goBack() }
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