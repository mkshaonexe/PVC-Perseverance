package com.perseverance.pvc

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class PVCApplication : Application() {
    
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "pvc_notifications"
        const val NOTIFICATION_CHANNEL_NAME = "PVC Notifications"
        const val TIMER_CHANNEL_ID = "timer_notifications"
        const val TIMER_CHANNEL_NAME = "Timer Notifications"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize Firebase Analytics
        FirebaseAnalytics.getInstance(this)
        
        // Initialize Firebase Crashlytics
        FirebaseCrashlytics.getInstance().apply {
            setCrashlyticsCollectionEnabled(true)
            // Log app initialization
            log("PVCApplication initialized")
        }
        
        // Create notification channels
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            
            // General notifications channel
            val generalChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "General app notifications and push messages"
                enableVibration(true)
                enableLights(true)
            }
            
            // Timer notifications channel
            val timerChannel = NotificationChannel(
                TIMER_CHANNEL_ID,
                TIMER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pomodoro timer notifications"
                enableVibration(true)
                enableLights(true)
            }
            
            notificationManager.createNotificationChannel(generalChannel)
            notificationManager.createNotificationChannel(timerChannel)
        }
    }
}
