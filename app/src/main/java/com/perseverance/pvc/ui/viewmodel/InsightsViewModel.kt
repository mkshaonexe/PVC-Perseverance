package com.perseverance.pvc.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.perseverance.pvc.data.StudyRepository
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
    val selectedPeriod: PeriodType = PeriodType.DAY,
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val monthDays: Map<LocalDate, DayStudyData> = emptyMap(),
    val selectedDayData: DayStudyData? = null,
    val isLoading: Boolean = false
)

class InsightsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StudyRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()
    
    init {
        loadMonthData()
    }
    
    fun selectPeriod(period: PeriodType) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
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
    }
    
    private fun formatTime(hour: Int, minute: Int): String {
        val period = if (hour < 12) "AM" else "PM"
        val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        return "$period $hour12:${minute.toString().padStart(2, '0')}"
    }
    
    fun getMonthName(): String {
        return _uiState.value.currentMonth.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }
}

