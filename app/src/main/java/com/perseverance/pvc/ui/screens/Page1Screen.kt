package com.perseverance.pvc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.perseverance.pvc.ui.components.VideoBackground
import com.perseverance.pvc.ui.theme.PerseverancePVCTheme
import com.perseverance.pvc.ui.viewmodel.InsightsViewModel
import com.perseverance.pvc.ui.viewmodel.PeriodType
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun Page1Screen() {
    val context = LocalContext.current
    val viewModel: InsightsViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Video background
        VideoBackground(
            isPlaying = false,
            modifier = Modifier.fillMaxSize()
        )
        
        // Semi-transparent overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            // Title
            Text(
                text = "Insights",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Period selection buttons
            PeriodSelector(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodSelected = { viewModel.selectPeriod(it) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Calendar Section
            CalendarView(
                currentMonth = uiState.currentMonth,
                selectedDate = uiState.selectedDate,
                monthDays = uiState.monthDays,
                onDateSelected = { viewModel.selectDate(it) },
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Selected Day Details
            DayDetailsSection(
                selectedDate = uiState.selectedDate,
                dayData = uiState.selectedDayData
            )
        }
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: PeriodType,
    onPeriodSelected: (PeriodType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PeriodType.values().forEach { period ->
            val isSelected = period == selectedPeriod
            Button(
                onClick = { onPeriodSelected(period) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Color(0xFFFF8C42) else Color(0xFF2C2C2C),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text(
                    text = period.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun CalendarView(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    monthDays: Map<LocalDate, com.perseverance.pvc.ui.viewmodel.DayStudyData>,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF2C2C2C)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Month header with navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous month",
                        tint = Color.White
                    )
                }
                
                Text(
                    text = currentMonth.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next month",
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Day of week headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                    Text(
                        text = day,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Calendar grid
            CalendarGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                monthDays = monthDays,
                onDateSelected = onDateSelected
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Color legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    "0+" to Color(0xFF4A4A4A),
                    "4+" to Color(0xFF6B4E3D),
                    "7+" to Color(0xFF8B5A3C),
                    "10+" to Color(0xFFB87333),
                    "12+" to Color(0xFFFF8C42)
                ).forEach { (label, color) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(color, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    monthDays: Map<LocalDate, com.perseverance.pvc.ui.viewmodel.DayStudyData>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val lastDayOfMonth = currentMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = currentMonth.lengthOfMonth()
    
    Column {
        var dayCounter = 1
        repeat(6) { week ->
            if (dayCounter > daysInMonth) return@repeat
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(7) { dayOfWeek ->
                    val dayIndex = week * 7 + dayOfWeek
                    
                    if (dayIndex < firstDayOfWeek || dayCounter > daysInMonth) {
                        Box(modifier = Modifier.weight(1f))
                    } else {
                        val date = currentMonth.atDay(dayCounter)
                        val dayData = monthDays[date]
                        CalendarDayCell(
                            day = dayCounter,
                            isSelected = date == selectedDate,
                            studyData = dayData,
                            onSelected = { onDateSelected(date) },
                            modifier = Modifier.weight(1f)
                        )
                        dayCounter++
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun CalendarDayCell(
    day: Int,
    isSelected: Boolean,
    studyData: com.perseverance.pvc.ui.viewmodel.DayStudyData?,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> Color(0xFF8B5A3C)
        studyData != null -> {
            val minutes = studyData.totalSeconds / 60
            when {
                minutes >= 12 -> Color(0xFFFF8C42)
                minutes >= 10 -> Color(0xFFB87333)
                minutes >= 7 -> Color(0xFF8B5A3C)
                minutes >= 4 -> Color(0xFF6B4E3D)
                else -> Color(0xFF4A4A4A)
            }
        }
        else -> Color.Transparent
    }
    
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable { onSelected() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day.toString(),
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (studyData != null) {
            val hours = studyData.totalSeconds / 3600
            val minutes = (studyData.totalSeconds % 3600) / 60
            val timeText = if (hours > 0) "$hours:${minutes.toString().padStart(2, '0')}" else "0:${minutes.toString().padStart(2, '0')}"
            Text(
                text = timeText,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun DayDetailsSection(
    selectedDate: LocalDate,
    dayData: com.perseverance.pvc.ui.viewmodel.DayStudyData?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF2C2C2C)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())}, ${selectedDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${selectedDate.dayOfMonth}",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Total
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total",
                        fontSize = 14.sp,
                        color = Color(0xFFFF8C42),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatSecondsToHMS(dayData?.totalSeconds ?: 0),
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Max Focus
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Max Focus",
                        fontSize = 14.sp,
                        color = Color(0xFFFF8C42),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatSecondsToHMS(dayData?.maxFocusSeconds ?: 0),
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Started
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Started",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = dayData?.startTime ?: "--",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
                
                // Finished
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Finished",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = dayData?.endTime ?: "--",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

fun formatSecondsToHMS(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%d:%02d:%02d", hours, minutes, secs)
}

@Preview(showBackground = true)
@Composable
fun Page1ScreenPreview() {
    PerseverancePVCTheme {
        Page1Screen()
    }
}
