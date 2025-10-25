package com.perseverance.pvc.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager {
    
    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
        private const val BACKGROUND_PERMISSION_REQUEST_CODE = 1002
        private const val ALARM_PERMISSION_REQUEST_CODE = 1003
        
        fun hasNotificationPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                // For Android 12 and below, notifications are enabled by default
                true
            }
        }
        
        fun requestNotificationPermission(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!hasNotificationPermission(activity)) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
        }
        
        fun shouldShowNotificationPermissionRationale(activity: Activity): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } else {
                false
            }
        }
        
        fun isNotificationPermissionRequestCode(requestCode: Int): Boolean {
            return requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE
        }
        
        // Background running permissions
        fun hasExactAlarmPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                alarmManager.canScheduleExactAlarms()
            } else {
                true // Permission not required for older versions
            }
        }
        
        fun requestExactAlarmPermission(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!hasExactAlarmPermission(activity)) {
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:${activity.packageName}")
                        }
                        activity.startActivityForResult(intent, ALARM_PERMISSION_REQUEST_CODE)
                    } catch (e: Exception) {
                        // Fallback to general settings
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${activity.packageName}")
                        }
                        activity.startActivity(intent)
                    }
                }
            }
        }
        
        fun isExactAlarmPermissionRequestCode(requestCode: Int): Boolean {
            return requestCode == ALARM_PERMISSION_REQUEST_CODE
        }
        
        // Battery optimization exemption
        fun requestBatteryOptimizationExemption(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!BackgroundOptimizationHelper.isBatteryOptimizationIgnored(activity)) {
                    BackgroundOptimizationHelper.requestBatteryOptimizationExemption(activity)
                }
            }
        }
        
        // Check if all background permissions are granted
        fun hasAllBackgroundPermissions(context: Context): Boolean {
            return hasNotificationPermission(context) && 
                   hasExactAlarmPermission(context) &&
                   BackgroundOptimizationHelper.isBatteryOptimizationIgnored(context)
        }
        
        // Request all background permissions
        fun requestAllBackgroundPermissions(activity: Activity) {
            // Request notification permission first
            requestNotificationPermission(activity)
            
            // Request exact alarm permission
            requestExactAlarmPermission(activity)
            
            // Request battery optimization exemption
            requestBatteryOptimizationExemption(activity)
        }
    }
}
