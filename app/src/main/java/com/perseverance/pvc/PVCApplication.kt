package com.perseverance.pvc

import android.app.Application
import com.perseverance.pvc.utils.AnalyticsHelper

class PVCApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AnalyticsHelper.initialize(this)
    }
}
