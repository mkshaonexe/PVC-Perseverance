package com.perseverance.pvc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perseverance.pvc.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

data class StudyUiState(
    val chartData: StudyChartData = StudyChartData(
        dailyData = emptyList(),
        totalStudyTime = 0,
        mostStudiedSubject = null
    ),
    val isLoading: Boolean = false,
    val timerDisplay: String = "25:00",
    val isTimerRunning: Boolean = false,
    val remainingSeconds: Int = 25 * 60, // 25 minutes in seconds
    val completedSessions: Int = 0
)

class StudyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(StudyUiState())
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()
    
    private var timerJob: Job? = null
    private val workDuration = 25 * 60 // 25 minutes in seconds
    
    init {
        loadStudyData()
    }
    
    private fun loadStudyData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Generate sample data for demonstration
            val sampleData = generateSampleStudyData()
            
            _uiState.value = _uiState.value.copy(
                chartData = sampleData,
                isLoading = false
            )
        }
    }
    
    fun startTimer() {
        if (timerJob?.isActive == true) return
        
        _uiState.value = _uiState.value.copy(isTimerRunning = true)
        
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 && _uiState.value.isTimerRunning) {
                delay(1000)
                val newRemainingSeconds = _uiState.value.remainingSeconds - 1
                _uiState.value = _uiState.value.copy(remainingSeconds = newRemainingSeconds)
                updateTimerDisplay()
            }
            
            if (_uiState.value.remainingSeconds <= 0) {
                onTimerComplete()
            }
        }
    }
    
    fun pauseTimer() {
        _uiState.value = _uiState.value.copy(isTimerRunning = false)
        timerJob?.cancel()
    }
    
    fun resetTimer() {
        pauseTimer()
        _uiState.value = _uiState.value.copy(
            remainingSeconds = workDuration,
            timerDisplay = "25:00"
        )
    }
    
    private fun updateTimerDisplay() {
        val minutes = _uiState.value.remainingSeconds / 60
        val seconds = _uiState.value.remainingSeconds % 60
        _uiState.value = _uiState.value.copy(
            timerDisplay = String.format("%02d:%02d", minutes, seconds)
        )
    }
    
    private fun onTimerComplete() {
        _uiState.value = _uiState.value.copy(
            isTimerRunning = false,
            completedSessions = _uiState.value.completedSessions + 1
        )
        timerJob?.cancel()
        
        // Reset timer for next session
        _uiState.value = _uiState.value.copy(
            remainingSeconds = workDuration,
            timerDisplay = "25:00"
        )
    }
    
    private fun generateSampleStudyData(): StudyChartData {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val twoDaysAgo = today.minusDays(2)
        
        // Sample subjects
        val subjects = listOf("Mathematics", "Physics", "Chemistry", "Biology", "English")
        
        // Generate sample data for the last 3 days
        val dailyData = listOf(
            createDailyData(twoDaysAgo, subjects),
            createDailyData(yesterday, subjects),
            createDailyData(today, subjects)
        )
        
        val totalStudyTime = dailyData.sumOf { day ->
            day.subjects.sumOf { it.totalMinutes }
        }
        
        val mostStudiedSubject = dailyData.lastOrNull()?.subjects
            ?.maxByOrNull { it.totalMinutes }?.subject
        
        return StudyChartData(
            dailyData = dailyData,
            totalStudyTime = totalStudyTime,
            mostStudiedSubject = mostStudiedSubject
        )
    }
    
    private fun createDailyData(date: LocalDate, subjects: List<String>): DailyStudyData {
        val subjectStudyTimes = subjects.map { subject ->
            val totalMinutes = (30..180).random() // Random study time between 30-180 minutes
            val sessionCount = (1..4).random() // 1-4 sessions per subject
            
            val sessions = (1..sessionCount).map { sessionIndex ->
                val sessionMinutes = totalMinutes / sessionCount
                val startTime = LocalDateTime.of(date, java.time.LocalTime.of(8 + sessionIndex * 2, 0))
                val endTime = startTime.plusMinutes(sessionMinutes.toLong())
                
                StudySession(
                    id = "${subject}_${date}_${sessionIndex}",
                    subject = subject,
                    startTime = startTime,
                    endTime = endTime,
                    durationMinutes = sessionMinutes
                )
            }
            
            SubjectStudyTime(
                subject = subject,
                totalMinutes = totalMinutes,
                sessions = sessions
            )
        }
        
        return DailyStudyData(
            date = date,
            subjects = subjectStudyTimes
        )
    }
    
    fun refreshData() {
        loadStudyData()
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
