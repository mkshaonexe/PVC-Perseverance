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
    }
}
