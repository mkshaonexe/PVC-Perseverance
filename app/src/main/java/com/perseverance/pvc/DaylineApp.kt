package com.perseverance.pvc

import android.app.Application
import com.google.firebase.FirebaseApp

class DaylineApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Firebase is typically initialized automatically by the ContentProvider, 
        // but explicit initialization can be useful for debugging or specific configurations.
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            android.util.Log.e("DaylineApp", "Failed to initialize Firebase", e)
        }
        
        // Setup global exception handler to prevent hard crashes where possible (allows logging)
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("DaylineApp", "Uncaught Exception: ${throwable.message}", throwable)
            // Optional: You could save this to a local file or try to upload it if you had a custom backend
            
            // Re-throw or pass to default handler to let the app close 'naturally' after logging
            // or swallow it (dangerous, but prevents "Stopped" dialog sometimes)
             defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
