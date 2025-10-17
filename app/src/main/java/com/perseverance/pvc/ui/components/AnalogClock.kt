package com.perseverance.pvc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.luminance
import kotlinx.coroutines.delay

@Composable
fun AnalogClock(
    modifier: Modifier = Modifier,
    tickColor: Color = Color.Unspecified,
    handColor: Color = Color.Unspecified // default from theme
) {
    var seconds by remember { mutableStateOf(0) }

    // Update the seconds every second based on system clock
    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            seconds = ((now / 1000) % 60).toInt()
            delay(1000)
        }
    }

    val isLightTheme = MaterialTheme.colorScheme.background.luminance() >= 0.5f
    val effectiveTick = if (tickColor == Color.Unspecified) {
        if (isLightTheme) Color(0xFF9E9E9E) else Color.White.copy(alpha = 0.6f)
    } else tickColor
    val ringColor = if (isLightTheme) Color(0xFFBDBDBD) else Color.White.copy(alpha = 0.25f)
    val effectiveHand = if (handColor == Color.Unspecified) Color(0xFFFFA000) else handColor

    Canvas(modifier = modifier) {
        val diameter = size.minDimension
        val radius = diameter / 2f

        // Outer circle
        drawCircle(
            color = ringColor,
            radius = radius,
            style = Stroke(width = 3.dp.toPx())
        )

        // Tick marks (60 small, with larger every 5 seconds)
        val center = Offset(x = size.width / 2f, y = size.height / 2f)
        repeat(60) { i ->
            val angle = i * 6f // 360 / 60
            val isMajor = i % 5 == 0
            val tickLength = if (isMajor) radius * 0.14f else radius * 0.08f
            val strokeWidth = if (isMajor) 3.dp.toPx() else 2.dp.toPx()

            val start = Offset(
                x = center.x,
                y = center.y - radius + strokeWidth
            )
            val end = Offset(
                x = center.x,
                y = center.y - radius + strokeWidth + tickLength
            )
            rotate(degrees = angle, pivot = center) {
                drawLine(
                    color = effectiveTick,
                    start = start,
                    end = end,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }

        // Second hand
        val secondAngle = seconds * 6f
        val handEnd = Offset(
            x = center.x,
            y = center.y - radius * 0.75f
        )
        rotate(degrees = secondAngle, pivot = center) {
            drawLine(
                color = effectiveHand,
                start = center,
                end = handEnd,
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Center dot
        drawCircle(
            color = effectiveHand,
            radius = 3.dp.toPx(),
            center = center
        )
    }
}


