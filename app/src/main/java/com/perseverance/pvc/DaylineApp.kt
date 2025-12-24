package com.perseverance.pvc

import android.app.Application
import com.google.firebase.FirebaseApp

class DaylineApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Firebase is typically initialized automatically by the ContentProvider, 
        // but explicit initialization can be useful for debugging or specific configurations.
        FirebaseApp.initializeApp(this)
    }
}
