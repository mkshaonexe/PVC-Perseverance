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
            android.widget.Toast.makeText(this, "Firebase Initialized Successfully", android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            android.util.Log.e("DaylineApp", "Failed to initialize Firebase", e)
            android.widget.Toast.makeText(this, "Firebase Init FAILED: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
        
        // Setup global exception handler to prevent hard crashes where possible (allows logging)
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("DaylineApp", "Uncaught Exception: ${throwable.message}", throwable)
             defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
