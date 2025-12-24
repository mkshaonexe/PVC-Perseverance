package com.perseverance.pvc.utils

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object AnalyticsHelper {
    private var firebaseAnalytics: FirebaseAnalytics? = null

    init {
        try {
            firebaseAnalytics = Firebase.analytics
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
