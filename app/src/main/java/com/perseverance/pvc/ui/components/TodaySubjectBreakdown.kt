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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.perseverance.pvc.data.SubjectTodayStats
import com.perseverance.pvc.ui.theme.glassBorder
import com.perseverance.pvc.ui.theme.glassElevation
import kotlin.math.*

@Composable
fun TodaySubjectBreakdownComponent(
    subjects: List<SubjectTodayStats>
) {
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() >= 0.5f
    
    if (subjects.isEmpty()) return
    
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
                text = "Today's Study Breakdown",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Pie Chart
                PieChartWithText(
                    subjects = subjects,
                    modifier = Modifier
                        .size(200.dp)
                        .weight(1f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Legend
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjects.forEach { subject ->
                        SubjectLegendItem(
                            subject = subject,
                            color = getSubjectColor(subject.subject, subjects.size)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PieChartWithText(
    subjects: List<SubjectTodayStats>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        PieChart(
            subjects = subjects,
            modifier = Modifier.fillMaxSize()
        )
        
        // Center text overlay
        val totalMinutes = subjects.sumOf { it.totalMinutes }
        val totalHours = totalMinutes / 60
        val totalMins = totalMinutes % 60
        val timeText = if (totalHours > 0) {
            "${totalHours}:${totalMins.toString().padStart(2, '0')}"
        } else {
            "${totalMins}m"
        }
        
        Text(
            text = timeText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PieChart(
    subjects: List<SubjectTodayStats>,
    modifier: Modifier = Modifier
) {
    val totalMinutes = subjects.sumOf { it.totalMinutes }
    if (totalMinutes == 0) return
    
    var startAngle = -90f // Start from top
    
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = minOf(size.width, size.height) / 2 - 20f
        
        subjects.forEach { subject ->
            val sweepAngle = (subject.totalMinutes.toFloat() / totalMinutes) * 360f
            
            if (sweepAngle > 0) {
                drawArc(
                    color = getSubjectColor(subject.subject, subjects.size),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
                
                startAngle += sweepAngle
            }
        }
        
        // Draw center circle for text background
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = 30f,
            center = center
        )
    }
}

@Composable
private fun SubjectLegendItem(
    subject: SubjectTodayStats,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Color indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Subject info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subject.subject,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${subject.sessionCount} sessions",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        // Time and percentage
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatMinutesToHMS(subject.totalMinutes),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF8C42)
            )
            Text(
                text = "${String.format("%.0f", subject.percentageOfTotal)}%",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

private fun getSubjectColor(subject: String, totalSubjects: Int): Color {
    val colors = listOf(
        Color(0xFF4CAF50), // Green
        Color(0xFF2196F3), // Blue
        Color(0xFFFF9800), // Orange
        Color(0xFF9C27B0), // Purple
        Color(0xFFF44336), // Red
        Color(0xFF00BCD4), // Cyan
        Color(0xFFFFEB3B), // Yellow
        Color(0xFF795548), // Brown
        Color(0xFF607D8B), // Blue Grey
        Color(0xFFE91E63)  // Pink
    )
    
    val index = subject.hashCode().mod(colors.size)
    return colors[index]
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
