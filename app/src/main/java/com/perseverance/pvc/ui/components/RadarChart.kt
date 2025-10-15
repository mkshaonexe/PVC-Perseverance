package com.perseverance.pvc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

data class SubjectRadarData(
    val subject: String,
    val minutes: Int
)

@Composable
fun RadarChart(
    subjects: List<SubjectRadarData>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E1E1E)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Top Subjects",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (subjects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No study data",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    RadarChartCanvas(
                        subjects = subjects,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun RadarChartCanvas(
    subjects: List<SubjectRadarData>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = minOf(size.width, size.height) / 2.8f
        
        val numAxes = 6
        val maxValue = subjects.maxOfOrNull { it.minutes }?.toFloat() ?: 1f
        
        // Fill subjects to 6
        val displaySubjects = subjects.take(6).toMutableList()
        while (displaySubjects.size < 6) {
            displaySubjects.add(SubjectRadarData("", 0))
        }
        
        // Draw concentric polygons (grid)
        val gridLevels = 5
        for (level in 1..gridLevels) {
            val levelRadius = radius * (level.toFloat() / gridLevels)
            val gridPath = Path()
            
            for (i in 0 until numAxes) {
                val angle = (PI / 2 - i * 2 * PI / numAxes).toFloat()
                val x = centerX + levelRadius * cos(angle)
                val y = centerY - levelRadius * sin(angle)
                
                if (i == 0) {
                    gridPath.moveTo(x, y)
                } else {
                    gridPath.lineTo(x, y)
                }
            }
            gridPath.close()
            
            drawPath(
                path = gridPath,
                color = Color.White.copy(alpha = 0.1f),
                style = Stroke(width = 1f)
            )
        }
        
        // Draw axis lines
        for (i in 0 until numAxes) {
            val angle = (PI / 2 - i * 2 * PI / numAxes).toFloat()
            val endX = centerX + radius * cos(angle)
            val endY = centerY - radius * sin(angle)
            
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(centerX, centerY),
                end = Offset(endX, endY),
                strokeWidth = 1f
            )
        }
        
        // Draw data polygon
        val dataPath = Path()
        val points = mutableListOf<Offset>()
        
        for (i in 0 until numAxes) {
            val subjectData = displaySubjects[i]
            val value = if (maxValue > 0) (subjectData.minutes / maxValue) else 0f
            val distance = radius * value.coerceIn(0f, 1f)
            
            val angle = (PI / 2 - i * 2 * PI / numAxes).toFloat()
            val x = centerX + distance * cos(angle)
            val y = centerY - distance * sin(angle)
            
            points.add(Offset(x, y))
            
            if (i == 0) {
                dataPath.moveTo(x, y)
            } else {
                dataPath.lineTo(x, y)
            }
        }
        dataPath.close()
        
        // Fill
        drawPath(
            path = dataPath,
            color = Color(0xFF9370DB).copy(alpha = 0.4f)
        )
        
        // Outline
        drawPath(
            path = dataPath,
            color = Color(0xFF9370DB),
            style = Stroke(width = 3f)
        )
        
        // Points
        points.forEach { point ->
            drawCircle(
                color = Color(0xFF9370DB),
                radius = 6f,
                center = point
            )
        }
        
        // Labels using native canvas
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 30f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        
        val minutesPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(153, 255, 255, 255)
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        
        displaySubjects.forEachIndexed { index, subject ->
            if (subject.subject.isNotEmpty()) {
                val angle = (PI / 2 - index * 2 * PI / numAxes).toFloat()
                val labelRadius = radius * 1.25f
                
                val labelX = centerX + labelRadius * cos(angle)
                val labelY = centerY - labelRadius * sin(angle)
                
                drawContext.canvas.nativeCanvas.drawText(
                    subject.subject.take(8),
                    labelX,
                    labelY - 5f,
                    textPaint
                )
                
                drawContext.canvas.nativeCanvas.drawText(
                    "${subject.minutes}m",
                    labelX,
                    labelY + 22f,
                    minutesPaint
                )
            }
        }
    }
}

