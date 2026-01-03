package com.perseverance.pvc.ui.screens

import android.text.format.DateUtils
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.perseverance.pvc.data.StudyRepository
import com.perseverance.pvc.ui.components.TopHeader
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToEditProfile: () -> Unit = {},
    socialViewModel: com.perseverance.pvc.ui.viewmodel.SocialViewModel = viewModel()
) {
    val context = LocalContext.current
    val repository = remember { StudyRepository(context) }
    val socialUiState by socialViewModel.uiState.collectAsState()
    val currentUser = socialUiState.currentUser
    
    // Check authentication and profile completion
    val isSignedIn = socialUiState.isSignedIn
    val isProfileComplete = currentUser?.displayName?.isNotEmpty() == true && 
                           currentUser?.photoUrl?.isNotEmpty() == true
    
    // If neither signed in nor having local data, show skeleton with login prompt
    if (!isSignedIn && currentUser == null) {
        Scaffold(
            topBar = {
                TopHeader(
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToInsights = onNavigateToInsights,
                    onHamburgerClick = onNavigateToMenu,
                    showBackButton = false,
                    title = "Profile"
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f) // Use 90% width instead of default logic
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp), // Reduced from 32dp
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var email by remember { mutableStateOf("") }
                        var password by remember { mutableStateOf("") }
                        var name by remember { mutableStateOf("") }
                        var isSignUp by remember { mutableStateOf(false) }

                        // Show Toast on Error
                        val currentError = socialUiState.error
                        LaunchedEffect(currentError) {
                            if (currentError != null) {
                                android.widget.Toast.makeText(context, "Error: $currentError", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }

                        Icon(
                            imageVector = Icons.Filled.Leaderboard,
                            contentDescription = "Profile",
                            modifier = Modifier.size(48.dp), // Reduced from 64dp
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp)) // Reduced form 16
                        Text(
                            text = if (isSignUp) "Create Account" else "Welcome Back",
                            fontSize = 20.sp, // Reduced from 24
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp)) // Reduced from 8
                        Text(
                            text = "Sign in to view your profile and stats.",
                            fontSize = 12.sp, // Reduced from 14
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp)) // Reduced from 24

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email", fontSize = 12.sp) }, // Smaller label
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                        )
                        Spacer(modifier = Modifier.height(8.dp)) // Reduced from 12

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                        )
                        
                        if (isSignUp) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Display Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (socialUiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp).padding(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Button(
                                onClick = { 
                                    if (isSignUp) {
                                        socialViewModel.performEmailSignUp(email, password, name)
                                    } else {
                                        socialViewModel.performEmailLogin(email, password)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !socialUiState.isLoading
                            ) {
                                Text(if (isSignUp) "Sign Up" else "Login", fontSize = 16.sp)
                            }
                        }

                        if (socialUiState.error != null) {
                            Text(
                                text = socialUiState.error ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                                
                        TextButton(onClick = { isSignUp = !isSignUp }) {
                            Text(
                                text = if (isSignUp) "Already have an account? Login" else "Don't have an account? Sign Up",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        return
    }
    
    // If signed in but profile incomplete, navigate to edit profile
    LaunchedEffect(isSignedIn, isProfileComplete) {
        if (isSignedIn && !isProfileComplete) {
            onNavigateToEditProfile()
        }
    }
    
    var todayStudySeconds by remember { mutableStateOf(0) }
    var weeklyStudyHours by remember { mutableStateOf("0:0") }
    var monthlyStudyHours by remember { mutableStateOf("0:0") }
    var allTimeStudyHours by remember { mutableStateOf("0:0") }
    var avgStudyTime by remember { mutableStateOf("0:0") }
    var maxFocusSession by remember { mutableStateOf("0:0") }
    var totalSessionsToday by remember { mutableStateOf(0) }
    var userRank by remember { mutableStateOf((1..1000).random()) }
    var monthlyStudyData by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) } // day -> seconds
    val currentMonth = remember { LocalDate.now() }

    // Fetch study data
    LaunchedEffect(Unit) {
        val todaySeconds = repository.getTodayTotalSeconds().first()
        todayStudySeconds = todaySeconds
        
        // Get today's sessions for total count and max focus
        val todaySessions = repository.getStudySessionsByDate(LocalDate.now()).first()
        totalSessionsToday = todaySessions.size
        maxFocusSession = todaySessions.maxOfOrNull { it.durationSeconds }?.let {
            val hours = it / 3600
            val minutes = (it % 3600) / 60
            "$hours:${minutes.toString().padStart(2, '0')}"
        } ?: "0:0"
        
        // Calculate monthly data for calendar
        val monthStart = currentMonth.withDayOfMonth(1)
        val monthEnd = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth())
        val monthSessions = repository.getStudySessionsInRange(monthStart, monthEnd).first()
        
        val dailyData = monthSessions.groupBy { it.startTime?.toLocalDate()?.dayOfMonth ?: 0 }
            .mapValues { (_, sessions) -> sessions.sumOf { it.durationSeconds } }
        monthlyStudyData = dailyData
        
        // Calculate monthly total
        val monthlySeconds = monthSessions.sumOf { it.durationSeconds }
        
        // Calculate weekly total (last 7 days)
        val weekStart = LocalDate.now().minusDays(6)
        val weekSessions = repository.getStudySessionsInRange(weekStart, LocalDate.now()).first()
        val weeklySeconds = weekSessions.sumOf { it.durationSeconds }
        
        // Calculate all time total
        val allSessions = repository.getAllStudySessions().first()
        val allTimeSeconds = allSessions.sumOf { it.durationSeconds }
        
        // Calculate average study time (last 7 days)
        val avgSeconds = weeklySeconds / 7
        avgStudyTime = "${avgSeconds / 3600}:${((avgSeconds % 3600) / 60).toString().padStart(2, '0')}"
        
        // Format all time values
        monthlyStudyHours = "${monthlySeconds / 3600}:${((monthlySeconds % 3600) / 60).toString().padStart(2, '0')}"
        weeklyStudyHours = "${weeklySeconds / 3600}:${((weeklySeconds % 3600) / 60).toString().padStart(2, '0')}"
        allTimeStudyHours = "${allTimeSeconds / 3600}:${((allTimeSeconds % 3600) / 60).toString().padStart(2, '0')}"
    }

    val todayStudyTimeFormatted = remember(todayStudySeconds) {
        DateUtils.formatElapsedTime(todayStudySeconds.toLong())
    }

    // Colors derived from MaterialTheme for dynamic theming
    val darkBackground = MaterialTheme.colorScheme.background
    val cardBackground = MaterialTheme.colorScheme.surface
    val goldColor = MaterialTheme.colorScheme.primary // Starts as Gold in Midnight/Dark, can be different in Light if needed
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.secondary
    val accentColor = MaterialTheme.colorScheme.tertiary // Map tertiary to accent

    Scaffold(
        topBar = {
            TopHeader(
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToInsights = onNavigateToInsights,
                onHamburgerClick = onNavigateToMenu,
                showBackButton = false, // Bottom nav is available
                title = "Profile"
            )
        },
        containerColor = darkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Header: Avatar, Name, Achievements
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Gray) // Placeholder
                        ) {
                            // In a real app, use coil to load image. 
                            // Using a placeholder icon or random image if URL unavailable
                             AsyncImage(
                                model = currentUser?.photoUrl ?: "https://i.pravatar.cc/300", // Placeholder internet image
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = currentUser?.displayName ?: "User", // Mock Name
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = goldColor
                            )
                            Text(
                                text = "PROFILE",
                                style = MaterialTheme.typography.labelMedium,
                                color = goldColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Achievements Row (Mock)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                                Icon(Icons.Default.Leaderboard, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                            }
                        }
                        
                         Column(
                            horizontalAlignment = Alignment.End
                        ) {
                             Text(
                                text = "ACHIEVEMENTS",
                                style = MaterialTheme.typography.labelSmall,
                                color = goldColor,
                                fontWeight = FontWeight.Bold
                            )
                             // More achievement icons grid
                              Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top=8.dp)) {
                                 Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                 Icon(Icons.Default.Star, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                             }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Rank Progress
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icons for currency/stats (Mock)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text("🟡 541", color = textColor, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                             Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text("💎 0", color = textColor, fontSize = 12.sp)
                            }
                             Spacer(modifier = Modifier.width(8.dp))
                             Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text("🎁 0", color = textColor, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "UNRANKED",
                            color = goldColor,
                            fontWeight = FontWeight.Bold
                        )
                        
                        LinearProgressIndicator(
                            progress = 0.0f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            color = goldColor
                        )
                        Text(
                            text = "NO RANKS AVAILABLE",
                            color = secondaryTextColor,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Statistics & Voice Streak Card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Statistics Column
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "STATISTICS",
                                color = goldColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text("LEADERBOARD POSITION", color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("USER RANK: #${userRank}", color = secondaryTextColor, fontSize = 13.sp, modifier = Modifier.padding(top=4.dp))
                            Text("ANKI: COMING SOON", color = secondaryTextColor, fontSize = 13.sp, modifier = Modifier.padding(top=4.dp))
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                             Text("STUDY TIME", color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                             Text("DAILY: $todayStudyTimeFormatted", color = secondaryTextColor, fontSize = 13.sp, modifier = Modifier.padding(top=4.dp))
                             Text("AVG: $avgStudyTime", color = secondaryTextColor, fontSize = 13.sp, modifier = Modifier.padding(top=4.dp))
                             Text("MAX: $maxFocusSession", color = secondaryTextColor, fontSize = 13.sp, modifier = Modifier.padding(top=4.dp))
                             Text("SESSIONS: $totalSessionsToday", color = secondaryTextColor, fontSize = 13.sp, modifier = Modifier.padding(top=4.dp))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Streak Calendar Column
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "STUDY STREAK",
                                color = goldColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                             Spacer(modifier = Modifier.height(16.dp))
                             
                             Row(
                                 modifier = Modifier.fillMaxWidth(),
                                 horizontalArrangement = Arrangement.Center
                             ) {
                                 Text(
                                     "${currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
                                     color = textColor,
                                     fontWeight = FontWeight.Bold,
                                     fontSize = 14.sp
                                 )
                             }
                             
                             Spacer(modifier = Modifier.height(12.dp))

                             
                             // Dynamic Calendar Grid
                             val firstDayOfMonth = currentMonth.withDayOfMonth(1)
                             val daysInMonth = currentMonth.lengthOfMonth()
                             val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Sunday
                             
                             val daysHeader = listOf("S", "M", "T", "W", "T", "F", "S")
                             
                             // Lambda to get color based on study hours
                             val getStudyColor: (Int) -> Color = { seconds ->
                                 val hours = seconds / 3600f
                                 when {
                                     hours >= 10 -> Color(0xFF00A86B) // Deep green
                                     hours >= 5 -> Color(0xFF4CAF50) // Medium green
                                     hours >= 1 -> Color(0xFF81C784) // Light green
                                     else -> Color.Transparent // No study
                                 }
                             }

                             Column {
                                 // Header
                                 Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                     daysHeader.forEach { day ->
                                         Text(
                                             day,
                                             color = textColor,
                                             fontWeight = FontWeight.Bold,
                                             fontSize = 12.sp,
                                             modifier = Modifier.width(20.dp),
                                             textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                         )
                                     }
                                 }
                                 Spacer(modifier = Modifier.height(8.dp))
                                 
                                 // Calendar days
                                 var dayCounter = 1
                                 var weekIndex = 0
                                 while (dayCounter <= daysInMonth) {
                                     Row(
                                         modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                         horizontalArrangement = Arrangement.SpaceBetween
                                     ) {
                                         for (dayOfWeek in 0..6) {
                                             val dayIndex = weekIndex * 7 + dayOfWeek
                                             
                                             if ((weekIndex == 0 && dayOfWeek < firstDayOfWeek) || dayCounter > daysInMonth) {
                                                 Spacer(modifier = Modifier.width(20.dp))
                                             } else {
                                                 val studySeconds = monthlyStudyData[dayCounter] ?: 0
                                                 val studyColor = getStudyColor(studySeconds)
                                                 val isToday = dayCounter == currentMonth.dayOfMonth
                                                 
                                                 Box(
                                                     modifier = Modifier
                                                         .width(20.dp)
                                                         .height(20.dp)
                                                         .clip(CircleShape)
                                                         .background(studyColor),
                                                     contentAlignment = Alignment.Center
                                                 ) {
                                                     Text(
                                                         text = dayCounter.toString(),
                                                         color = if (studyColor != Color.Transparent) Color.White else secondaryTextColor,
                                                         fontSize = 10.sp,
                                                         fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                                     )
                                                 }
                                                 dayCounter++
                                             }
                                         }
                                     }
                                     weekIndex++
                                 }
                             }
                        }
                    }
                }
            }
        }
    }
}
