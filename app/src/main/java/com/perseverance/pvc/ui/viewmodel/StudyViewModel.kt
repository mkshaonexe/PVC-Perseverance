package com.perseverance.pvc.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.perseverance.pvc.data.*
import com.perseverance.pvc.data.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class StudyUiState(
    val chartData: StudyChartData = StudyChartData(
        dailyData = emptyList(),
        totalStudyTime = 0,
        mostStudiedSubject = null
    ),
    val isLoading: Boolean = false,
    val timerDisplay: String = "50:00",
    val isTimerRunning: Boolean = false,
    val remainingSeconds: Int = 50 * 60, // Default 50 minutes in seconds
    val completedSessions: Int = 0,
    val totalStudyTimeDisplay: String = "00:00:00"  // HH:MM:SS format
)

class StudyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StudyRepository(application.applicationContext)
    private val settingsRepository = SettingsRepository(application)
    private val _uiState = MutableStateFlow(StudyUiState())
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()
    
    private var timerJob: Job? = null
    private var workDuration = 50 * 60 // Default 50 minutes, will be updated from settings
    
    init {
        loadTimerDuration()
        loadStudyData()
        loadTodayTotalStudyTime()
    }
    
    private fun loadTimerDuration() {
        viewModelScope.launch {
            settingsRepository.getTimerDuration().collect { durationString ->
                val durationMinutes = durationString.toIntOrNull() ?: 50
                workDuration = durationMinutes * 60
                // Update remaining time if not currently running
                if (!_uiState.value.isTimerRunning) {
                    _uiState.value = _uiState.value.copy(remainingSeconds = workDuration)
                    updateTimerDisplay()
                }
            }
        }
    }
    
    private fun loadStudyData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Load real data from repository
            repository.getStudyChartData(days = 7).collect { chartData ->
                _uiState.value = _uiState.value.copy(
                    chartData = chartData,
                    isLoading = false
                )
            }
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
        val minutes = workDuration / 60
        val seconds = workDuration % 60
        _uiState.value = _uiState.value.copy(
            remainingSeconds = workDuration,
            timerDisplay = String.format("%02d:%02d", minutes, seconds)
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
        val minutes = workDuration / 60
        val seconds = workDuration % 60
        _uiState.value = _uiState.value.copy(
            remainingSeconds = workDuration,
            timerDisplay = String.format("%02d:%02d", minutes, seconds)
        )
    }
    
    fun refreshData() {
        loadStudyData()
        loadTodayTotalStudyTime()
    }
    
    private fun loadTodayTotalStudyTime() {
        viewModelScope.launch {
            repository.getTodayTotalSeconds().collect { totalSeconds ->
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60
                val timeDisplay = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                
                _uiState.value = _uiState.value.copy(
                    totalStudyTimeDisplay = timeDisplay
                )
            }
        }
    }
    
    // Helper function to add test data (for development/testing)
    fun addTestData() {
        viewModelScope.launch {
            val subjects = listOf("Mathematics", "Physics", "English")
            subjects.forEach { subject ->
                val session = repository.createStudySession(
                    subject = subject,
                    durationSeconds = workDuration,  // Use current work duration
                    startTime = LocalDateTime.now().minusHours(1)
                )
                repository.saveStudySession(session)
            }
            refreshData()
        }
    }
    
    // Add manual study time for developer mode
    fun addManualStudyTime(
        date: LocalDate,
        subject: String,
        durationMinutes: Int,
        startTime: LocalTime
    ) {
        viewModelScope.launch {
            val startDateTime = date.atTime(startTime)
            val session = repository.createStudySession(
                subject = subject,
                durationSeconds = durationMinutes * 60,
                startTime = startDateTime
            )
            repository.saveStudySession(session)
            refreshData()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
