package com.perseverance.pvc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PomodoroUiState(
    val timeDisplay: String = "25:00",
    val isPlaying: Boolean = false,
    val completedSessions: Int = 0,
    val currentSessionType: SessionType = SessionType.WORK
)

enum class SessionType {
    WORK, SHORT_BREAK, LONG_BREAK
}

class PomodoroViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()
    
    private var timerJob: Job? = null
    private var remainingTimeInSeconds = 25 * 60 // 25 minutes in seconds
    private val workDuration = 25 * 60 // 25 minutes
    private val shortBreakDuration = 5 * 60 // 5 minutes
    private val longBreakDuration = 15 * 60 // 15 minutes
    
    init {
        updateTimeDisplay()
    }
    
    fun startTimer() {
        if (timerJob?.isActive == true) return
        
        _uiState.value = _uiState.value.copy(isPlaying = true)
        
        timerJob = viewModelScope.launch {
            while (remainingTimeInSeconds > 0 && _uiState.value.isPlaying) {
                delay(1000)
                remainingTimeInSeconds--
                updateTimeDisplay()
            }
            
            if (remainingTimeInSeconds <= 0) {
                onTimerComplete()
            }
        }
    }
    
    fun pauseTimer() {
        _uiState.value = _uiState.value.copy(isPlaying = false)
        timerJob?.cancel()
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
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
