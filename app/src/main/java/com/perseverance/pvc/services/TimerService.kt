package com.perseverance.pvc.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.perseverance.pvc.MainActivity
import com.perseverance.pvc.R
import com.perseverance.pvc.data.SettingsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

class TimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null
    
    private lateinit var settingsRepository: SettingsRepository
    private var notificationsEnabled = true

    companion object {
        const val CHANNEL_ID = "pomodoro_timer_channel"
        const val NOTIFICATION_ID = 777
        
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESET = "ACTION_RESET"
        const val EXTRA_DURATION = "EXTRA_DURATION"
        const val EXTRA_DAILY_TOTAL_SECONDS = "EXTRA_DAILY_TOTAL_SECONDS"
        const val EXTRA_SESSION_INITIAL_SECONDS = "EXTRA_SESSION_INITIAL_SECONDS"
        const val EXTRA_IS_STUDY_SESSION = "EXTRA_IS_STUDY_SESSION"

        // Singleton state for UI to observe
        private val _remainingSeconds = MutableStateFlow(50 * 60)
        val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()
        
        private val _isTimerRunning = MutableStateFlow(false)
        val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()
        
        // Expose function to update initial time if needed from ViewModel (e.g. settings change)
        fun updateRemainingTimeLocally(seconds: Int) {
            if (!_isTimerRunning.value) {
                _remainingSeconds.value = seconds
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(applicationContext)
        createNotificationChannel()
        
        // Observe settings
        serviceScope.launch {
            settingsRepository.getEnableTimerNotifications().collect { enabled ->
                notificationsEnabled = enabled
                if (enabled && _isTimerRunning.value) {
                    updateNotification()
                } else if (!enabled) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                }
            }
        }
    }

    private var dailyTotalSeconds = 0
    private var sessionInitialDuration = 0
    private var isStudySession = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val duration = intent.getIntExtra(EXTRA_DURATION, -1)
                
                // Read extras for study tracking
                if (intent.hasExtra(EXTRA_DAILY_TOTAL_SECONDS)) {
                    dailyTotalSeconds = intent.getIntExtra(EXTRA_DAILY_TOTAL_SECONDS, 0)
                }
                if (intent.hasExtra(EXTRA_SESSION_INITIAL_SECONDS)) {
                    sessionInitialDuration = intent.getIntExtra(EXTRA_SESSION_INITIAL_SECONDS, 0)
                }
                if (intent.hasExtra(EXTRA_IS_STUDY_SESSION)) {
                    isStudySession = intent.getBooleanExtra(EXTRA_IS_STUDY_SESSION, false)
                }

                // If duration is passed and we are not running/resuming, update it.
                // Logic: If paused, we resume from current state. If reset/fresh, we might use duration.
                startTimer()
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESET -> {
                val duration = intent.getIntExtra(EXTRA_DURATION, 50 * 60)
                resetTimer(duration)
            }
        }
        return START_NOT_STICKY
    }

    private fun startTimer() {
        if (_isTimerRunning.value) return

        _isTimerRunning.value = true
        startForegroundService()

        timerJob = serviceScope.launch {
            while (_remainingSeconds.value > 0 && _isTimerRunning.value) {
                updateNotification()
                delay(1000)
                _remainingSeconds.value -= 1
            }
            
            if (_remainingSeconds.value <= 0) {
                stopTimer()
                _isTimerRunning.value = false
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
        }
    }

    private fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        updateNotification()
        // We keep the notification (detached), but update text
        stopForeground(STOP_FOREGROUND_DETACH) 
    }

    private fun resetTimer(duration: Int) {
        pauseTimer()
        _remainingSeconds.value = duration
        updateNotification()
        stopSelf() 
    }

    private fun stopTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        updateNotification()
    }

    private fun startForegroundService() {
        if (!notificationsEnabled) return

        val notification = buildNotification()
        
        // Start foreground with type if needed (Android 14+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
             try {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
             } catch (e: Exception) {
                // Fallback or log if permission missing (though we added it)
                startForeground(NOTIFICATION_ID, notification)
             }
        } else {
             startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification() {
        if (!notificationsEnabled) return
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): android.app.Notification {
        val minutes = _remainingSeconds.value / 60
        val seconds = _remainingSeconds.value % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)
        
        var content = if (_isTimerRunning.value) "Focusing... $timeString" else "Paused $timeString"
        
        // Add total study time if applicable
        if (isStudySession) {
            val sessionElapsed = if (sessionInitialDuration > 0) sessionInitialDuration - _remainingSeconds.value else 0
            val currentTotal = dailyTotalSeconds + sessionElapsed.coerceAtLeast(0)
            
            val totalHours = currentTotal / 3600
            val totalMinutes = (currentTotal % 3600) / 60
            val totalSeconds = currentTotal % 60
            val totalString = String.format("%02d:%02d:%02d", totalHours, totalMinutes, totalSeconds)
            
            content += " | Total: $totalString"
        }
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pomodoro Timer")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground) 
            .setOngoing(_isTimerRunning.value)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true) 
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pomodoro Timer",
                NotificationManager.IMPORTANCE_LOW // Low importance so it doesn't pop up sound every second
            ).apply {
                description = "Shows the active Pomodoro timer"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
    }
}
