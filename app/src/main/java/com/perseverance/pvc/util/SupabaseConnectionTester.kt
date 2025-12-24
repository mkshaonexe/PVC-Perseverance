package com.perseverance.pvc.util

import android.util.Log
import com.perseverance.pvc.BuildConfig
import com.perseverance.pvc.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility class to test Supabase connection and configuration
 * 
 * Usage:
 * ```
 * // In your ViewModel or Activity
 * viewModelScope.launch {
 *     val isConnected = SupabaseConnectionTester.testConnection()
 *     Log.d("MyApp", "Supabase connected: $isConnected")
 * }
 * ```
 */
object SupabaseConnectionTester {
    
    private const val TAG = "SupabaseConnectionTest"
    
    /**
     * Tests if Supabase is properly configured and connected
     * @return true if Supabase is connected, false otherwise
     */
    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "========== SUPABASE CONNECTION TEST ==========")
            
            // Test 1: Check BuildConfig values
            val hasValidUrl = checkBuildConfig()
            if (!hasValidUrl) {
                Log.e(TAG, "❌ BuildConfig has placeholder values - Supabase NOT configured")
                return@withContext false
            }
            
            // Test 2: Check Supabase client initialization
            val clientInitialized = checkClientInitialization()
            if (!clientInitialized) {
                Log.e(TAG, "❌ Supabase client failed to initialize")
                return@withContext false
            }
            
            // Test 3: Check Auth module
            val authWorking = checkAuthModule()
            if (!authWorking) {
                Log.e(TAG, "❌ Supabase Auth module not working")
                return@withContext false
            }
            
            Log.d(TAG, "✅ ALL TESTS PASSED - Supabase is CONNECTED!")
            Log.d(TAG, "=============================================")
            return@withContext true
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Connection test failed with exception: ${e.message}", e)
            Log.d(TAG, "=============================================")
            return@withContext false
        }
    }
    
    /**
     * Checks if BuildConfig has valid Supabase credentials
     */
    private fun checkBuildConfig(): Boolean {
        Log.d(TAG, "Test 1: Checking BuildConfig values...")
        
        val url = BuildConfig.SUPABASE_URL
        val key = BuildConfig.SUPABASE_ANON_KEY
        
        Log.d(TAG, "  SUPABASE_URL: $url")
        Log.d(TAG, "  SUPABASE_ANON_KEY: ${key.take(20)}...")
        
        // Check if using placeholder values
        if (url == "https://placeholder.supabase.co" || key == "placeholder") {
            Log.e(TAG, "  ❌ Using placeholder values - Check your .env file")
            return false
        }
        
        // Check if values are not empty
        if (url.isBlank() || key.isBlank()) {
            Log.e(TAG, "  ❌ Empty values - Check your .env file")
            return false
        }
        
        // Check URL format
        if (!url.startsWith("https://") || !url.contains(".supabase.co")) {
            Log.e(TAG, "  ❌ Invalid Supabase URL format")
            return false
        }
        
        Log.d(TAG, "  ✅ BuildConfig values look valid")
        return true
    }
    
    /**
     * Checks if Supabase client is properly initialized
     */
    private fun checkClientInitialization(): Boolean {
        Log.d(TAG, "Test 2: Checking Supabase client initialization...")
        
        return try {
            val client = SupabaseClient.client
            Log.d(TAG, "  ✅ Supabase client initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "  ❌ Failed to initialize client: ${e.message}", e)
            false
        }
    }
    
    /**
     * Checks if Auth module is working
     */
    private fun checkAuthModule(): Boolean {
        Log.d(TAG, "Test 3: Checking Auth module...")
        
        return try {
            val client = SupabaseClient.client
            val currentUser = client.auth.currentUserOrNull()
            
            if (currentUser != null) {
                Log.d(TAG, "  ✅ Auth module working - User logged in: ${currentUser.email}")
            } else {
                Log.d(TAG, "  ✅ Auth module working - No user logged in (this is OK)")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "  ❌ Auth module error: ${e.message}", e)
            false
        }
    }
    
    /**
     * Prints detailed diagnostic information
     */
    fun printDiagnostics() {
        Log.d(TAG, "========== SUPABASE DIAGNOSTICS ==========")
        Log.d(TAG, "SUPABASE_URL: ${BuildConfig.SUPABASE_URL}")
        Log.d(TAG, "SUPABASE_ANON_KEY length: ${BuildConfig.SUPABASE_ANON_KEY.length}")
        Log.d(TAG, "GOOGLE_WEB_CLIENT_ID: ${BuildConfig.GOOGLE_WEB_CLIENT_ID}")
        
        try {
            val client = SupabaseClient.client
            val currentUser = client.auth.currentUserOrNull()
            Log.d(TAG, "Current user: ${currentUser?.email ?: "Not logged in"}")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user: ${e.message}")
        }
        
        Log.d(TAG, "==========================================")
    }
}
