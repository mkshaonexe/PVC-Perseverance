package com.perseverance.pvc.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsHelper {
    
    private var analytics: FirebaseAnalytics? = null
    
    fun initialize(context: Context) {
        if (analytics == null) {
            analytics = FirebaseAnalytics.getInstance(context)
        }
    }
    
    /**
     * Log a screen view event
     */
    fun logScreenView(screenName: String, screenClass: String? = null) {
        analytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            screenClass?.let { putString(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
        })
    }
    
    /**
     * Log a custom event
     */
    fun logEvent(eventName: String, params: Map<String, Any>? = null) {
        analytics?.logEvent(eventName, Bundle().apply {
            params?.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                }
            }
        })
    }
    
    /**
     * Log study session start
     */
    fun logStudySessionStart(duration: Int) {
        logEvent("study_session_start", mapOf(
            "duration_minutes" to duration
        ))
    }
    
    /**
     * Log study session complete
     */
    fun logStudySessionComplete(duration: Int, actualTime: Long) {
        logEvent("study_session_complete", mapOf(
            "planned_duration_minutes" to duration,
            "actual_duration_seconds" to actualTime
        ))
    }
    
    /**
     * Log pomodoro timer start
     */
    fun logPomodoroStart(duration: Int) {
        logEvent("pomodoro_start", mapOf(
            "duration_minutes" to duration
        ))
    }
    
    /**
     * Log pomodoro timer complete
     */
    fun logPomodoroComplete(duration: Int) {
        logEvent("pomodoro_complete", mapOf(
            "duration_minutes" to duration
        ))
    }
    
    /**
     * Log settings change
     */
    fun logSettingsChange(settingName: String, newValue: String) {
        logEvent("settings_change", mapOf(
            "setting_name" to settingName,
            "new_value" to newValue
        ))
    }
    
    /**
     * Log feature usage
     */
    fun logFeatureUsage(featureName: String) {
        logEvent("feature_used", mapOf(
            "feature_name" to featureName
        ))
    }
    
    /**
     * Set user property
     */
    fun setUserProperty(name: String, value: String) {
        analytics?.setUserProperty(name, value)
    }
    
    /**
     * Set user ID
     */
    fun setUserId(userId: String) {
        analytics?.setUserId(userId)
    }
}
