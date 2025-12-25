package com.perseverance.pvc.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.perseverance.pvc.data.*
import com.perseverance.pvc.data.SettingsRepository
import com.perseverance.pvc.utils.AnalyticsHelper
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
                
                // If timer is NOT running, update the display and local service state
                if (!com.perseverance.pvc.services.TimerService.isTimerRunning.value) {
                    _uiState.value = _uiState.value.copy(remainingSeconds = workDuration)
                    updateTimerDisplay()
                    // Update service static state so it starts from correct time if started
                    com.perseverance.pvc.services.TimerService.updateRemainingTimeLocally(workDuration)
                }
            }
        }
    }
    
    private fun loadStudyData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Observe TimerService state
            launch {
                com.perseverance.pvc.services.TimerService.remainingSeconds.collect { seconds ->
                    _uiState.value = _uiState.value.copy(remainingSeconds = seconds)
                    updateTimerDisplay()
                }
            }
            
            launch {
                com.perseverance.pvc.services.TimerService.isTimerRunning.collect { isRunning ->
                    _uiState.value = _uiState.value.copy(isTimerRunning = isRunning)
                    // If it just stopped and reached 0, handle completion
                    if (!isRunning && _uiState.value.remainingSeconds <= 0) {
                         onTimerComplete()
                    }
                }
            }
            
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
        // Start Service
        val context = getApplication<Application>().applicationContext
        
        // Get data for notification
        val dailyTotal = try {
            // Need to run blocking or launch separate, but here we are in Main thread.
            // Ideally we get this from UI state or flow.
            // For now, let's use a simple approximate or launch a coroutine to start service
            // Better: launch in viewModelScope
             0 // Placeholder, handled inside launch below
        } catch (e: Exception) { 0 }

        viewModelScope.launch {
            val actualDailyTotal = repository.getTodayTotalSecondsOnce()
            
            val intent = android.content.Intent(context, com.perseverance.pvc.services.TimerService::class.java).apply {
                action = com.perseverance.pvc.services.TimerService.ACTION_START
                putExtra(com.perseverance.pvc.services.TimerService.EXTRA_DURATION, workDuration)
                putExtra(com.perseverance.pvc.services.TimerService.EXTRA_DAILY_TOTAL_SECONDS, actualDailyTotal)
                putExtra(com.perseverance.pvc.services.TimerService.EXTRA_SESSION_INITIAL_SECONDS, workDuration) // In Dashboard, we restart from full duration
                putExtra(com.perseverance.pvc.services.TimerService.EXTRA_IS_STUDY_SESSION, true)
            }
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            
            AnalyticsHelper.logEvent("dashboard_timer_start")
        }
    }
    
    fun pauseTimer() {
        val context = getApplication<Application>().applicationContext
        val intent = android.content.Intent(context, com.perseverance.pvc.services.TimerService::class.java).apply {
            action = com.perseverance.pvc.services.TimerService.ACTION_PAUSE
        }
        context.startService(intent)
        AnalyticsHelper.logEvent("dashboard_timer_pause")
    }
    
    fun resetTimer() {
        AnalyticsHelper.logEvent("dashboard_timer_reset")
        val context = getApplication<Application>().applicationContext
        val intent = android.content.Intent(context, com.perseverance.pvc.services.TimerService::class.java).apply {
            action = com.perseverance.pvc.services.TimerService.ACTION_RESET
            putExtra(com.perseverance.pvc.services.TimerService.EXTRA_DURATION, workDuration)
        }
        context.startService(intent)
        
        // Immediate UI update
        val minutes = workDuration / 60
        val seconds = workDuration % 60
        _uiState.value = _uiState.value.copy(
            remainingSeconds = workDuration,
            timerDisplay = String.format("%02d:%02d", minutes, seconds),
            isTimerRunning = false
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
        // Only trigger if we haven't already processed this completion
        // (Simple check: if remainingSeconds is 0)
        AnalyticsHelper.logEvent("dashboard_timer_complete")
        
        _uiState.value = _uiState.value.copy(
             completedSessions = _uiState.value.completedSessions + 1
        )
        // Reset Logic is handled by user action or separate flow usually, 
        // but here we just reset the timer for next round in UI
         val minutes = workDuration / 60
        val seconds = workDuration % 60
         _uiState.value = _uiState.value.copy(
            remainingSeconds = workDuration,
            timerDisplay = String.format("%02d:%02d", minutes, seconds)
        )
        // Also ensure service state is reset locally if needed
        com.perseverance.pvc.services.TimerService.updateRemainingTimeLocally(workDuration)
    }
    
    fun refreshData() {
        // loadStudyData() // Don't re-subscribe to flows multiple times
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
