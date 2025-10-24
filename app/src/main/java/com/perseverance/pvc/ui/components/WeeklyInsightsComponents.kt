package com.perseverance.pvc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import com.perseverance.pvc.data.WeekStudyData
import com.perseverance.pvc.data.WeeklyChartData
import com.perseverance.pvc.data.SubjectWeeklyStats
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeeklySummaryCard(
    weekData: WeekStudyData,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() >= 0.5f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isLightTheme) 2.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Week navigation header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousWeek) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous week",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Text(
                    text = "${weekData.weekStartDate.dayOfMonth} ${weekData.weekStartDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} - ${weekData.weekEndDate.dayOfMonth} ${weekData.weekEndDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                IconButton(onClick = onNextWeek) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next week",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Main statistics
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
                        text = formatMinutesToHMS(weekData.totalStudyMinutes),
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Study Days
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Study Days",
                        fontSize = 14.sp,
                        color = Color(0xFFFF8C42),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${weekData.studyDays}/7",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Average study time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Daily Average",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatMinutesToHMS(weekData.averageStudyMinutesPerDay.toInt()),
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Hours",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = String.format("%.1f", weekData.totalStudyHours),
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectBreakdownCard(
    subjects: List<SubjectWeeklyStats>
) {
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() >= 0.5f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isLightTheme) 2.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Subject Breakdown",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            subjects.forEach { subject ->
                SubjectBreakdownItem(
                    subject = subject,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SubjectBreakdownItem(
    subject: SubjectWeeklyStats,
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
                text = "${subject.studyDays} days • ${String.format("%.1f", subject.averageMinutesPerDay)} min/day avg",
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

@Composable
fun WeeklyStudyChart(
    chartData: WeeklyChartData
) {
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() >= 0.5f
    val maxMinutes = chartData.chartPoints.maxOfOrNull { it.totalMinutes } ?: 1
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isLightTheme) 2.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Weekly Study Chart",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Chart bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                chartData.chartPoints.forEach { point ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Bar
                        val barHeight = if (maxMinutes > 0) {
                            (point.totalMinutes.toFloat() / maxMinutes * 120).dp
                        } else 0.dp
                        
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(barHeight)
                                .background(
                                    Color(0xFFFF8C42),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Day label
                        Text(
                            text = point.day,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        
                        // Minutes label
                        Text(
                            text = "${point.totalMinutes}m",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
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
