package com.perseverance.pvc.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.perseverance.pvc.MainActivity
import com.perseverance.pvc.PVCApplication
import com.perseverance.pvc.R

class PVCFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "PVCFCMService"
    }
    
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d(TAG, "Message received from: ${message.from}")
        
        // Log to Crashlytics for debugging
        FirebaseCrashlytics.getInstance().log("FCM message received: ${message.messageId}")
        
        // Handle notification payload
        message.notification?.let { notification ->
            val title = notification.title ?: "PVC Study App"
            val body = notification.body ?: ""
            
            showNotification(title, body, message.data)
        }
        
        // Handle data payload (for custom in-app messages)
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")
            handleDataPayload(message.data)
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        Log.d(TAG, "New FCM token: $token")
        
        // Log to Crashlytics
        FirebaseCrashlytics.getInstance().log("New FCM token generated")
        
        // TODO: Send token to your server if needed
        // For now, just log it
        sendTokenToServer(token)
    }
    
    private fun handleDataPayload(data: Map<String, String>) {
        // Handle custom data messages
        val type = data["type"]
        val title = data["title"] ?: "PVC Study App"
        val body = data["message"] ?: data["body"] ?: ""
        
        when (type) {
            "study_reminder" -> {
                showNotification(title, body, data)
            }
            "achievement" -> {
                showNotification(title, body, data)
            }
            "group_message" -> {
                showNotification(title, body, data)
            }
            else -> {
                // Default notification
                showNotification(title, body, data)
            }
        }
    }
    
    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // Add data to intent if needed
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notificationBuilder = NotificationCompat.Builder(this, PVCApplication.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        
        Log.d(TAG, "Notification displayed: $title - $body")
    }
    
    private fun sendTokenToServer(token: String) {
        // TODO: Implement server token registration if needed
        // For now, just log the token
        Log.d(TAG, "FCM Token: $token")
        
        // You can save this to SharedPreferences or send to your backend
        val prefs = getSharedPreferences("fcm_prefs", MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
    }
}
