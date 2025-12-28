package com.perseverance.pvc

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.perseverance.pvc.utils.PermissionManager
import com.perseverance.pvc.utils.AnalyticsHelper
import com.perseverance.pvc.di.SupabaseModule
import io.github.jan.supabase.auth.handleDeeplinks
import android.content.Intent

class MainActivity : ComponentActivity() {
    private var pomodoroViewModel: PomodoroViewModel? = null
    
    // Notification permission launcher
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, notifications are now enabled
        } else {
            // Permission denied, user will need to enable manually in settings
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request notification permission on first launch
        requestNotificationPermissionIfNeeded()
        
        // Enable edge-to-edge display with visible status bar
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Configure status bar to be visible with light icons (for dark theme)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            isAppearanceLightStatusBars = false // Use light icons on dark background
        }
        
        // Log App Open
        // Log App Open
        AnalyticsHelper.logEvent("app_open")
        
        // Get FCM Token for debugging
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    android.util.Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                android.util.Log.d("MainActivity", "FCM Token: $token")
                // android.widget.Toast.makeText(this, "FCM Token retrieved (Check Logcat)", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
             android.util.Log.e("MainActivity", "Error fetching FCM token", e)
        }

        // Get Firebase Installation ID for In-App Messaging testing
        try {
            com.google.firebase.installations.FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    android.util.Log.d("MainActivity", "Firebase Installation ID: ${task.result}")
                } else {
                    android.util.Log.e("MainActivity", "Unable to get Installation ID", task.exception)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error fetching Installation ID", e)
        }
        
        // Handle deep links for Supabase Auth
        val intent = intent
        val data = intent.data
        if (data != null && data.scheme == "pvcperseverance" && data.host == "login") {
            android.widget.Toast.makeText(this, "Processing Login...", android.widget.Toast.LENGTH_LONG).show()
        }
        SupabaseModule.client.handleDeeplinks(intent)

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

            // Update status bar icons based on theme
            val view = androidx.compose.ui.platform.LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (context as android.app.Activity).window
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    // isAppearanceLightStatusBars = true -> Dark Icons (for Light Theme)
                    // isAppearanceLightStatusBars = false -> Light Icons (for Dark Theme)
                    insetsController.isAppearanceLightStatusBars = !useDarkTheme
                }
            }

            PerseverancePVCTheme(themeMode = darkMode) {
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val data = intent.data
        if (data != null && data.scheme == "pvcperseverance" && data.host == "login") {
            android.widget.Toast.makeText(this, "Processing Login...", android.widget.Toast.LENGTH_LONG).show()
        }
        SupabaseModule.client.handleDeeplinks(intent)
    }
    
    override fun onPause() {
        super.onPause()
        // Auto-save timer state when app goes to background
        pomodoroViewModel?.onAppGoingToBackground()
    }
    
    override fun onStart() {
        super.onStart()
        // Restore timer state when app comes to foreground
        pomodoroViewModel?.onAppReturningToForeground()
    }
    
    override fun onResume() {
        super.onResume()
        // Ensure UI state is properly restored when app resumes
        pomodoroViewModel?.onAppResumed()
    }
    
    override fun onStop() {
        super.onStop()
        // Additional auto-save when app is stopped
        pomodoroViewModel?.onAppGoingToBackground()
    }
    
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionManager.hasNotificationPermission(this)) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    fun requestBackgroundPermissionsIfNeeded() {
        // Request all background permissions for proper timer functionality
        PermissionManager.requestAllBackgroundPermissions(this)
    }
    
    fun setStatusBarVisibility(isVisible: Boolean) {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (isVisible) {
            windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
        } else {
            windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
            windowInsetsController.systemBarsBehavior = 
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

@Composable
fun AppNavigation(
    onPomodoroViewModelCreated: (PomodoroViewModel) -> Unit = {}
) {
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application
        )
    )
    
    val onboardingCompleted by settingsViewModel.onboardingCompleted.collectAsState()
    var showOnboarding by remember { mutableStateOf(false) }
    
    // Check if onboarding should be shown
    LaunchedEffect(onboardingCompleted) {
        showOnboarding = !onboardingCompleted
    }
    
    var currentRoute by remember { mutableStateOf(Screen.Home.route) }
    var previousRoute by remember { mutableStateOf(Screen.Home.route) }
    var showBottomBar by remember { mutableStateOf(true) }
    
    // Function to navigate to a route and remember the previous one
    fun navigateToRoute(route: String) {
        if (route != currentRoute) {
            previousRoute = currentRoute
            currentRoute = route
            AnalyticsHelper.logScreenView(route)
        }
    }
    
    // Function to go back to previous route
    fun goBack() {
        currentRoute = previousRoute
    }
    

    
    // Show onboarding screen if not completed
    if (showOnboarding) {
        OnboardingScreen(
            onComplete = {
                settingsViewModel.completeOnboarding()
                showOnboarding = false
            }
        )
        return
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
                    onNavigateToInsights = { navigateToRoute(Screen.Insights.route) },
                    onNavigateToMenu = { navigateToRoute(Screen.Menu.route) }
                ) // Dashboard = Page2Screen (study stats)
                Screen.Home.route -> PomodoroScreen(
                    onNavigateToSettings = { navigateToRoute(Screen.Settings.route) },
                    onNavigateToInsights = { navigateToRoute(Screen.Insights.route) },
                    onNavigateToMenu = { navigateToRoute(Screen.Menu.route) },
                    onTimerStateChanged = { isPlaying -> 
                        showBottomBar = !isPlaying 
                    },
                    onViewModelCreated = onPomodoroViewModelCreated
                ) // Home = Pomodoro timer
                Screen.Group.route -> GroupScreen(
                    onNavigateToSettings = { navigateToRoute(Screen.Settings.route) },
                    onNavigateToInsights = { navigateToRoute(Screen.Insights.route) },
                    onNavigateToMenu = { navigateToRoute(Screen.Menu.route) },
                    onNavigateToGroupDetails = { navigateToRoute(Screen.GroupDetails.route) }
                ) // Group = GroupScreen (Study Groups)
                Screen.Settings.route -> SettingsScreen(
                    onNavigateToSettings = { navigateToRoute(Screen.Settings.route) },
                    onNavigateToInsights = { navigateToRoute(Screen.Insights.route) },
                    onNavigateToMenu = { navigateToRoute(Screen.Menu.route) },
                    onBackClick = { goBack() }
                )
                Screen.Profile.route -> ProfileScreen(
                    onNavigateToSettings = { navigateToRoute(Screen.Settings.route) },
                    onNavigateToInsights = { navigateToRoute(Screen.Insights.route) },
                    onNavigateToMenu = { navigateToRoute(Screen.Menu.route) },
                    onNavigateToEditProfile = { navigateToRoute(Screen.EditProfile.route) }
                )
                Screen.EditProfile.route -> {
                    val socialViewModel: com.perseverance.pvc.ui.viewmodel.SocialViewModel = viewModel(
                        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
                            context.applicationContext as android.app.Application
                        )
                    )
                    val socialUiState by socialViewModel.uiState.collectAsState()
                    val currentUser = socialUiState.currentUser
                    val isProfileIncomplete = currentUser?.displayName?.isEmpty() == true || 
                                             currentUser?.photoUrl?.isEmpty() == true
                    
                    EditProfileScreen(
                        onNavigateToSettings = { navigateToRoute(Screen.Settings.route) },
                        onNavigateToInsights = { navigateToRoute(Screen.Insights.route) },
                        onNavigateToMenu = { navigateToRoute(Screen.Menu.route) },
                        onBackClick = { goBack() },
                        isProfileIncomplete = isProfileIncomplete,
                        socialViewModel = socialViewModel
                    )
                }
                Screen.GroupDetails.route -> {
                    GroupDetailsScreen(
                        onNavigateToSettings = { navigateToRoute(Screen.Settings.route) },
                        onBackClick = { goBack() }
                    )
                }
                Screen.Insights.route -> Page1Screen(
                    onNavigateToSettings = { navigateToRoute(Screen.Settings.route) },
                    onNavigateToInsights = { navigateToRoute(Screen.Insights.route) },
                    onNavigateToMenu = { navigateToRoute(Screen.Menu.route) },
                    onBackClick = { goBack() }
                ) // Insights = Page1Screen (accessible via top icon)
                Screen.Menu.route -> MenuScreen(
                    onNavigateToSettings = { navigateToRoute(Screen.Settings.route) },
                    onNavigateToInsights = { navigateToRoute(Screen.Insights.route) },
                    onBackClick = { goBack() },
                    onNavigate = { route -> navigateToRoute(route) }
                ) // Menu = MenuScreen (accessible via hamburger menu)
                Screen.Developer.route -> DeveloperModeScreen(
                    onNavigateToSettings = { navigateToRoute(Screen.Settings.route) },
                    onNavigateToInsights = { navigateToRoute(Screen.Insights.route) },
                    onBackClick = { goBack() }
                ) // Developer = DeveloperModeScreen (accessible via menu)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavigationPreview() {
    PerseverancePVCTheme(themeMode = "Dark") {
        AppNavigation()
    }
}