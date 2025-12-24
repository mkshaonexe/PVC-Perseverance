package com.perseverance.pvc.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.perseverance.pvc.data.SettingsRepository
import com.perseverance.pvc.utils.AnalyticsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = SettingsRepository(application)
    
    // Dark Mode
    private val _darkMode = MutableStateFlow("Dark")
    val darkMode: StateFlow<String> = _darkMode.asStateFlow()
    
    // Use Timer in Background
    private val _useTimerInBackground = MutableStateFlow(true)
    val useTimerInBackground: StateFlow<Boolean> = _useTimerInBackground.asStateFlow()
    
    // Reset Session Every Day
    private val _resetSessionEveryDay = MutableStateFlow(false)
    val resetSessionEveryDay: StateFlow<Boolean> = _resetSessionEveryDay.asStateFlow()
    
    // Hide Navigation Bar
    private val _hideNavigationBar = MutableStateFlow(false)
    val hideNavigationBar: StateFlow<Boolean> = _hideNavigationBar.asStateFlow()
    
    // Hide Status Bar During Focus
    private val _hideStatusBarDuringFocus = MutableStateFlow(true)
    val hideStatusBarDuringFocus: StateFlow<Boolean> = _hideStatusBarDuringFocus.asStateFlow()
    
    // Follow System Font Settings
    private val _followSystemFontSettings = MutableStateFlow(false)
    val followSystemFontSettings: StateFlow<Boolean> = _followSystemFontSettings.asStateFlow()
    
    // Day Start Time
    private val _dayStartTime = MutableStateFlow("4:00 AM")
    val dayStartTime: StateFlow<String> = _dayStartTime.asStateFlow()
    
    // Language
    private val _language = MutableStateFlow("English")
    val language: StateFlow<String> = _language.asStateFlow()
    
    // Use Do Not Disturb During Focus
    private val _useDNDDuringFocus = MutableStateFlow(false)
    val useDNDDuringFocus: StateFlow<Boolean> = _useDNDDuringFocus.asStateFlow()
    
    // Timer Duration
    private val _timerDuration = MutableStateFlow("50")
    val timerDuration: StateFlow<String> = _timerDuration.asStateFlow()
    
    // Break Duration
    private val _breakDuration = MutableStateFlow("10")
    val breakDuration: StateFlow<String> = _breakDuration.asStateFlow()
    
    // Enable Timer Notifications
    private val _enableTimerNotifications = MutableStateFlow(true)
    val enableTimerNotifications: StateFlow<Boolean> = _enableTimerNotifications.asStateFlow()
    
    // Onboarding Completed
    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            repository.getDarkMode().collect { _darkMode.value = it }
        }
        viewModelScope.launch {
            repository.getUseTimerInBackground().collect { _useTimerInBackground.value = it }
        }
        viewModelScope.launch {
            repository.getResetSessionEveryDay().collect { _resetSessionEveryDay.value = it }
        }
        viewModelScope.launch {
            repository.getHideNavigationBar().collect { _hideNavigationBar.value = it }
        }
        viewModelScope.launch {
            repository.getHideStatusBarDuringFocus().collect { _hideStatusBarDuringFocus.value = it }
        }
        viewModelScope.launch {
            repository.getFollowSystemFontSettings().collect { _followSystemFontSettings.value = it }
        }
        viewModelScope.launch {
            repository.getDayStartTime().collect { _dayStartTime.value = it }
        }
        viewModelScope.launch {
            repository.getLanguage().collect { _language.value = it }
        }
        viewModelScope.launch {
            repository.getUseDNDDuringFocus().collect { _useDNDDuringFocus.value = it }
        }
        viewModelScope.launch {
            repository.getTimerDuration().collect { _timerDuration.value = it }
        }
        viewModelScope.launch {
            repository.getBreakDuration().collect { _breakDuration.value = it }
        }
        viewModelScope.launch {
            repository.getEnableTimerNotifications().collect { _enableTimerNotifications.value = it }
        }
        viewModelScope.launch {
            repository.getOnboardingCompleted().collect { _onboardingCompleted.value = it }
        }
    }
    
    // Update functions
    fun updateDarkMode(mode: String) {
        viewModelScope.launch {
            _darkMode.value = mode
            repository.setDarkMode(mode)
        }
        AnalyticsHelper.logEvent("settings_change", mapOf("setting" to "dark_mode", "value" to mode))
    }
    
    fun updateUseTimerInBackground(enabled: Boolean) {
        viewModelScope.launch {
            _useTimerInBackground.value = enabled
            repository.setUseTimerInBackground(enabled)
        }
        AnalyticsHelper.logEvent("settings_change", mapOf("setting" to "bg_timer", "value" to enabled.toString()))
    }
    
    fun updateResetSessionEveryDay(enabled: Boolean) {
        viewModelScope.launch {
            _resetSessionEveryDay.value = enabled
            repository.setResetSessionEveryDay(enabled)
        }
        AnalyticsHelper.logEvent("settings_change", mapOf("setting" to "reset_session", "value" to enabled.toString()))
    }
    
    fun updateHideNavigationBar(enabled: Boolean) {
        viewModelScope.launch {
            _hideNavigationBar.value = enabled
            repository.setHideNavigationBar(enabled)
        }
        AnalyticsHelper.logEvent("settings_change", mapOf("setting" to "hide_nav", "value" to enabled.toString()))
    }
    
    fun updateHideStatusBarDuringFocus(enabled: Boolean) {
        viewModelScope.launch {
            _hideStatusBarDuringFocus.value = enabled
            repository.setHideStatusBarDuringFocus(enabled)
        }
        AnalyticsHelper.logEvent("settings_change", mapOf("setting" to "hide_status_bar", "value" to enabled.toString()))
    }
    
    fun updateFollowSystemFontSettings(enabled: Boolean) {
        viewModelScope.launch {
            _followSystemFontSettings.value = enabled
            repository.setFollowSystemFontSettings(enabled)
        }
        AnalyticsHelper.logEvent("settings_change", mapOf("setting" to "system_font", "value" to enabled.toString()))
    }
    
    fun updateDayStartTime(time: String) {
        viewModelScope.launch {
            _dayStartTime.value = time
            repository.setDayStartTime(time)
        }
        AnalyticsHelper.logEvent("settings_change", mapOf("setting" to "day_start", "value" to time))
    }
    
    fun updateLanguage(language: String) {
        viewModelScope.launch {
            _language.value = language
            repository.setLanguage(language)
        }
        AnalyticsHelper.logEvent("settings_change", mapOf("setting" to "language", "value" to language))
    }
    
    fun updateUseDNDDuringFocus(enabled: Boolean) {
        viewModelScope.launch {
            _useDNDDuringFocus.value = enabled
            repository.setUseDNDDuringFocus(enabled)
        }
        AnalyticsHelper.logEvent("settings_change", mapOf("setting" to "dnd", "value" to enabled.toString()))
    }
    
    fun updateTimerDuration(duration: String) {
        viewModelScope.launch {
            _timerDuration.value = duration
            repository.setTimerDuration(duration)
        }
        AnalyticsHelper.logEvent("settings_change", mapOf("setting" to "timer_duration", "value" to duration))
    }
    
    fun updateBreakDuration(duration: String) {
        viewModelScope.launch {
            _breakDuration.value = duration
            repository.setBreakDuration(duration)
        }
        AnalyticsHelper.logEvent("settings_change", mapOf("setting" to "break_duration", "value" to duration))
    }
    
    fun updateEnableTimerNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _enableTimerNotifications.value = enabled
            repository.setEnableTimerNotifications(enabled)
        }
        AnalyticsHelper.logEvent("settings_change", mapOf("setting" to "notifications", "value" to enabled.toString()))
    }
    
    fun completeOnboarding() {
            _onboardingCompleted.value = true
            repository.setOnboardingCompleted(true)
        }
        AnalyticsHelper.logEvent("onboarding_complete")
    }
}

