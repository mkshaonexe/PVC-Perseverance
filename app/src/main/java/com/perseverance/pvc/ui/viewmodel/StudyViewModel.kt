package com.perseverance.pvc.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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

class StudyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StudyRepository(application.applicationContext)
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
    
    fun refreshData() {
        loadStudyData()
    }
    
    // Helper function to add test data (for development/testing)
    fun addTestData() {
        viewModelScope.launch {
            val subjects = listOf("Mathematics", "Physics", "English")
            subjects.forEach { subject ->
                val session = repository.createStudySession(
                    subject = subject,
                    durationSeconds = 25 * 60,  // 25 minutes in seconds
                    startTime = LocalDateTime.now().minusHours(1)
                )
                repository.saveStudySession(session)
            }
            refreshData()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
