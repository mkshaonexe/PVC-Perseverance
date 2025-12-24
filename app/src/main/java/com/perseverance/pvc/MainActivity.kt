package com.perseverance.pvc

import android.Manifest
import android.content.pm.PackageManager
import com.google.firebase.analytics.FirebaseAnalytics
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
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
import com.perseverance.pvc.utils.AnalyticsHelper
import com.perseverance.pvc.utils.PermissionManager

class MainActivity : ComponentActivity() {
    private var pomodoroViewModel: PomodoroViewModel? = null
    
    // Notification permission launcher
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, notifications are now enabled
            AnalyticsHelper.logEvent("permission_notification_granted")
        } else {
            // Permission denied, user will need to enable manually in settings
            AnalyticsHelper.logEvent("permission_notification_denied")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Log App Open
        AnalyticsHelper.logEvent(FirebaseAnalytics.Event.APP_OPEN)
        
        // Request notification permission on first launch
        requestNotificationPermissionIfNeeded()
        
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
            
            // Auth ViewModel
            val authViewModel: com.perseverance.pvc.ui.viewmodel.AuthViewModel = viewModel(
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
            
            // Observe app rotation setting and update screen orientation
            val allowAppRotation by settingsViewModel.allowAppRotation.collectAsState()
            
            LaunchedEffect(allowAppRotation) {
                requestedOrientation = if (allowAppRotation) {
                    android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR
                } else {
                    android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }

            PerseverancePVCTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        onPomodoroViewModelCreated = { viewModel ->
                            pomodoroViewModel = viewModel
                        },
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
    
    // ... (Lifecycle methods remain the same)
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
}

@Composable
fun AppNavigation(
    onPomodoroViewModelCreated: (PomodoroViewModel) -> Unit = {},
    authViewModel: com.perseverance.pvc.ui.viewmodel.AuthViewModel
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
    
    // Determine start destination based on auth state
    // We can check this synchronously for simplicity or use the Flow
    val isUserLoggedIn = authViewModel.isUserLoggedIn()
    
    // Default to Home (Offline First)
    var currentRoute by remember { 
        mutableStateOf(Screen.Home.route) 
    }
    var previousRoute by remember { mutableStateOf(Screen.Home.route) } // Default previous
    
    // Track screen views
    LaunchedEffect(currentRoute) {
        AnalyticsHelper.logScreenView(currentRoute)
    }

    var showBottomBar by remember { mutableStateOf(true) }
    
    // Update bottom bar visibility based on route
    LaunchedEffect(currentRoute) {
        showBottomBar = when(currentRoute) {
             Screen.Login.route, Screen.StudyGroupSelection.route -> false 
             else -> true
        }
    }

    var lastSwipeTime by remember { mutableStateOf(0L) }
    
    // Define the navigation order for swipe gestures
    val navigationOrder = listOf(
        Screen.Dashboard.route,
        Screen.Home.route,
        Screen.Group.route,
        Screen.Settings.route
    )
    
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
    
    // Function to navigate to next page (swipe left)
    fun navigateNext() {
        val currentIndex = navigationOrder.indexOf(currentRoute)
        if (currentIndex >= 0 && currentIndex < navigationOrder.size - 1) {
            navigateToRoute(navigationOrder[currentIndex + 1])
        }
    }
    
    // Function to navigate to previous page (swipe right)
    fun navigatePrevious() {
        val currentIndex = navigationOrder.indexOf(currentRoute)
        if (currentIndex > 0) {
            navigateToRoute(navigationOrder[currentIndex - 1])
        }
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
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // We'll handle navigation in the onDrag callback
                        }
                    ) { _, dragAmount ->
                        // Swipe right (negative drag) = go to next page
                        // Swipe left (positive drag) = go to previous page
                        val currentTime = System.currentTimeMillis()
                        // Only allow swipe navigation on main screens
                        if (navigationOrder.contains(currentRoute) && currentTime - lastSwipeTime > 300) { 
                            if (dragAmount < -50) { // Swipe right = next page
                                navigateNext()
                                lastSwipeTime = currentTime
                            } else if (dragAmount > 50) { // Swipe left = previous page
                                navigatePrevious()
                                lastSwipeTime = currentTime
                            }
                        }
                    }
                }
        ) {
            when (currentRoute) {
                Screen.Login.route -> LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        // After login, go to Study Group Selection
                        navigateToRoute(Screen.StudyGroupSelection.route)
                    }
                )
                Screen.StudyGroupSelection.route -> StudyGroupSelectionScreen(
                    authViewModel = authViewModel,
                    onGroupSelected = { groupName ->
                        // After selection, go to Home
                        navigateToRoute(Screen.Home.route)
                    }
                )
                Screen.Dashboard.route -> Page2Screen(
                    onNavigateToSettings = { navigateToRoute(Screen.Settings.route) },
                    onNavigateToInsights = { navigateToRoute(Screen.Insights.route) },
                    onNavigateToMenu = { navigateToRoute(Screen.Menu.route) }
                ) // Dashboard = Page2Screen (study stats)
                Screen.Home.route -> PomodoroScreen(
                    onNavigateToSettings = { navigateToRoute(Screen.Settings.route) },
                    onNavigateToInsights = { navigateToRoute(Screen.Insights.route) },
                    onNavigateToMenu = { navigateToRoute(Screen.Menu.route) },
                    // Only hide bottom bar if timer playing, AND we are on a screen that shows it
                    onTimerStateChanged = { isPlaying -> 
                         if (navigationOrder.contains(currentRoute)) {
                             showBottomBar = !isPlaying
                         }
                    },
                    onViewModelCreated = onPomodoroViewModelCreated
                ) // Home = Pomodoro timer
                Screen.Group.route -> GroupScreen(
                    onNavigateToSettings = { navigateToRoute(Screen.Settings.route) },
                    onNavigateToInsights = { navigateToRoute(Screen.Insights.route) },
                    onNavigateToMenu = { navigateToRoute(Screen.Menu.route) }
                ) // Group = GroupScreen (Study Groups)
                Screen.Settings.route -> SettingsScreen(
                    onNavigateToSettings = { navigateToRoute(Screen.Settings.route) },
                    onNavigateToInsights = { navigateToRoute(Screen.Insights.route) },
                    onNavigateToMenu = { navigateToRoute(Screen.Menu.route) },
                    onBackClick = { goBack() }
                )
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

/*
@Preview(showBackground = true)
@Composable
fun AppNavigationPreview() {
    PerseverancePVCTheme {
        // AppNavigation() // Requires AuthViewModel
    }
}
*/