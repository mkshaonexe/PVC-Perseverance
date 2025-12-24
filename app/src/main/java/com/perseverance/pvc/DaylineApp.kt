package com.perseverance.pvc

import android.app.Application

class DaylineApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Firebase is initialized automatically by the ContentProvider.
        
        // Setup global exception handler to prevent hard crashes where possible (allows logging)
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("DaylineApp", "Uncaught Exception: ${throwable.message}", throwable)
             defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
