package com.perseverance.pvc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.perseverance.pvc.data.StudyChartData
import com.perseverance.pvc.data.SubjectStudyTime

@Composable
fun StudyTimeChart(
    chartData: StudyChartData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Text(
                text = "Daily Study Time",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Chart
            if (chartData.dailyData.isNotEmpty()) {
                val latestDay = chartData.dailyData.last()
                StudyTimeBarChart(
                    subjects = latestDay.subjects,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Subject legend
                SubjectLegend(
                    subjects = latestDay.subjects,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No study data available",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StudyTimeBarChart(
    subjects: List<SubjectStudyTime>,
    modifier: Modifier = Modifier
) {
    val maxTime = subjects.maxOfOrNull { it.totalMinutes } ?: 1
    val colors = listOf(
        Color(0xFFFF6B6B), // Red
        Color(0xFF4ECDC4), // Teal
        Color(0xFF45B7D1), // Blue
        Color(0xFF96CEB4), // Green
        Color(0xFFFECA57), // Yellow
        Color(0xFFFF9FF3), // Pink
        Color(0xFF54A0FF), // Light Blue
        Color(0xFF5F27CD)  // Purple
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
                
                // Draw bar
                drawRect(
                    color = colors[index % colors.size],
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight)
                )
            }
        }
        
        // Draw labels on top of bars using Compose Text
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            subjects.forEachIndexed { index, subject ->
                val maxTime = subjects.maxOfOrNull { it.totalMinutes } ?: 1
                val barHeight = (subject.totalMinutes.toFloat() / maxTime) * 0.8f
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "${subject.totalMinutes}m",
                        color = MaterialTheme.colorScheme.onSurface,
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
private fun SubjectLegend(
    subjects: List<SubjectStudyTime>,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(0xFFFF6B6B), // Red
        Color(0xFF4ECDC4), // Teal
        Color(0xFF45B7D1), // Blue
        Color(0xFF96CEB4), // Green
        Color(0xFFFECA57), // Yellow
        Color(0xFFFF9FF3), // Pink
        Color(0xFF54A0FF), // Light Blue
        Color(0xFF5F27CD)  // Purple
    )
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        subjects.forEachIndexed { index, subject ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "${subject.totalMinutes}m",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
