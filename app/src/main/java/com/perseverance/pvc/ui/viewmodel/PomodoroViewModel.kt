package com.perseverance.pvc.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.perseverance.pvc.data.StudyRepository
import com.perseverance.pvc.data.PomodoroTimerState
import com.perseverance.pvc.data.SettingsRepository
import com.perseverance.pvc.services.TimerNotificationService
import com.perseverance.pvc.services.TimerSoundService
import com.perseverance.pvc.utils.PermissionManager
import com.perseverance.pvc.widgets.PomodoroTimerWidgetProvider
import com.perseverance.pvc.widgets.StudyTimeWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
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
    val selectedSubject: String = "English",
    val availableSubjects: List<String> = listOf(
        "English",
        "Math",
        "Break"
    ),
    val showSubjectDialog: Boolean = false,
    val totalStudyTimeDisplay: String = "00:00:00",  // HH:MM:SS format
    val isTimerCompleted: Boolean = false,  // New state for timer completion
    val isWaitingForAcknowledgment: Boolean = false  // New state to track if waiting for user acknowledgment
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
    
    // Track if we've already restored state to avoid multiple restores
    private var hasRestoredState = false
    
    // Sound service connection
    private var soundService: TimerSoundService? = null
    private var isSoundServiceBound = false
    private val soundServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("PomodoroViewModel", "Sound service connected")
            val binder = service as TimerSoundService.TimerSoundBinder
            soundService = binder.getService()
            isSoundServiceBound = true
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("PomodoroViewModel", "Sound service disconnected")
            soundService = null
            isSoundServiceBound = false
        }
    }
    
    private var timerJob: Job? = null
    private var remainingTimeInSeconds = 50 * 60 // Default 50 minutes in seconds
    private var workDuration = 50 * 60 // Default 50 minutes, will be updated from settings
    private var breakDuration = 10 * 60 // Default 10 minutes, will be updated from settings
    private val shortBreakDuration = 5 * 60 // 5 minutes
    private val longBreakDuration = 15 * 60 // 15 minutes
    
    // Track session start time and initial duration for accurate recording
    private var sessionStartTime: LocalDateTime? = null
    private var sessionInitialDuration: Int = 0
    
    init {
        loadTimerDuration()
        loadBreakDuration()
        loadNotificationSetting()
        updateTimeDisplay()
        loadSavedSubjects()
        loadTodayTotalStudyTime()
        restoreTimerState()
        bindSoundService()
    }
    
    private fun bindSoundService() {
        Log.d("PomodoroViewModel", "Binding sound service")
        val intent = Intent(getApplication(), TimerSoundService::class.java)
        getApplication<Application>().bindService(intent, soundServiceConnection, Context.BIND_AUTO_CREATE)
    }
    
    private fun unbindSoundService() {
        if (isSoundServiceBound) {
            getApplication<Application>().unbindService(soundServiceConnection)
            isSoundServiceBound = false
        }
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
    
    private fun loadBreakDuration() {
        viewModelScope.launch {
            settingsRepository.getBreakDuration().collect { durationString ->
                val durationMinutes = durationString.toIntOrNull() ?: 10
                breakDuration = durationMinutes * 60
                // Update remaining time if it's a break session and not currently running
                if ((_uiState.value.currentSessionType == SessionType.SHORT_BREAK || 
                     _uiState.value.currentSessionType == SessionType.LONG_BREAK) && 
                    !_uiState.value.isPlaying) {
                    remainingTimeInSeconds = breakDuration
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
        // Add current session time if timer is running AND subject is not "Break"
        val currentSessionSeconds = if (_uiState.value.isPlaying && 
                                         sessionStartTime != null && 
                                         !_uiState.value.selectedSubject.equals("Break", ignoreCase = true)) {
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
        hasRestoredState = false // Allow restore on next background/foreground cycle
        
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
                
                // Update widgets after completing session
                updateWidgets()
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
            SessionType.SHORT_BREAK -> breakDuration
            SessionType.LONG_BREAK -> breakDuration
        }
        
        // Reset session tracking
        sessionStartTime = null
        sessionInitialDuration = 0
        hasRestoredState = false // Allow restore on next background/foreground cycle
        
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
        _uiState.value = _uiState.value.copy(
            isPlaying = false, 
            isPaused = false, 
            isTimerCompleted = true,
            isWaitingForAcknowledgment = true
        )
        
        // Start infinite sound
        Log.d("PomodoroViewModel", "Timer completed, starting sound. Service bound: $isSoundServiceBound, Service: $soundService")
        soundService?.startInfiniteSound()
        
        when (_uiState.value.currentSessionType) {
            SessionType.WORK -> {
                // Save completed work session
                saveCurrentSession()
                
                // Update widgets after timer completes
                updateWidgets()
                
                // Show work session complete notification
                if (enableNotifications && PermissionManager.hasNotificationPermission(getApplication())) {
                    notificationService.showWorkSessionCompleteNotification()
                }
                
                // Don't automatically switch to break mode - wait for user acknowledgment
                // The timer will stay in WORK mode until user clicks "I got it"
                Log.d("PomodoroViewModel", "Work session completed, waiting for user acknowledgment")
            }
            SessionType.SHORT_BREAK, SessionType.LONG_BREAK -> {
                // Show break complete notification
                if (enableNotifications && PermissionManager.hasNotificationPermission(getApplication())) {
                    notificationService.showBreakCompleteNotification()
                }
                
                // Wait for user acknowledgment just like work sessions
                // The sound will play and user must click "I got it" to continue
                Log.d("PomodoroViewModel", "Break session completed, waiting for user acknowledgment")
            }
        }
        
        updateTimeDisplay()
    }
    
    fun acknowledgeTimerCompletion() {
        // Stop the infinite sound
        soundService?.stopInfiniteSound()
        
        // Reset timer completion state
        _uiState.value = _uiState.value.copy(
            isTimerCompleted = false,
            isWaitingForAcknowledgment = false
        )
        
        hasRestoredState = false // Allow restore on next background/foreground cycle
        
        // Handle the transition based on current session type
        when (_uiState.value.currentSessionType) {
            SessionType.WORK -> {
                // Work session completed - stay in work mode and reset to initial state
                // User can choose to start a new session or take a break
                val newCompletedSessions = _uiState.value.completedSessions + 1
                _uiState.value = _uiState.value.copy(
                    completedSessions = newCompletedSessions,
                    currentSessionType = SessionType.WORK
                )
                remainingTimeInSeconds = workDuration
                
                // Reset session tracking so user can start fresh
                sessionStartTime = null
                sessionInitialDuration = 0
                
                // Clear timer state after completing work session
                viewModelScope.launch {
                    repository.clearTimerState()
                }
                
                Log.d("PomodoroViewModel", "Work session acknowledged, staying in work mode for user choice")
            }
            SessionType.SHORT_BREAK, SessionType.LONG_BREAK -> {
                // Break completed - transition back to work and reset to initial state
                _uiState.value = _uiState.value.copy(currentSessionType = SessionType.WORK)
                remainingTimeInSeconds = workDuration
                
                // Reset session tracking so user can start fresh
                sessionStartTime = null
                sessionInitialDuration = 0
                
                // Clear timer state after completing break session
                viewModelScope.launch {
                    repository.clearTimerState()
                }
                
                Log.d("PomodoroViewModel", "Break acknowledged, transitioning back to work and reset to initial state")
            }
        }
        
        updateTimeDisplay()
    }
    
    // Start break timer directly from work session completion
    fun startBreakTimer() {
        Log.d("PomodoroViewModel", "Starting break timer")
        
        // Stop the infinite sound
        soundService?.stopInfiniteSound()
        
        // Reset timer completion state
        _uiState.value = _uiState.value.copy(
            isTimerCompleted = false,
            isWaitingForAcknowledgment = false
        )
        
        // If currently in work session, increment completed sessions
        if (_uiState.value.currentSessionType == SessionType.WORK) {
            val newCompletedSessions = _uiState.value.completedSessions + 1
            _uiState.value = _uiState.value.copy(
                completedSessions = newCompletedSessions,
                currentSessionType = SessionType.SHORT_BREAK
            )
            
            // Clear timer state after completing work session
            viewModelScope.launch {
                repository.clearTimerState()
            }
        } else {
            // If in break session, transition back to work
            _uiState.value = _uiState.value.copy(currentSessionType = SessionType.WORK)
        }
        
        // Set the appropriate duration
        remainingTimeInSeconds = if (_uiState.value.currentSessionType == SessionType.SHORT_BREAK) {
            breakDuration
        } else {
            workDuration
        }
        
        updateTimeDisplay()
        
        // Auto-start the timer
        startTimer()
        
        Log.d("PomodoroViewModel", "Break timer started with duration: $breakDuration seconds")
    }
    
    // Method to start a new work session with the same duration
    fun startNewWorkSession() {
        if (_uiState.value.currentSessionType == SessionType.WORK && !_uiState.value.isPlaying && !_uiState.value.isPaused) {
            remainingTimeInSeconds = workDuration
            updateTimeDisplay()
            Log.d("PomodoroViewModel", "Starting new work session with duration: $workDuration seconds")
        }
    }
    
    private fun saveCurrentSession() {
        if (sessionStartTime == null) return
        
        // Don't save session if subject is "Break" - break time should not count as study time
        if (_uiState.value.selectedSubject.equals("Break", ignoreCase = true)) {
            Log.d("PomodoroViewModel", "Skipping save for Break session - not counting as study time")
            sessionStartTime = null
            sessionInitialDuration = 0
            return
        }
        
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
            // Update widgets after saving session
            updateWidgets()
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
    
    // Update timer duration from home screen (only when timer is not running)
    fun updateTimerDurationFromHome(minutes: Int) {
        // Only allow changing duration when timer is not running or paused
        if (!_uiState.value.isPlaying && !_uiState.value.isPaused) {
            workDuration = minutes * 60
            remainingTimeInSeconds = workDuration
            updateTimeDisplay()
            
            // Save to settings repository
            viewModelScope.launch {
                settingsRepository.setTimerDuration(minutes.toString())
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
                isPaused = _uiState.value.isPaused,
                lastUpdateTime = LocalDateTime.now() // Save current timestamp
            )
            
            viewModelScope.launch {
                repository.saveTimerState(timerState)
            }
        }
    }
    
    // Restore timer state on app restart
    private fun restoreTimerState() {
        // Skip if already restored and timer is currently running
        if (hasRestoredState && _uiState.value.isPlaying) {
            Log.d("PomodoroViewModel", "Timer already running, skipping restore")
            return
        }
        
        viewModelScope.launch {
            val timerState = repository.restoreTimerState()
            
            if (timerState != null && timerState.sessionStartTime != null) {
                hasRestoredState = true
                // Calculate elapsed time since last update using timestamp
                val now = LocalDateTime.now()
                val elapsedSeconds = if (timerState.isPlaying) {
                    // If timer was running, calculate elapsed time since last update
                    java.time.Duration.between(timerState.lastUpdateTime, now).seconds.toInt()
                } else {
                    // If timer was paused, no elapsed time
                    0
                }
                
                // Calculate new remaining time based on elapsed time
                val newRemainingTime = (timerState.remainingTimeInSeconds - elapsedSeconds).coerceAtLeast(0)
                
                // Check if timer completed while in background
                val wasTimerCompleted = newRemainingTime <= 0 && timerState.isPlaying
                
                if (wasTimerCompleted) {
                    // Timer was completed while in background - show completion UI
                    Log.d("PomodoroViewModel", "Timer completed in background. Elapsed: ${elapsedSeconds}s, Remaining was: ${timerState.remainingTimeInSeconds}s")
                    
                    // Calculate actual study duration (initial duration minus what remained when it completed)
                    val actualStudyDuration = timerState.sessionInitialDuration
                    
                    // Only save if subject is not "Break"
                    if (!timerState.selectedSubject.equals("Break", ignoreCase = true)) {
                        val session = repository.createStudySession(
                            subject = timerState.selectedSubject,
                            durationSeconds = actualStudyDuration,
                            startTime = timerState.sessionStartTime
                        )
                        repository.saveStudySession(session)
                        // Update widgets after saving session in background
                        updateWidgets()
                    } else {
                        Log.d("PomodoroViewModel", "Timer completed with Break subject - not saving as study time")
                    }
                    repository.clearTimerState()
                    
                    // Reset remaining time to 0 to show completion
                    remainingTimeInSeconds = 0
                    
                    // Set completion state
                    _uiState.value = _uiState.value.copy(
                        isTimerCompleted = true,
                        isWaitingForAcknowledgment = true,
                        isPlaying = false,
                        isPaused = false,
                        currentSessionType = SessionType.valueOf(timerState.sessionType),
                        completedSessions = timerState.completedSessions,
                        selectedSubject = timerState.selectedSubject
                    )
                    
                    // Show notification immediately when timer completes in background
                    when (SessionType.valueOf(timerState.sessionType)) {
                        SessionType.WORK -> {
                            if (enableNotifications && PermissionManager.hasNotificationPermission(getApplication())) {
                                Log.d("PomodoroViewModel", "Showing work session complete notification")
                                notificationService.showWorkSessionCompleteNotification()
                            }
                        }
                        SessionType.SHORT_BREAK, SessionType.LONG_BREAK -> {
                            if (enableNotifications && PermissionManager.hasNotificationPermission(getApplication())) {
                                Log.d("PomodoroViewModel", "Showing break complete notification")
                                notificationService.showBreakCompleteNotification()
                            }
                        }
                    }
                    
                    // Start sound for completion
                    Log.d("PomodoroViewModel", "Starting completion sound after background completion")
                    soundService?.startInfiniteSound()
                    
                    updateTimeDisplay()
                    loadTodayTotalStudyTime()
                } else if (timerState.isPlaying && newRemainingTime > 0) {
                    // Timer was running and still has time - restore and AUTO-RESUME
                    Log.d("PomodoroViewModel", "Timer was running in background. Elapsed: ${elapsedSeconds}s, New remaining: ${newRemainingTime}s. Auto-resuming...")
                    
                    sessionStartTime = timerState.sessionStartTime
                    remainingTimeInSeconds = newRemainingTime
                    sessionInitialDuration = timerState.sessionInitialDuration
                    
                    _uiState.value = _uiState.value.copy(
                        selectedSubject = timerState.selectedSubject,
                        currentSessionType = SessionType.valueOf(timerState.sessionType),
                        completedSessions = timerState.completedSessions,
                        isPlaying = false, // Set to false first, startTimer will set to true
                        isPaused = false,
                        isTimerCompleted = false,
                        isWaitingForAcknowledgment = false
                    )
                    
                    updateTimeDisplay()
                    loadTodayTotalStudyTime()
                    
                    // Auto-resume the timer
                    startTimer()
                } else if (timerState.isPaused) {
                    // Timer was paused - just restore the state
                    Log.d("PomodoroViewModel", "Timer was paused, restoring state")
                    
                    sessionStartTime = timerState.sessionStartTime
                    remainingTimeInSeconds = timerState.remainingTimeInSeconds
                    sessionInitialDuration = timerState.sessionInitialDuration
                    
                    _uiState.value = _uiState.value.copy(
                        selectedSubject = timerState.selectedSubject,
                        currentSessionType = SessionType.valueOf(timerState.sessionType),
                        completedSessions = timerState.completedSessions,
                        isPlaying = false,
                        isPaused = true,
                        isTimerCompleted = false,
                        isWaitingForAcknowledgment = false
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
        
        // Stop sound when app goes to background to prevent infinite playing
        if (isSoundServiceBound && soundService != null) {
            Log.d("PomodoroViewModel", "App going to background, stopping sound")
            soundService?.stopInfiniteSound()
        }
    }
    
    // Called when app returns to foreground
    fun onAppReturningToForeground() {
        Log.d("PomodoroViewModel", "App returning to foreground")
        // Rebind sound service if needed
        if (!isSoundServiceBound) {
            bindSoundService()
        }
        
        // Restore timer state if needed
        restoreTimerState()
    }
    
    // Called when app resumes (after onStart)
    fun onAppResumed() {
        Log.d("PomodoroViewModel", "App resumed")
        // Ensure UI state is properly updated
        updateTimeDisplay()
        
        // If timer was completed while in background, ensure sound is playing
        if (_uiState.value.isTimerCompleted && _uiState.value.isWaitingForAcknowledgment) {
            Log.d("PomodoroViewModel", "Timer was completed in background, restarting sound")
            soundService?.startInfiniteSound()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        unbindSoundService()
        
        // Auto-save timer state when ViewModel is destroyed
        if (sessionStartTime != null && (_uiState.value.isPlaying || _uiState.value.isPaused)) {
            // Save the current session immediately (but only if not "Break")
            val studyDurationSeconds = (sessionInitialDuration - remainingTimeInSeconds).coerceAtLeast(0)
            
            if (studyDurationSeconds > 0 && !_uiState.value.selectedSubject.equals("Break", ignoreCase = true)) {
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
                
                // Update widgets after saving session on cleanup
                updateWidgets()
            } else if (_uiState.value.selectedSubject.equals("Break", ignoreCase = true)) {
                Log.d("PomodoroViewModel", "ViewModel cleared with Break subject - not saving as study time")
            }
        }
    }
    
    // Helper method to update all widgets
    private fun updateWidgets() {
        val context = getApplication<Application>().applicationContext
        PomodoroTimerWidgetProvider.updateAllWidgets(context)
        StudyTimeWidgetProvider.updateAllWidgets(context)
    }
}
