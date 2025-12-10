package com.perseverance.pvc.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.perseverance.pvc.MainActivity
import com.perseverance.pvc.R
import kotlinx.coroutines.*
import kotlin.math.*

class TimerSoundService : Service() {
    
    private val binder = TimerSoundBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var toneGenerator: ToneGenerator? = null
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var soundJob: Job? = null
    private var currentSessionType: String? = null
    
    // Audio parameters for 1000 Hz sine wave
    private val sampleRate = 44100
    private val frequency = 1000 // 1000 Hz
    private val duration = 0.1 // 0.1 seconds per beep
    private val beepInterval = 1000L // 1 second between beeps
    
    // Timeout parameters
    private val maxSoundDurationMillis = 120000L // 2 minutes max
    private var soundStartTime = 0L
    
    // Foreground service constants
    companion object {
        private const val FOREGROUND_SERVICE_ID = 2001
        private const val CHANNEL_ID = "timer_sound_service_high_priority"
        private const val CHANNEL_NAME = "Timer Sound Service"
        private const val CHANNEL_DESCRIPTION = "Keeps timer sounds playing in background"
    }
    
    inner class TimerSoundBinder : Binder() {
        fun getService(): TimerSoundService = this@TimerSoundService
    }
    
    override fun onBind(intent: Intent): IBinder = binder
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d("TimerSoundService", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TimerSoundService", "onStartCommand called with action: ${intent?.action}")
        
        when (intent?.action) {
            "ACTION_START_SOUND" -> {
                Log.d("TimerSoundService", "Starting infinite sound from Intent")
                if (intent.hasExtra("SESSION_TYPE")) {
                    currentSessionType = intent.getStringExtra("SESSION_TYPE")
                }
                startInfiniteSound()
            }
            "ACTION_STOP_SOUND" -> {
                Log.d("TimerSoundService", "Stopping infinite sound from Intent")
                stopInfiniteSound()
                stopSelf()
            }
        }
        
        return START_STICKY
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(false)
                enableLights(false)
                setSound(null, null) // Silent channel
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createForegroundNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val title: String
        val text: String
        
        when (currentSessionType) {
            "WORK" -> {
                title = "Pomodoro session done, take a break"
                text = "Great job! Time for a break. Tap to continue."
            }
            "SHORT_BREAK", "LONG_BREAK" -> {
                title = "Break Complete! ⏰"
                text = "Break time is over. Ready for your next work session?"
            }
            else -> {
                 title = "Pomodoro Timer Expired"
                 text = "Tap to stop the alarm."
            }
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }
    
    fun startInfiniteSound() {
        Log.d("TimerSoundService", "startInfiniteSound called, isPlaying: $isPlaying")
        if (isPlaying) {
            Log.d("TimerSoundService", "Sound already playing, ignoring request")
            return
        }
        
        try {
            // Start foreground service
            startForeground(FOREGROUND_SERVICE_ID, createForegroundNotification())
            
            // Stop any existing sound first
            stopInfiniteSound()
            
            // Record start time for timeout
            soundStartTime = System.currentTimeMillis()
            
            // Try 1000 Hz sine wave beep first
            try {
                startSineWaveBeep()
            } catch (e: Exception) {
                Log.e("TimerSoundService", "Sine wave beep failed, trying fallback", e)
                startFallbackSound()
            }
        } catch (e: Exception) {
            Log.e("TimerSoundService", "Error starting sound", e)
            isPlaying = false
        }
    }
    
    private fun startSineWaveBeep() {
        Log.d("TimerSoundService", "Starting 1000 Hz sine wave beep")
        try {
            isPlaying = true
            
            // Create a coroutine to play continuous beeps
            soundJob = CoroutineScope(Dispatchers.IO).launch {
                while (isPlaying) {
                    // Check if timeout has been reached
                    val elapsedTime = System.currentTimeMillis() - soundStartTime
                    if (elapsedTime >= maxSoundDurationMillis) {
                        Log.d("TimerSoundService", "Sound timeout reached (${elapsedTime}ms), stopping sound")
                        stopInfiniteSound()
                        break
                    }
                    
                    playSineWaveBeep()
                    delay(beepInterval)
                }
            }
            Log.d("TimerSoundService", "Sine wave beep started")
        } catch (e: Exception) {
            Log.e("TimerSoundService", "Error starting sine wave beep", e)
            isPlaying = false
        }
    }
    
    private suspend fun playSineWaveBeep() {
        try {
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .build()
            
            val samples = (sampleRate * duration).toInt()
            val buffer = ShortArray(samples)
            
            // Generate sine wave
            for (i in 0 until samples) {
                val angle = 2.0 * PI * frequency * i / sampleRate
                buffer[i] = (sin(angle) * Short.MAX_VALUE * 1.0).toInt().toShort()
            }
            
            audioTrack?.play()
            audioTrack?.write(buffer, 0, samples)
            
            // Wait for the beep to finish
            delay((duration * 1000).toLong())
            
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
            
        } catch (e: Exception) {
            Log.e("TimerSoundService", "Error playing sine wave beep", e)
        }
    }
    
    private fun startFallbackSound() {
        Log.d("TimerSoundService", "Starting fallback sound with ToneGenerator")
        try {
            toneGenerator = ToneGenerator(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100)
            isPlaying = true
            
            // Create a coroutine to play continuous beeps
            soundJob = CoroutineScope(Dispatchers.IO).launch {
                while (isPlaying) {
                    // Check if timeout has been reached
                    val elapsedTime = System.currentTimeMillis() - soundStartTime
                    if (elapsedTime >= maxSoundDurationMillis) {
                        Log.d("TimerSoundService", "Fallback sound timeout reached (${elapsedTime}ms), stopping sound")
                        stopInfiniteSound()
                        break
                    }
                    
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000)
                    delay(1500) // 1.5 second intervals
                }
            }
            Log.d("TimerSoundService", "Fallback sound started")
        } catch (e: Exception) {
            Log.e("TimerSoundService", "Error starting fallback sound", e)
            isPlaying = false
        }
    }
    
    fun stopInfiniteSound() {
        Log.d("TimerSoundService", "stopInfiniteSound called, isPlaying: $isPlaying")
        try {
            isPlaying = false
            soundStartTime = 0L // Reset start time
            
            // Stop MediaPlayer
            mediaPlayer?.let { mp ->
                try {
                    if (mp.isPlaying) {
                        mp.stop()
                    }
                    mp.release()
                } catch (e: Exception) {
                    Log.e("TimerSoundService", "Error stopping MediaPlayer", e)
                }
            }
            mediaPlayer = null
            
            // Stop AudioTrack
            audioTrack?.let { at ->
                try {
                    if (at.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        at.stop()
                    }
                    at.release()
                } catch (e: Exception) {
                    Log.e("TimerSoundService", "Error stopping AudioTrack", e)
                }
            }
            audioTrack = null
            
            // Stop ToneGenerator
            toneGenerator?.let { tg ->
                try {
                    tg.release()
                } catch (e: Exception) {
                    Log.e("TimerSoundService", "Error releasing ToneGenerator", e)
                }
            }
            toneGenerator = null
            
            // Cancel coroutine job
            soundJob?.let { job ->
                try {
                    job.cancel()
                } catch (e: Exception) {
                    Log.e("TimerSoundService", "Error canceling sound job", e)
                }
            }
            soundJob = null
            
            // Stop foreground service
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
            } catch (e: Exception) {
                Log.e("TimerSoundService", "Error stopping foreground service", e)
            }
            
            Log.d("TimerSoundService", "Sound stopped successfully")
        } catch (e: Exception) {
            Log.e("TimerSoundService", "Error stopping sound", e)
        }
    }
    
    fun isSoundPlaying(): Boolean = isPlaying
    
    fun setSessionType(type: String) {
        currentSessionType = type
        if (isPlaying) {
            startForeground(FOREGROUND_SERVICE_ID, createForegroundNotification())
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopInfiniteSound()
        Log.d("TimerSoundService", "Service destroyed")
    }
}
