package com.perseverance.pvc.services

import android.app.Service
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
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.*

class TimerSoundService : Service() {
    
    private val binder = TimerSoundBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var toneGenerator: ToneGenerator? = null
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var soundJob: Job? = null
    
    // Audio parameters for 1000 Hz sine wave
    private val sampleRate = 44100
    private val frequency = 1000 // 1000 Hz
    private val duration = 0.1 // 0.1 seconds per beep
    private val beepInterval = 1000L // 1 second between beeps
    
    // Timeout parameters
    private val maxSoundDurationMillis = 120000L // 2 minutes max
    private var soundStartTime = 0L
    
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
        if (isPlaying) {
            Log.d("TimerSoundService", "Sound already playing, ignoring request")
            return
        }
        
        try {
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
            
            Log.d("TimerSoundService", "Sound stopped successfully")
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
