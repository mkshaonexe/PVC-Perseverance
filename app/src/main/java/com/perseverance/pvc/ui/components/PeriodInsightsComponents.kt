package com.perseverance.pvc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.perseverance.pvc.data.TodayStudyData
import com.perseverance.pvc.data.PeriodInsights
import com.perseverance.pvc.data.SubjectTodayStats
import com.perseverance.pvc.ui.theme.glassBorder
import com.perseverance.pvc.ui.theme.glassElevation
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TodaySummaryCard(
    todayData: TodayStudyData
) {
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
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Today's date
            Text(
                text = "Today, ${todayData.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Main statistics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Total Study Time
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Study Time",
                        fontSize = 14.sp,
                        color = Color(0xFFFF8C42),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatMinutesToHMS(todayData.totalStudyMinutes),
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Sessions Count
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Sessions",
                        fontSize = 14.sp,
                        color = Color(0xFFFF8C42),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${todayData.sessionCount}",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Secondary statistics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Average Session Length
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Avg Session",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatMinutesToHMS(todayData.averageSessionLength.toInt()),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Longest Session
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Longest",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatMinutesToHMS(todayData.longestSession),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Study Hours
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Hours",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = String.format("%.1f", todayData.totalStudyHours),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Study time range
            if (todayData.firstSessionTime != null && todayData.lastSessionTime != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Started",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = todayData.firstSessionTime,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Finished",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = todayData.lastSessionTime,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodInsightsCard(
    insights: PeriodInsights
) {
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
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Today's Insights",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Progress metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Daily Streak
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Daily Streak",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${insights.dailyStreak} days",
                        fontSize = 20.sp,
                        color = Color(0xFFFF8C42),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Productivity Score
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Productivity",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${insights.productivityScore}%",
                        fontSize = 20.sp,
                        color = getProductivityColor(insights.productivityScore),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Weekly Progress
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weekly Progress",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${String.format("%.1f", insights.weeklyProgress)}%",
                        fontSize = 16.sp,
                        color = Color(0xFFFF8C42),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress bar
                LinearProgressIndicator(
                    progress = (insights.weeklyProgress / 100).toFloat().coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFFFF8C42),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun TodaySubjectBreakdown(
    subjects: List<SubjectTodayStats>
) {
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
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Today's Subjects",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            subjects.forEach { subject ->
                TodaySubjectItem(
                    subject = subject,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun TodaySubjectItem(
    subject: SubjectTodayStats,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subject.subject,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${subject.sessionCount} sessions • ${String.format("%.1f", subject.averageSessionLength)} min avg",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatMinutesToHMS(subject.totalMinutes),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF8C42)
            )
            Text(
                text = "${String.format("%.1f", subject.percentageOfTotal)}%",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

private fun formatMinutesToHMS(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) {
        String.format("%d:%02d", hours, mins)
    } else {
        "${mins}m"
    }
}

private fun getProductivityColor(score: Int): Color {
    return when {
        score >= 80 -> Color(0xFF4CAF50) // Green
        score >= 60 -> Color(0xFFFF9800) // Orange
        score >= 40 -> Color(0xFFFF5722) // Red-Orange
        else -> Color(0xFFF44336) // Red
    }
}
