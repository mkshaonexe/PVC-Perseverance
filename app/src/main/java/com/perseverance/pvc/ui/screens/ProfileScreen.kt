package com.perseverance.pvc.ui.screens

import android.text.format.DateUtils
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
    onNavigateToMenu: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { StudyRepository(context) }
    var todayStudySeconds by remember { mutableStateOf(0) }
    var weeklyStudyHours by remember { mutableStateOf(0.0) }
    var userRank by remember { mutableStateOf<String?>("4TH") } 
    var monthlyStudyHours by remember { mutableStateOf("04:10") }
    var allTimeStudyHours by remember { mutableStateOf("04:10") }

    // Fetch study data
    LaunchedEffect(Unit) {
        val todaySeconds = repository.getTodayTotalSeconds().first()
        todayStudySeconds = todaySeconds
        // Dummy data for now for other stats since we might not have them calculated in repo yet or complex logic
        // But we can try to fetch real data if available from repo helpers
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
                                model = "https://i.pravatar.cc/300", // Placeholder internet image
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
                                text = "MK Shaon", // Mock Name
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
                            Text("TIME: ${userRank}", color = secondaryTextColor, fontSize = 13.sp, modifier = Modifier.padding(top=4.dp))
                            Text("ANKI: COMING SOON", color = secondaryTextColor, fontSize = 13.sp, modifier = Modifier.padding(top=4.dp))
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text("STUDY TIME", color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("DAILY: $todayStudyTimeFormatted", color = secondaryTextColor, fontSize = 13.sp, modifier = Modifier.padding(top=4.dp))
                            Text("WEEKLY: $weeklyStudyHours", color = secondaryTextColor, fontSize = 13.sp, modifier = Modifier.padding(top=4.dp))
                            Text("MONTHLY: $monthlyStudyHours", color = secondaryTextColor, fontSize = 13.sp, modifier = Modifier.padding(top=4.dp))
                            Text("ALL TIME: $allTimeStudyHours", color = secondaryTextColor, fontSize = 13.sp, modifier = Modifier.padding(top=4.dp))
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
                                 horizontalArrangement = Arrangement.SpaceBetween
                             ) {
                                 Text("DECEMBER: 4 hours", color = accentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                 Text("2025", color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                             }
                             
                             Spacer(modifier = Modifier.height(12.dp))

                             // Simple Calendar Grid Mockup
                             val weeks = listOf(
                                 listOf("", "1", "2", "3", "4", "5", "6"),
                                 listOf("7", "8", "9", "10", "11", "12", "13"),
                                 listOf("14", "15", "16", "17", "18", "19", "20"),
                                 listOf("21", "22", "23", "24", "25", "26", "27"),
                                 listOf("28", "29", "30", "31", "", "", "")
                             )
                             val daysHeader = listOf("S", "M", "T", "W", "T", "F", "S")

                             Column {
                                 // Header
                                 Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                     daysHeader.forEach { day ->
                                         Text(day, color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(20.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                     }
                                 }
                                 Spacer(modifier = Modifier.height(8.dp))
                                 // Days
                                 weeks.forEach { week ->
                                     Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                         week.forEach { date ->
                                              if (date.isNotEmpty()) {
                                                  val isSelected = date == "25" || date == "27" // Mock selected days
                                                  Box(
                                                      modifier = Modifier
                                                          .width(20.dp)
                                                          .height(20.dp)
                                                          .clip(CircleShape)
                                                          .background(if(isSelected) accentColor else Color.Transparent),
                                                      contentAlignment = Alignment.Center
                                                  ) {
                                                      Text(
                                                          text = date, 
                                                          color = if (isSelected) Color.White else secondaryTextColor, 
                                                          fontSize = 12.sp
                                                      )
                                                  }
                                              } else {
                                                  Spacer(modifier = Modifier.width(20.dp))
                                              }
                                         }
                                     }
                                 }
                             }
                        }
                    }
                }
            }
        }
    }
}
