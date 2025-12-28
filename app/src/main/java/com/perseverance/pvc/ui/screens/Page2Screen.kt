package com.perseverance.pvc.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Insights
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.perseverance.pvc.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.luminance
import com.perseverance.pvc.ui.components.StudyTimeChart
import com.perseverance.pvc.ui.theme.PerseverancePVCTheme
import com.perseverance.pvc.ui.theme.glassBorder
import com.perseverance.pvc.ui.theme.glassElevation
import com.perseverance.pvc.ui.theme.isLightTheme
import com.perseverance.pvc.ui.components.AnalogClock
import com.perseverance.pvc.ui.components.TopHeader
import com.perseverance.pvc.ui.viewmodel.StudyViewModel

@Composable
fun Page2Screen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {}
) {
    val context = LocalContext.current
    val studyViewModel: StudyViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application
        )
    )
    val uiState by studyViewModel.uiState.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Simple background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
        
        // Overlay removed to match Group screen background

        
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top header with hamburger menu and settings/insights icons
            TopHeader(
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToInsights = onNavigateToInsights,
                onHamburgerClick = onNavigateToMenu
            )
            
            // Main content with padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Timer section with circular clock and remaining time
                val isLightTheme = MaterialTheme.colorScheme.background.luminance() >= 0.5f
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = glassBorder(isLightTheme),
                    elevation = glassElevation(isLightTheme)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Analog clock drawn in code
                        AnalogClock(
                            modifier = Modifier.size(100.dp)
                        )
                        
                        // Remaining time display
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Pomodoro Timer",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = uiState.timerDisplay,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Inline controls directly under time to match reference
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (uiState.isTimerRunning) {
                                    ControlIconButton(
                                        icon = Icons.Filled.Pause,
                                        onClick = { studyViewModel.pauseTimer() }
                                    )
                                } else {
                                    ControlIconButton(
                                        icon = Icons.Filled.PlayArrow,
                                        onClick = { studyViewModel.startTimer() }
                                    )
                                }
                                ControlIconButton(
                                    icon = Icons.Filled.Stop,
                                    onClick = { studyViewModel.resetTimer() }
                                )
                            }
                        }
                        
                        // Reference image on the right
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.hello_world_2_ref),
                                contentDescription = "Reference icon",
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
							text = uiState.totalStudyTimeDisplay,
                                fontSize = 16.sp,
                                color = Color(0xFFFF8C42)
                            )
                        }
                    }
                }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Today's Total Study Time - Green marked box
            TodayTotalStudyTimeBox(
                totalStudyTime = uiState.totalStudyTimeDisplay
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Study Time Chart (fixed - non-scrollable part)
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                StudyTimeChartWithScrollableLegend(
                    chartData = uiState.chartData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun StudyTimeChartWithScrollableLegend(
    chartData: com.perseverance.pvc.data.StudyChartData,
    modifier: Modifier = Modifier
) {
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() >= 0.5f
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp), // More rounded corners
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = glassBorder(isLightTheme) // Restore border for visibility
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "Daily Study Time",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 0.5.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (chartData.dailyData.isNotEmpty() && chartData.dailyData.last().subjects.isNotEmpty()) {
                    val latestDay = chartData.dailyData.last()
                    
                    // Bar chart
                    StudyTimeBarChartOnly(
                        subjects = latestDay.subjects,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp) // Slightly taller
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No study data today",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            // Scrollable legend
            if (chartData.dailyData.isNotEmpty() && chartData.dailyData.last().subjects.isNotEmpty()) {
                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                val latestDay = chartData.dailyData.last()
                val scrollState = rememberScrollState()
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val colors = listOf(
                        Color(0xFF6C5CE7), // Purple
                        Color(0xFF00CEC9), // Teal
                        Color(0xFF0984E3), // Blue
                        Color(0xFFFD79A8), // Pink
                        Color(0xFFFF7675), // Salmon
                        Color(0xFFFFA502), // Orange
                        Color(0xFF2ED573), // Green
                        Color(0xFFA29BFE)  // Lavender
                    )
                    
                    latestDay.subjects.forEachIndexed { index, subject ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            color = colors[index % colors.size],
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = subject.subject,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "${subject.totalMinutes}m",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudyTimeBarChartOnly(
    subjects: List<com.perseverance.pvc.data.SubjectStudyTime>,
    modifier: Modifier = Modifier
) {
    val maxTime = subjects.maxOfOrNull { it.totalMinutes } ?: 1
    val colors = listOf(
        Color(0xFF6C5CE7), // Purple
        Color(0xFF00CEC9), // Teal
        Color(0xFF0984E3), // Blue
        Color(0xFFFD79A8), // Pink
        Color(0xFFFF7675), // Salmon
        Color(0xFFFFA502), // Orange
        Color(0xFF2ED573), // Green
        Color(0xFFA29BFE)  // Lavender
    )
    
    Column(modifier = modifier) {
        // Chart Area (Bars + Time Labels)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Take up remaining space leaving room for labels
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Give some padding for the text at the top
                val topPadding = 24.dp.toPx()
                val availableHeight = size.height - topPadding
                
                // Adjust bar width logic
                val barWidth = size.width / subjects.size * 0.5f 
                val spacing = size.width / subjects.size * 0.5f
                
                subjects.forEachIndexed { index, subject ->
                    val x = index * (barWidth + spacing) + spacing / 2
                    val ratio = if(subject.totalMinutes > 0) 
                        subject.totalMinutes.toFloat() / maxTime 
                    else 0f
                    
                    val barHeight = ratio * availableHeight
                    
                    // Draw bar with rounded top corners
                    if (barHeight > 0) {
                        drawRoundRect(
                            color = colors[index % colors.size],
                            topLeft = Offset(x, size.height - barHeight),
                            size = Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }
                }
            }
            
            // Time Labels Overlay
            Row(
                 modifier = Modifier.fillMaxSize()
            ) {
                 subjects.forEachIndexed { index, subject ->
                     val ratio = if(maxTime > 0) subject.totalMinutes.toFloat() / maxTime else 0f
                     
                     Box(
                         modifier = Modifier
                             .weight(1f)
                             .fillMaxHeight(),
                         contentAlignment = Alignment.BottomCenter
                     ) {
                         Column(
                             modifier = Modifier.fillMaxSize(),
                             verticalArrangement = Arrangement.Bottom,
                             horizontalAlignment = Alignment.CenterHorizontally
                         ) {
                             // Push down to just above the bar
                             Spacer(modifier = Modifier.weight(1f - ratio + 0.001f)) 
                             
                             Text(
                                 text = "${subject.totalMinutes}m",
                                 color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                 fontSize = 11.sp,
                                 fontWeight = FontWeight.Bold,
                                 textAlign = TextAlign.Center,
                                 modifier = Modifier.padding(bottom = 4.dp)
                             )
                             
                             // Occupy the bar's height
                             Spacer(modifier = Modifier.weight(ratio + 0.001f)) 
                         }
                     }
                 }
            }
        }
        
        // Subject Names Row (X-Axis Labels)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            subjects.forEachIndexed { index, subject ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subject.subject,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun TodayTotalStudyTimeBox(
    totalStudyTime: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF4CAF50).copy(alpha = 0.2f) // Green background with transparency
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Study session icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF4CAF50), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📚",
                    fontSize = 24.sp
                )
            }
            
            // Study time display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Today's Total Study Time",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = totalStudyTime,
                    fontSize = 24.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            
            // Green arrow pointing to the time
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF4CAF50), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "→",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ControlIconButton(
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFFA000),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Page2ScreenPreview() {
    PerseverancePVCTheme(themeMode = "Dark") {
        Page2Screen()
    }
}
