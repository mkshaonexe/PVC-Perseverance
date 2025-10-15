package com.perseverance.pvc.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.PlayArrow
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
import com.perseverance.pvc.ui.components.VideoBackground
import com.perseverance.pvc.ui.components.StudyTimeChart
import com.perseverance.pvc.ui.theme.PerseverancePVCTheme
import com.perseverance.pvc.ui.components.AnalogClock
import com.perseverance.pvc.ui.viewmodel.StudyViewModel

@Composable
fun Page2Screen() {
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
        // Video background
        VideoBackground(
            isPlaying = uiState.isTimerRunning,
            modifier = Modifier.fillMaxSize()
        )
        
        // Semi-transparent overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hamburger menu (3 lines) in top left
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Column(
                    modifier = Modifier.padding(start = 0.dp, top = 8.dp)
                ) {
                    // First line
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(3.dp)
                            .background(Color.White)
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    // Second line
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(3.dp)
                            .background(Color.White)
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    // Third line
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(3.dp)
                            .background(Color.White)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Timer section with circular clock and remaining time
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        text = "Remaining time",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = uiState.timerDisplay,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
                        text = "${uiState.completedSessions}:00:00",
                        fontSize = 16.sp,
                        color = Color(0xFFFF8C42)
                    )
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
                        color = Color.White,
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
            
            // Bottom navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📊",
                        fontSize = 24.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Home",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📚",
                        fontSize = 24.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Books",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⋯",
                        fontSize = 24.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "More",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun StudyTimeChartWithScrollableLegend(
    chartData: com.perseverance.pvc.data.StudyChartData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Fixed header and chart (non-scrollable)
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Daily Study Time",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (chartData.dailyData.isNotEmpty()) {
                    val latestDay = chartData.dailyData.last()
                    
                    // Bar chart (fixed)
                    StudyTimeBarChartOnly(
                        subjects = latestDay.subjects,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No study data available",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            // Scrollable legend
            if (chartData.dailyData.isNotEmpty()) {
                val latestDay = chartData.dailyData.last()
                val scrollState = rememberScrollState()
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val colors = listOf(
                        Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFF45B7D1),
                        Color(0xFF96CEB4), Color(0xFFFECA57), Color(0xFFFF9FF3),
                        Color(0xFF54A0FF), Color(0xFF5F27CD)
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
                                        .size(12.dp)
                                        .background(
                                            color = colors[index % colors.size],
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = subject.subject,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = "${subject.totalMinutes}m",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
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
        Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFF45B7D1),
        Color(0xFF96CEB4), Color(0xFFFECA57), Color(0xFFFF9FF3),
        Color(0xFF54A0FF), Color(0xFF5F27CD)
    )
    
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barWidth = size.width / subjects.size * 0.8f
            val spacing = size.width / subjects.size * 0.2f
            val maxHeight = size.height * 0.8f
            
            subjects.forEachIndexed { index, subject ->
                val x = index * (barWidth + spacing) + spacing / 2
                val barHeight = (subject.totalMinutes.toFloat() / maxTime) * maxHeight
                val y = size.height - barHeight
                
                drawRect(
                    color = colors[index % colors.size],
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight)
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            subjects.forEachIndexed { index, subject ->
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "${subject.totalMinutes}m",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
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
                    color = Color.White.copy(alpha = 0.8f),
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
        color = Color(0xFF2B2B2B),
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
    PerseverancePVCTheme {
        Page2Screen()
    }
}
