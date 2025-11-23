package com.perseverance.pvc.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.perseverance.pvc.data.StudyRepository
import com.perseverance.pvc.data.WeekStudyData
import com.perseverance.pvc.data.WeeklyChartData
import com.perseverance.pvc.data.TodayStudyData
import com.perseverance.pvc.data.PeriodInsights
import com.perseverance.pvc.ui.components.SubjectRadarData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

enum class PeriodType {
    PERIOD, DAY, WEEK, MONTH, TREND
}

data class DayStudyData(
    val date: LocalDate,
    val totalSeconds: Int,
    val maxFocusSeconds: Int,
    val startTime: String?,  // e.g., "AM 8:26"
    val endTime: String?     // e.g., "AM 11:58"
)

data class InsightsUiState(
    val selectedPeriod: PeriodType = PeriodType.PERIOD,
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val monthDays: Map<LocalDate, DayStudyData> = emptyMap(),
    val selectedDayData: DayStudyData? = null,
    val topSubjects: List<SubjectRadarData> = emptyList(),
    val weeklyData: WeekStudyData? = null,
    val weeklyChartData: WeeklyChartData? = null,
    val todayData: TodayStudyData? = null,
    val periodInsights: PeriodInsights? = null,
    val isLoading: Boolean = false
)

class InsightsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StudyRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()
    
    init {
        loadMonthData()
        // Load period data by default since PERIOD is the default selected period
        loadPeriodData()
    }
    
    fun selectPeriod(period: PeriodType) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        
        // Load data based on selected period
        when (period) {
            PeriodType.PERIOD -> loadPeriodData()
            PeriodType.WEEK -> loadWeeklyData()
            else -> {
                // Clear period and weekly data when switching to other periods
                _uiState.value = _uiState.value.copy(
                    todayData = null,
                    periodInsights = null,
                    weeklyData = null,
                    weeklyChartData = null
                )
            }
        }
    }
    
    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        loadDayDetails(date)
    }
    
    fun previousMonth() {
        val newMonth = _uiState.value.currentMonth.minusMonths(1)
        _uiState.value = _uiState.value.copy(currentMonth = newMonth)
        loadMonthData()
    }
    
    fun nextMonth() {
        val newMonth = _uiState.value.currentMonth.plusMonths(1)
        _uiState.value = _uiState.value.copy(currentMonth = newMonth)
        loadMonthData()
    }
    
    private fun loadMonthData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val month = _uiState.value.currentMonth
            val startDate = month.atDay(1)
            val endDate = month.atEndOfMonth()
            
            repository.getStudySessionsInRange(startDate, endDate).collect { sessions ->
                val dayDataMap = sessions
                    .groupBy { it.startTime?.toLocalDate() }
                    .mapNotNull { (date, daySessions) ->
                        if (date == null) return@mapNotNull null
                        
                        val totalSeconds = daySessions.sumOf { it.durationSeconds }
                        val maxFocusSeconds = daySessions.maxOfOrNull { it.durationSeconds } ?: 0
                        
                        val sortedSessions = daySessions.sortedBy { it.startTime }
                        val startTime = sortedSessions.firstOrNull()?.startTime?.let { formatTime(it.hour, it.minute) }
                        val endTime = sortedSessions.lastOrNull()?.endTime?.let { formatTime(it.hour, it.minute) }
                        
                        date to DayStudyData(
                            date = date,
                            totalSeconds = totalSeconds,
                            maxFocusSeconds = maxFocusSeconds,
                            startTime = startTime,
                            endTime = endTime
                        )
                    }
                    .toMap()
                
                _uiState.value = _uiState.value.copy(
                    monthDays = dayDataMap,
                    isLoading = false
                )
                
                // Load selected day details
                loadDayDetails(_uiState.value.selectedDate)
            }
        }
    }
    
    private fun loadDayDetails(date: LocalDate) {
        val dayData = _uiState.value.monthDays[date]
        _uiState.value = _uiState.value.copy(selectedDayData = dayData)
        
        // Load top subjects for selected day
        loadTopSubjects(date)
    }
    
    private fun loadTopSubjects(date: LocalDate) {
        viewModelScope.launch {
            repository.getStudySessionsByDate(date).collect { sessions ->
                val subjectMinutes = sessions
                    .groupBy { it.subject }
                    .map { (subject, subjectSessions) ->
                        SubjectRadarData(
                            subject = subject,
                            minutes = subjectSessions.sumOf { it.durationSeconds } / 60
                        )
                    }
                    .sortedByDescending { it.minutes }
                    .take(6)
                
                _uiState.value = _uiState.value.copy(topSubjects = subjectMinutes)
            }
        }
    }
    
    private fun formatTime(hour: Int, minute: Int): String {
        val period = if (hour < 12) "AM" else "PM"
        val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        return "$period $hour12:${minute.toString().padStart(2, '0')}"
    }
    
    fun getMonthName(): String {
        return _uiState.value.currentMonth.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }
    
    private fun loadWeeklyData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Get the start of the current week (Monday)
            val today = LocalDate.now()
            val currentWeekStart = today.minusDays((today.dayOfWeek.value - 1).toLong())
            
            repository.getWeeklyStudyData(currentWeekStart).collect { weekData ->
                _uiState.value = _uiState.value.copy(
                    weeklyData = weekData,
                    isLoading = false
                )
            }
        }
        
        // Load chart data separately
        viewModelScope.launch {
            val today = LocalDate.now()
            val currentWeekStart = today.minusDays((today.dayOfWeek.value - 1).toLong())
            
            repository.getWeeklyChartData(currentWeekStart).collect { chartData ->
                _uiState.value = _uiState.value.copy(weeklyChartData = chartData)
            }
        }
    }
    
    fun navigateToPreviousWeek() {
        val currentWeekStart = _uiState.value.weeklyData?.weekStartDate ?: return
        val previousWeekStart = currentWeekStart.minusWeeks(1)
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            repository.getWeeklyStudyData(previousWeekStart).collect { weekData ->
                _uiState.value = _uiState.value.copy(
                    weeklyData = weekData,
                    isLoading = false
                )
            }
        }
        
        viewModelScope.launch {
            repository.getWeeklyChartData(previousWeekStart).collect { chartData ->
                _uiState.value = _uiState.value.copy(weeklyChartData = chartData)
            }
        }
    }
    
    fun navigateToNextWeek() {
        val currentWeekStart = _uiState.value.weeklyData?.weekStartDate ?: return
        val nextWeekStart = currentWeekStart.plusWeeks(1)
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            repository.getWeeklyStudyData(nextWeekStart).collect { weekData ->
                _uiState.value = _uiState.value.copy(
                    weeklyData = weekData,
                    isLoading = false
                )
            }
        }
        
        viewModelScope.launch {
            repository.getWeeklyChartData(nextWeekStart).collect { chartData ->
                _uiState.value = _uiState.value.copy(weeklyChartData = chartData)
            }
        }
    }
    
    private fun loadPeriodData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            repository.getTodayStudyData().collect { todayData ->
                _uiState.value = _uiState.value.copy(
                    todayData = todayData,
                    isLoading = false
                )
            }
        }
        
        // Load period insights separately
        viewModelScope.launch {
            repository.getPeriodInsights().collect { insights ->
                _uiState.value = _uiState.value.copy(periodInsights = insights)
            }
        }
    }
}

