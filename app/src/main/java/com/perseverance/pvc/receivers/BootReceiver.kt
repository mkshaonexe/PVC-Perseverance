package com.perseverance.pvc.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Receives boot completion broadcasts to restore app state after device restart
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.d(TAG, "Boot completed or package updated, performing initialization")
                
                try {
                    // Here you can add logic to restore any persistent timers or app state
                    // For now, we'll just log that the receiver was triggered
                    Log.d(TAG, "Perseverance PVC app initialized after boot/update")
                    
                    // You could potentially restart any saved timer states here
                    // or initialize notification channels, etc.
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error during boot initialization", e)
                }
            }
        }
    }
}
