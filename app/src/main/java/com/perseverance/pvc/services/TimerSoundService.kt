package com.perseverance.pvc.services

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*

class TimerSoundService : Service() {
    
    private val binder = TimerSoundBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var toneGenerator: ToneGenerator? = null
    private var isPlaying = false
    private var soundJob: Job? = null
    
    inner class TimerSoundBinder : Binder() {
        fun getService(): TimerSoundService = this@TimerSoundService
    }
    
    override fun onBind(intent: Intent): IBinder = binder
    
    override fun onCreate() {
        super.onCreate()
        Log.d("TimerSoundService", "Service created")
    }
    
    fun startInfiniteSound() {
        Log.d("TimerSoundService", "startInfiniteSound called, isPlaying: $isPlaying")
        if (isPlaying) return
        
        try {
            // Stop any existing sound first
            stopInfiniteSound()
            
            // Try MediaPlayer first
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    
                    // Use system default notification sound
                    val notificationUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    Log.d("TimerSoundService", "Using notification URI: $notificationUri")
                    setDataSource(this@TimerSoundService, notificationUri)
                    
                    setOnPreparedListener { mp ->
                        Log.d("TimerSoundService", "MediaPlayer prepared, starting sound")
                        mp.isLooping = true
                        mp.start()
                        this@TimerSoundService.isPlaying = true
                        Log.d("TimerSoundService", "Sound started successfully")
                    }
                    
                    setOnErrorListener { _, what, extra ->
                        Log.e("TimerSoundService", "MediaPlayer error: what=$what, extra=$extra")
                        this@TimerSoundService.isPlaying = false
                        // Try fallback method
                        startFallbackSound()
                        true
                    }
                    
                    setOnCompletionListener {
                        Log.d("TimerSoundService", "MediaPlayer completed")
                        this@TimerSoundService.isPlaying = false
                    }
                    
                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e("TimerSoundService", "MediaPlayer failed, trying fallback", e)
                startFallbackSound()
            }
        } catch (e: Exception) {
            Log.e("TimerSoundService", "Error starting sound", e)
            isPlaying = false
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
        try {
            isPlaying = false
            
            // Stop MediaPlayer
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            }
            mediaPlayer = null
            
            // Stop ToneGenerator
            toneGenerator?.release()
            toneGenerator = null
            
            // Cancel coroutine job
            soundJob?.cancel()
            soundJob = null
            
            Log.d("TimerSoundService", "Sound stopped")
        } catch (e: Exception) {
            Log.e("TimerSoundService", "Error stopping sound", e)
        }
    }
    
    fun isSoundPlaying(): Boolean = isPlaying
    
    override fun onDestroy() {
        super.onDestroy()
        stopInfiniteSound()
        Log.d("TimerSoundService", "Service destroyed")
    }
}
