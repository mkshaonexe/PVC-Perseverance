package com.perseverance.pvc.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.perseverance.pvc.data.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class PomodoroUiState(
    val timeDisplay: String = "25:00",
    val isPlaying: Boolean = false,
    val completedSessions: Int = 0,
    val currentSessionType: SessionType = SessionType.WORK,
    val selectedSubject: String = "pomodoro",
    val availableSubjects: List<String> = listOf(
        "Mathematics",
        "Physics",
        "Chemistry",
        "Biology",
        "English",
        "History",
        "Geography",
        "Computer Science"
    ),
    val showSubjectDialog: Boolean = false,
    val totalStudyTimeDisplay: String = "00:00:00"  // HH:MM:SS format
)

enum class SessionType {
    WORK, SHORT_BREAK, LONG_BREAK
}

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StudyRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()
    
    private var timerJob: Job? = null
    private var remainingTimeInSeconds = 25 * 60 // 25 minutes in seconds
    private val workDuration = 25 * 60 // 25 minutes
    private val shortBreakDuration = 5 * 60 // 5 minutes
    private val longBreakDuration = 15 * 60 // 15 minutes
    
    // Track session start time and initial duration for accurate recording
    private var sessionStartTime: LocalDateTime? = null
    private var sessionInitialDuration: Int = 0
    
    init {
        updateTimeDisplay()
        loadSavedSubjects()
        loadTodayTotalStudyTime()
    }
    
    private fun loadSavedSubjects() {
        viewModelScope.launch {
            repository.getSavedSubjects().collect { subjects ->
                _uiState.value = _uiState.value.copy(
                    availableSubjects = subjects
                )
            }
        }
    }
    
    private fun loadTodayTotalStudyTime() {
        viewModelScope.launch {
            repository.getTodayTotalSeconds().collect { totalSeconds ->
                updateTotalStudyTimeDisplay(totalSeconds)
            }
        }
    }
    
    private fun updateTotalStudyTimeDisplay(savedSeconds: Int) {
        // Add current session time if timer is running
        val currentSessionSeconds = if (_uiState.value.isPlaying && sessionStartTime != null) {
            sessionInitialDuration - remainingTimeInSeconds
        } else {
            0
        }
        
        val totalSeconds = savedSeconds + currentSessionSeconds
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        _uiState.value = _uiState.value.copy(
            totalStudyTimeDisplay = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        )
    }
    
    fun startTimer() {
        if (timerJob?.isActive == true) return
        
        // Only set session start time if this is a new session (not resuming)
        if (sessionStartTime == null && _uiState.value.currentSessionType == SessionType.WORK) {
            sessionStartTime = LocalDateTime.now()
            sessionInitialDuration = remainingTimeInSeconds
        }
        
        _uiState.value = _uiState.value.copy(isPlaying = true)
        
        timerJob = viewModelScope.launch {
            while (remainingTimeInSeconds > 0 && _uiState.value.isPlaying) {
                delay(1000)
                remainingTimeInSeconds--
                updateTimeDisplay()
                
                // Update total study time display in real-time (without blocking)
                val savedSeconds = repository.getTodayTotalSecondsOnce()
                updateTotalStudyTimeDisplay(savedSeconds)
            }
            
            if (remainingTimeInSeconds <= 0) {
                onTimerComplete()
            }
        }
    }
    
    fun pauseTimer() {
        _uiState.value = _uiState.value.copy(isPlaying = false)
        timerJob?.cancel()
        
        // Save partial session when paused (only for work sessions)
        if (_uiState.value.currentSessionType == SessionType.WORK && sessionStartTime != null) {
            saveCurrentSession()
        }
        
        // Refresh total time display after pausing
        loadTodayTotalStudyTime()
    }
    
    fun resetTimer() {
        pauseTimer()
        remainingTimeInSeconds = when (_uiState.value.currentSessionType) {
            SessionType.WORK -> workDuration
            SessionType.SHORT_BREAK -> shortBreakDuration
            SessionType.LONG_BREAK -> longBreakDuration
        }
        updateTimeDisplay()
    }
    
    private fun updateTimeDisplay() {
        val minutes = remainingTimeInSeconds / 60
        val seconds = remainingTimeInSeconds % 60
        _uiState.value = _uiState.value.copy(
            timeDisplay = String.format("%02d:%02d", minutes, seconds)
        )
    }
    
    private fun onTimerComplete() {
        _uiState.value = _uiState.value.copy(isPlaying = false)
        
        when (_uiState.value.currentSessionType) {
            SessionType.WORK -> {
                // Save completed work session
                saveCurrentSession()
                
                val newCompletedSessions = _uiState.value.completedSessions + 1
                _uiState.value = _uiState.value.copy(
                    completedSessions = newCompletedSessions,
                    currentSessionType = if (newCompletedSessions % 4 == 0) SessionType.LONG_BREAK else SessionType.SHORT_BREAK
                )
                remainingTimeInSeconds = if (newCompletedSessions % 4 == 0) longBreakDuration else shortBreakDuration
            }
            SessionType.SHORT_BREAK, SessionType.LONG_BREAK -> {
                _uiState.value = _uiState.value.copy(currentSessionType = SessionType.WORK)
                remainingTimeInSeconds = workDuration
            }
        }
        
        updateTimeDisplay()
    }
    
    private fun saveCurrentSession() {
        if (sessionStartTime == null) return
        
        // Calculate actual study time in SECONDS (time elapsed since session start)
        val studyDurationSeconds = (sessionInitialDuration - remainingTimeInSeconds).coerceAtLeast(0)
        
        // Only save if user studied for at least 1 second
        if (studyDurationSeconds < 1) {
            sessionStartTime = null
            sessionInitialDuration = 0
            return
        }
        
        val session = repository.createStudySession(
            subject = _uiState.value.selectedSubject,
            durationSeconds = studyDurationSeconds,
            startTime = sessionStartTime!!
        )
        
        viewModelScope.launch {
            repository.saveStudySession(session)
        }
        
        // Reset session tracking
        sessionStartTime = null
        sessionInitialDuration = 0
    }
    
    fun showSubjectDialog() {
        _uiState.value = _uiState.value.copy(showSubjectDialog = true)
    }
    
    fun hideSubjectDialog() {
        _uiState.value = _uiState.value.copy(showSubjectDialog = false)
    }
    
    fun selectSubject(subject: String) {
        _uiState.value = _uiState.value.copy(
            selectedSubject = subject,
            showSubjectDialog = false
        )
    }
    
    fun addNewSubject(subject: String) {
        if (subject.isNotBlank() && !_uiState.value.availableSubjects.contains(subject)) {
            val newSubjects = _uiState.value.availableSubjects + subject
            _uiState.value = _uiState.value.copy(
                availableSubjects = newSubjects
            )
            
            // Save to repository
            viewModelScope.launch {
                repository.saveSubjects(newSubjects)
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
