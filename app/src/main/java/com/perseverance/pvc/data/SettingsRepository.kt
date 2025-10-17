package com.perseverance.pvc.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class SettingsRepository(private val context: Context) {
    
    companion object {
        // Keys for settings
        private val DARK_MODE_KEY = stringPreferencesKey("dark_mode")
        private val USE_TIMER_IN_BACKGROUND_KEY = booleanPreferencesKey("use_timer_in_background")
        private val RESET_SESSION_EVERY_DAY_KEY = booleanPreferencesKey("reset_session_every_day")
        private val HIDE_NAVIGATION_BAR_KEY = booleanPreferencesKey("hide_navigation_bar")
        private val HIDE_STATUS_BAR_DURING_FOCUS_KEY = booleanPreferencesKey("hide_status_bar_during_focus")
        private val FOLLOW_SYSTEM_FONT_SETTINGS_KEY = booleanPreferencesKey("follow_system_font_settings")
        private val DAY_START_TIME_KEY = stringPreferencesKey("day_start_time")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val USE_DND_DURING_FOCUS_KEY = booleanPreferencesKey("use_dnd_during_focus")
    }
    
    // Dark Mode
    suspend fun setDarkMode(mode: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = mode
        }
    }
    
    fun getDarkMode(): Flow<String> {
        return context.settingsDataStore.data.map { preferences ->
            preferences[DARK_MODE_KEY] ?: "System"
        }
    }
    
    // Use Timer in Background
    suspend fun setUseTimerInBackground(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[USE_TIMER_IN_BACKGROUND_KEY] = enabled
        }
    }
    
    fun getUseTimerInBackground(): Flow<Boolean> {
        return context.settingsDataStore.data.map { preferences ->
            preferences[USE_TIMER_IN_BACKGROUND_KEY] ?: true
        }
    }
    
    // Reset Session Every Day
    suspend fun setResetSessionEveryDay(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[RESET_SESSION_EVERY_DAY_KEY] = enabled
        }
    }
    
    fun getResetSessionEveryDay(): Flow<Boolean> {
        return context.settingsDataStore.data.map { preferences ->
            preferences[RESET_SESSION_EVERY_DAY_KEY] ?: false
        }
    }
    
    // Hide Navigation Bar
    suspend fun setHideNavigationBar(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[HIDE_NAVIGATION_BAR_KEY] = enabled
        }
    }
    
    fun getHideNavigationBar(): Flow<Boolean> {
        return context.settingsDataStore.data.map { preferences ->
            preferences[HIDE_NAVIGATION_BAR_KEY] ?: false
        }
    }
    
    // Hide Status Bar During Focus
    suspend fun setHideStatusBarDuringFocus(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[HIDE_STATUS_BAR_DURING_FOCUS_KEY] = enabled
        }
    }
    
    fun getHideStatusBarDuringFocus(): Flow<Boolean> {
        return context.settingsDataStore.data.map { preferences ->
            preferences[HIDE_STATUS_BAR_DURING_FOCUS_KEY] ?: true
        }
    }
    
    // Follow System Font Settings
    suspend fun setFollowSystemFontSettings(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[FOLLOW_SYSTEM_FONT_SETTINGS_KEY] = enabled
        }
    }
    
    fun getFollowSystemFontSettings(): Flow<Boolean> {
        return context.settingsDataStore.data.map { preferences ->
            preferences[FOLLOW_SYSTEM_FONT_SETTINGS_KEY] ?: false
        }
    }
    
    // Day Start Time
    suspend fun setDayStartTime(time: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[DAY_START_TIME_KEY] = time
        }
    }
    
    fun getDayStartTime(): Flow<String> {
        return context.settingsDataStore.data.map { preferences ->
            preferences[DAY_START_TIME_KEY] ?: "12:00 AM"
        }
    }
    
    // Language
    suspend fun setLanguage(language: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }
    
    fun getLanguage(): Flow<String> {
        return context.settingsDataStore.data.map { preferences ->
            preferences[LANGUAGE_KEY] ?: "English"
        }
    }
    
    // Use Do Not Disturb During Focus
    suspend fun setUseDNDDuringFocus(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[USE_DND_DURING_FOCUS_KEY] = enabled
        }
    }
    
    fun getUseDNDDuringFocus(): Flow<Boolean> {
        return context.settingsDataStore.data.map { preferences ->
            preferences[USE_DND_DURING_FOCUS_KEY] ?: false
        }
    }
}

