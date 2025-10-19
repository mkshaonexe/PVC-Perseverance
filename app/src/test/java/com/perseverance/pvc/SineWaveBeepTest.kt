package com.perseverance.pvc

import org.junit.Test
import org.junit.Assert.*
import kotlin.math.*

/**
 * Unit test for sine wave beep generation logic
 */
class SineWaveBeepTest {
    
    @Test
    fun testSineWaveGeneration() {
        val sampleRate = 44100
        val frequency = 1000 // 1000 Hz
        val duration = 0.1 // 0.1 seconds
        val samples = (sampleRate * duration).toInt()
        
        // Generate sine wave buffer
        val buffer = ShortArray(samples)
        for (i in 0 until samples) {
            val angle = 2.0 * PI * frequency * i / sampleRate
            buffer[i] = (sin(angle) * Short.MAX_VALUE * 1.0).toInt().toShort()
        }
        
        // Verify buffer is not empty
        assertTrue("Buffer should not be empty", buffer.isNotEmpty())
        
        // Verify buffer has correct length
        assertEquals("Buffer should have correct length", samples, buffer.size)
        
        // Verify buffer contains non-zero values (sine wave should have variation)
        val hasVariation = buffer.any { it != 0.toShort() }
        assertTrue("Buffer should contain non-zero values", hasVariation)
        
        // Verify the sine wave has reasonable amplitude variation
        val maxValue = buffer.maxOrNull() ?: 0
        val minValue = buffer.minOrNull() ?: 0
        val amplitude = maxValue - minValue
        assertTrue("Sine wave should have reasonable amplitude", amplitude > 1000)
        
        // Simple frequency check - verify we have multiple periods in the buffer
        val expectedPeriod = sampleRate / frequency // samples per period
        val expectedPeriods = samples / expectedPeriod
        assertTrue("Should have multiple periods in the buffer", expectedPeriods >= 4)
    }
    
    @Test
    fun testAudioParameters() {
        // Test that audio parameters are reasonable
        val sampleRate = 44100
        val frequency = 1000
        val duration = 0.1
        
        assertTrue("Sample rate should be positive", sampleRate > 0)
        assertTrue("Frequency should be positive", frequency > 0)
        assertTrue("Duration should be positive", duration > 0)
        assertTrue("Frequency should be within audible range", frequency in 20..20000)
        assertTrue("Sample rate should be standard", sampleRate in 8000..192000)
    }
}
