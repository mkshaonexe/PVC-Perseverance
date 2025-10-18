package com.perseverance.pvc.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.perseverance.pvc.data.StudyRepository
import com.perseverance.pvc.data.PomodoroTimerState
import com.perseverance.pvc.data.SettingsRepository
import com.perseverance.pvc.services.TimerNotificationService
import com.perseverance.pvc.utils.PermissionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class PomodoroUiState(
    val timeDisplay: String = "50:00",
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
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
    private val settingsRepository = SettingsRepository(application)
    private val notificationService = TimerNotificationService(application.applicationContext)
    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()
    
    // Track notification setting
    private var enableNotifications = true
    
    private var timerJob: Job? = null
    private var remainingTimeInSeconds = 50 * 60 // Default 50 minutes in seconds
    private var workDuration = 50 * 60 // Default 50 minutes, will be updated from settings
    private val shortBreakDuration = 5 * 60 // 5 minutes
    private val longBreakDuration = 15 * 60 // 15 minutes
    
    // Track session start time and initial duration for accurate recording
    private var sessionStartTime: LocalDateTime? = null
    private var sessionInitialDuration: Int = 0
    
    init {
        loadTimerDuration()
        loadNotificationSetting()
        updateTimeDisplay()
        loadSavedSubjects()
        loadTodayTotalStudyTime()
        restoreTimerState()
    }
    
    private fun loadTimerDuration() {
        viewModelScope.launch {
            settingsRepository.getTimerDuration().collect { durationString ->
                val durationMinutes = durationString.toIntOrNull() ?: 50
                workDuration = durationMinutes * 60
                // Update remaining time if it's a work session and not currently running
                if (_uiState.value.currentSessionType == SessionType.WORK && !_uiState.value.isPlaying) {
                    remainingTimeInSeconds = workDuration
                    updateTimeDisplay()
                }
            }
        }
    }
    
    private fun loadNotificationSetting() {
        viewModelScope.launch {
            settingsRepository.getEnableTimerNotifications().collect { enabled ->
                enableNotifications = enabled
            }
        }
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
            // Save initial state immediately
            saveTimerState()
        }
        
        _uiState.value = _uiState.value.copy(isPlaying = true, isPaused = false)
        
        timerJob = viewModelScope.launch {
            var secondsCounter = 0
            while (remainingTimeInSeconds > 0 && _uiState.value.isPlaying) {
                delay(1000)
                if (!_uiState.value.isPlaying) break
                remainingTimeInSeconds--
                secondsCounter++
                updateTimeDisplay()
                
                // Auto-save timer state every 5 seconds while running
                if (secondsCounter % 5 == 0) {
                    saveTimerState()
                }
                
                // Update total study time display in real-time (without blocking)
                val savedSeconds = repository.getTodayTotalSecondsOnce()
                updateTotalStudyTimeDisplay(savedSeconds)
            }
            
            // Only complete automatically if still in playing state
            if (remainingTimeInSeconds <= 0 && _uiState.value.isPlaying) {
                onTimerComplete()
            }
        }
    }
    
    fun pauseTimer() {
        // Set isPlaying to false FIRST so the while loop in startTimer stops naturally
        _uiState.value = _uiState.value.copy(isPlaying = false, isPaused = true)
        
        // Then cancel the job
        timerJob?.cancel()
        timerJob = null
        
        // Auto-save timer state when pausing
        saveTimerState()
        
        // Don't save the session when pausing - wait until user clicks "Done" or timer completes
        // Just refresh the display
        viewModelScope.launch {
            val savedSeconds = repository.getTodayTotalSecondsOnce()
            updateTotalStudyTimeDisplay(savedSeconds)
        }
    }
    
    fun completeSession() {
        // Stop the timer
        timerJob?.cancel()
        timerJob = null
        _uiState.value = _uiState.value.copy(isPlaying = false, isPaused = false)
        
        // Save the session (only for work sessions)
        if (_uiState.value.currentSessionType == SessionType.WORK && sessionStartTime != null) {
            viewModelScope.launch {
                saveCurrentSession()
                // Clear timer state after saving session
                repository.clearTimerState()
                // Reset timer to initial state
                remainingTimeInSeconds = workDuration
                updateTimeDisplay()
                loadTodayTotalStudyTime()
            }
        } else {
            // For break sessions, just reset
            resetTimer()
        }
    }
    
    fun resetTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.value = _uiState.value.copy(isPlaying = false, isPaused = false)
        
        // Clear any existing notifications
        notificationService.cancelAllNotifications()
        
        remainingTimeInSeconds = when (_uiState.value.currentSessionType) {
            SessionType.WORK -> workDuration
            SessionType.SHORT_BREAK -> shortBreakDuration
            SessionType.LONG_BREAK -> longBreakDuration
        }
        
        // Reset session tracking
        sessionStartTime = null
        sessionInitialDuration = 0
        
        // Clear saved timer state
        viewModelScope.launch {
            repository.clearTimerState()
        }
        
        updateTimeDisplay()
        loadTodayTotalStudyTime()
    }
    
    private fun updateTimeDisplay() {
        val minutes = remainingTimeInSeconds / 60
        val seconds = remainingTimeInSeconds % 60
        _uiState.value = _uiState.value.copy(
            timeDisplay = String.format("%02d:%02d", minutes, seconds)
        )
    }
    
    private fun onTimerComplete() {
        _uiState.value = _uiState.value.copy(isPlaying = false, isPaused = false)
        
        when (_uiState.value.currentSessionType) {
            SessionType.WORK -> {
                // Save completed work session
                saveCurrentSession()
                
                // Show work session complete notification
                if (enableNotifications && PermissionManager.hasNotificationPermission(getApplication())) {
                    notificationService.showWorkSessionCompleteNotification()
                }
                
                val newCompletedSessions = _uiState.value.completedSessions + 1
                _uiState.value = _uiState.value.copy(
                    completedSessions = newCompletedSessions,
                    currentSessionType = if (newCompletedSessions % 4 == 0) SessionType.LONG_BREAK else SessionType.SHORT_BREAK
                )
                remainingTimeInSeconds = if (newCompletedSessions % 4 == 0) longBreakDuration else shortBreakDuration
                
                // Clear timer state after completing work session
                viewModelScope.launch {
                    repository.clearTimerState()
                }
            }
            SessionType.SHORT_BREAK, SessionType.LONG_BREAK -> {
                // Show break complete notification
                if (enableNotifications && PermissionManager.hasNotificationPermission(getApplication())) {
                    notificationService.showBreakCompleteNotification()
                }
                
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
    
    // Save current timer state for auto-save functionality
    private fun saveTimerState() {
        // Only save state if there's an active session
        if (sessionStartTime != null && _uiState.value.currentSessionType == SessionType.WORK) {
            val timerState = PomodoroTimerState(
                sessionStartTime = sessionStartTime,
                remainingTimeInSeconds = remainingTimeInSeconds,
                sessionInitialDuration = sessionInitialDuration,
                selectedSubject = _uiState.value.selectedSubject,
                sessionType = _uiState.value.currentSessionType.name,
                completedSessions = _uiState.value.completedSessions,
                isPlaying = _uiState.value.isPlaying,
                isPaused = _uiState.value.isPaused
            )
            
            viewModelScope.launch {
                repository.saveTimerState(timerState)
            }
        }
    }
    
    // Restore timer state on app restart
    private fun restoreTimerState() {
        viewModelScope.launch {
            val timerState = repository.restoreTimerState()
            
            if (timerState != null && timerState.sessionStartTime != null) {
                // Calculate how much time was studied
                val studyDurationSeconds = (timerState.sessionInitialDuration - timerState.remainingTimeInSeconds).coerceAtLeast(0)
                
                // Save the session if any time was studied (even 1 second)
                if (studyDurationSeconds > 0) {
                    val session = repository.createStudySession(
                        subject = timerState.selectedSubject,
                        durationSeconds = studyDurationSeconds,
                        startTime = timerState.sessionStartTime
                    )
                    repository.saveStudySession(session)
                    
                    // Clear the saved state after auto-saving
                    repository.clearTimerState()
                    
                    // Reset to initial state for a fresh start
                    sessionStartTime = null
                    sessionInitialDuration = 0
                    remainingTimeInSeconds = workDuration
                    _uiState.value = _uiState.value.copy(
                        isPlaying = false,
                        isPaused = false,
                        currentSessionType = SessionType.WORK,
                        selectedSubject = timerState.selectedSubject // Keep the same subject
                    )
                    updateTimeDisplay()
                    loadTodayTotalStudyTime()
                } else {
                    // No time was studied, just restore the paused state
                    sessionStartTime = timerState.sessionStartTime
                    remainingTimeInSeconds = timerState.remainingTimeInSeconds
                    sessionInitialDuration = timerState.sessionInitialDuration
                    
                    _uiState.value = _uiState.value.copy(
                        selectedSubject = timerState.selectedSubject,
                        currentSessionType = SessionType.valueOf(timerState.sessionType),
                        completedSessions = timerState.completedSessions,
                        isPlaying = false,
                        isPaused = true
                    )
                    
                    updateTimeDisplay()
                }
            }
        }
    }
    
    // Public method to force save timer state (called from Activity lifecycle)
    fun onAppGoingToBackground() {
        if (_uiState.value.isPlaying || _uiState.value.isPaused) {
            saveTimerState()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        
        // Auto-save timer state when ViewModel is destroyed
        if (sessionStartTime != null && (_uiState.value.isPlaying || _uiState.value.isPaused)) {
            // Save the current session immediately
            val studyDurationSeconds = (sessionInitialDuration - remainingTimeInSeconds).coerceAtLeast(0)
            
            if (studyDurationSeconds > 0) {
                val session = repository.createStudySession(
                    subject = _uiState.value.selectedSubject,
                    durationSeconds = studyDurationSeconds,
                    startTime = sessionStartTime!!
                )
                
                // Use runBlocking to ensure the save completes before cleanup
                kotlinx.coroutines.runBlocking {
                    repository.saveStudySession(session)
                    repository.clearTimerState()
                }
            }
        }
    }
}
