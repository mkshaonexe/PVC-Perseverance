package com.perseverance.pvc.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.perseverance.pvc.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = SettingsRepository(application)
    
    // Dark Mode
    private val _darkMode = MutableStateFlow("System")
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
    private val _dayStartTime = MutableStateFlow("12:00 AM")
    val dayStartTime: StateFlow<String> = _dayStartTime.asStateFlow()
    
    // Language
    private val _language = MutableStateFlow("English")
    val language: StateFlow<String> = _language.asStateFlow()
    
    // Use Do Not Disturb During Focus
    private val _useDNDDuringFocus = MutableStateFlow(false)
    val useDNDDuringFocus: StateFlow<Boolean> = _useDNDDuringFocus.asStateFlow()
    
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
    }
    
    // Update functions
    fun updateDarkMode(mode: String) {
        viewModelScope.launch {
            _darkMode.value = mode
            repository.setDarkMode(mode)
        }
    }
    
    fun updateUseTimerInBackground(enabled: Boolean) {
        viewModelScope.launch {
            _useTimerInBackground.value = enabled
            repository.setUseTimerInBackground(enabled)
        }
    }
    
    fun updateResetSessionEveryDay(enabled: Boolean) {
        viewModelScope.launch {
            _resetSessionEveryDay.value = enabled
            repository.setResetSessionEveryDay(enabled)
        }
    }
    
    fun updateHideNavigationBar(enabled: Boolean) {
        viewModelScope.launch {
            _hideNavigationBar.value = enabled
            repository.setHideNavigationBar(enabled)
        }
    }
    
    fun updateHideStatusBarDuringFocus(enabled: Boolean) {
        viewModelScope.launch {
            _hideStatusBarDuringFocus.value = enabled
            repository.setHideStatusBarDuringFocus(enabled)
        }
    }
    
    fun updateFollowSystemFontSettings(enabled: Boolean) {
        viewModelScope.launch {
            _followSystemFontSettings.value = enabled
            repository.setFollowSystemFontSettings(enabled)
        }
    }
    
    fun updateDayStartTime(time: String) {
        viewModelScope.launch {
            _dayStartTime.value = time
            repository.setDayStartTime(time)
        }
    }
    
    fun updateLanguage(language: String) {
        viewModelScope.launch {
            _language.value = language
            repository.setLanguage(language)
        }
    }
    
    fun updateUseDNDDuringFocus(enabled: Boolean) {
        viewModelScope.launch {
            _useDNDDuringFocus.value = enabled
            repository.setUseDNDDuringFocus(enabled)
        }
    }
}

