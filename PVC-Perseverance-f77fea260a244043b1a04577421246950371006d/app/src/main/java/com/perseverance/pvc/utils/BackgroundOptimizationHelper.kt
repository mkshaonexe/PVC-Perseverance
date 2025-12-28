package com.perseverance.pvc.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

class BackgroundOptimizationHelper {
    
    companion object {
        private const val TAG = "BackgroundOptimizationHelper"
        
        /**
         * Check if the app is whitelisted from battery optimizations
         */
        fun isBatteryOptimizationIgnored(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            } else {
                true // Battery optimization doesn't exist in older versions
            }
        }
        
        /**
         * Request to ignore battery optimizations for this app
         * This will show a system dialog to the user
         */
        @SuppressLint("BatteryLife")
        fun requestBatteryOptimizationExemption(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!isBatteryOptimizationIgnored(context)) {
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                        Log.d(TAG, "Battery optimization exemption requested")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error requesting battery optimization exemption", e)
                        // Fallback: Open battery optimization settings
                        openBatteryOptimizationSettings(context)
                    }
                } else {
                    Log.d(TAG, "App is already exempt from battery optimization")
                }
            }
        }
        
        /**
         * Open the battery optimization settings page
         */
        private fun openBatteryOptimizationSettings(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Log.d(TAG, "Opened battery optimization settings")
            } catch (e: Exception) {
                Log.e(TAG, "Error opening battery optimization settings", e)
            }
        }
        
        /**
         * Check if the device has aggressive power management that might affect the app
         */
        fun hasAggressivePowerManagement(): Boolean {
            // Check for known manufacturers with aggressive power management
            val manufacturer = Build.MANUFACTURER.lowercase()
            val aggressiveManufacturers = listOf(
                "xiaomi", "huawei", "honor", "oppo", "vivo", "oneplus", "samsung"
            )
            return aggressiveManufacturers.any { manufacturer.contains(it) }
        }
        
        /**
         * Get a user-friendly message about battery optimization
         */
        fun getBatteryOptimizationMessage(context: Context): String {
            return if (isBatteryOptimizationIgnored(context)) {
                "Background functionality is enabled. Your timers will work properly when the app is minimized."
            } else {
                "For the best experience, please allow the app to run in the background. This ensures your timers continue working when the app is minimized."
            }
        }
    }
}
