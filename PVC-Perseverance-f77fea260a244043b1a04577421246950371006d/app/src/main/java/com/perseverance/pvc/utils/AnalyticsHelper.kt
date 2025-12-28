package com.perseverance.pvc.utils

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.FirebaseApp

object AnalyticsHelper {
    private var firebaseAnalytics: FirebaseAnalytics? = null

    init {
        try {
            val context = FirebaseApp.getInstance().applicationContext
            firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        } catch (e: Exception) {
            Log.e("AnalyticsHelper", "Firebase Analytics not available", e)
        }
    }

    fun logScreenView(screenName: String, screenClass: String = screenName) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        safeLogEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logTimerStart(sessionType: String, subject: String) {
        val params = mapOf(
            "session_type" to sessionType,
            "subject" to subject
        )
        logEvent("timer_start", params)
    }

    fun logTimerComplete(sessionType: String, durationMinutes: Int, subject: String) {
        val params = mapOf(
            "session_type" to sessionType,
            "duration_minutes" to durationMinutes.toString(),
            "subject" to subject
        )
        logEvent("timer_complete", params)
    }
    
    fun logTimerAbandon(sessionType: String, durationPlayedSeconds: Int) {
        val params = mapOf(
            "session_type" to sessionType,
            "duration_played_seconds" to durationPlayedSeconds.toString()
        )
        logEvent("timer_abandon", params)
    }

    fun logStreakUpdate(newStreak: Int) {
        val params = mapOf(
            "streak_count" to newStreak.toString()
        )
        logEvent("streak_updated", params)
        setUserProperty("user_streak", newStreak.toString())
    }

    fun logMissionJoin(missionId: String, missionName: String) {
        val params = mapOf(
            "mission_id" to missionId,
            "mission_name" to missionName
        )
        logEvent("mission_join", params)
    }

    fun logMissionCreate(title: String, targetHours: Int) {
        val params = mapOf(
            "mission_title" to title,
            "target_hours" to targetHours.toString()
        )
        logEvent("mission_create", params)
    }

    fun logLogin(method: String) {
        val params = mapOf("method" to method)
        logEvent("login", params)
    }

    fun logLogout() {
        logEvent("logout")
    }

    fun logTutorialBegin() {
        logEvent(FirebaseAnalytics.Event.TUTORIAL_BEGIN)
    }

    fun logTutorialComplete() {
        logEvent(FirebaseAnalytics.Event.TUTORIAL_COMPLETE)
    }

    fun logTutorialStep(stepIndex: Int, stepName: String) {
        val params = mapOf(
            "step_index" to stepIndex.toString(),
            "step_name" to stepName
        )
        logEvent("tutorial_step", params)
    }

    fun logInsightView(viewType: String) {
        val params = mapOf("view_type" to viewType)
        logEvent("insight_view", params)
    }

    fun logSectionClick(sectionName: String) {
        val params = mapOf("section_name" to sectionName)
        logEvent("section_click", params)
    }

    fun logSettingsChange(settingName: String, newValue: String) {
        val params = mapOf(
            "setting_name" to settingName,
            "new_value" to newValue
        )
        logEvent("settings_change", params)
    }
    
    fun setUserProperty(name: String, value: String) {
        try {
            firebaseAnalytics?.setUserProperty(name, value)
        } catch (e: Exception) {
            Log.e("AnalyticsHelper", "Failed to set user property: $name", e)
        }
    }

    fun logEvent(eventName: String, params: Map<String, String> = emptyMap()) {
        val bundle = Bundle()
        params.forEach { (key, value) ->
            bundle.putString(key, value)
        }
        safeLogEvent(eventName, bundle)
    }

    private fun safeLogEvent(name: String, params: Bundle) {
        try {
            firebaseAnalytics?.logEvent(name, params)
        } catch (e: Exception) {
            Log.e("AnalyticsHelper", "Failed to log event: $name", e)
        }
    }
}
