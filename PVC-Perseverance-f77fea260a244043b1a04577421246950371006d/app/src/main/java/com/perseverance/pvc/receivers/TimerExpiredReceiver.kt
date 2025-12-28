package com.perseverance.pvc.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.perseverance.pvc.services.TimerSoundService

class TimerExpiredReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_TIMER_EXPIRED = "com.perseverance.pvc.ACTION_TIMER_EXPIRED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("TimerExpiredReceiver", "Alarm received! Timer expired.")
        
        val sessionType = intent.getStringExtra("SESSION_TYPE")
        if (intent.action == ACTION_TIMER_EXPIRED) {
            startTimerSoundService(context, sessionType)
        }
    }

    private fun startTimerSoundService(context: Context, sessionType: String?) {
        val serviceIntent = Intent(context, TimerSoundService::class.java).apply {
            action = "ACTION_START_SOUND"
            putExtra("SESSION_TYPE", sessionType)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
