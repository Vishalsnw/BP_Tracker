package com.bptracker.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bptracker.R
import com.bptracker.data.model.BloodPressureReading
import com.bptracker.data.model.UserProfile

object EmergencyAlertManager {
    
    private const val CRISIS_CHANNEL_ID = "crisis_alerts"
    private const val CRISIS_NOTIFICATION_ID = 9999
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Crisis Alerts"
            val descriptionText = "Notifications for hypertensive crisis readings"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CRISIS_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
            }
            
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun checkAndAlertCrisis(
        context: Context,
        reading: BloodPressureReading,
        profile: UserProfile?
    ): Boolean {
        if (!reading.isCrisis) return false
        
        showCrisisNotification(context, reading)
        
        if (profile?.enableCrisisAlerts == true && 
            !profile.emergencyContactPhone.isNullOrBlank()) {
            sendEmergencySms(context, reading, profile)
        }
        
        return true
    }
    
    private fun showCrisisNotification(context: Context, reading: BloodPressureReading) {
        createNotificationChannel(context)
        
        val emergencyIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:911")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            emergencyIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CRISIS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Hypertensive Crisis Alert!")
            .setContentText("Reading ${reading.systolic}/${reading.diastolic} mmHg requires immediate attention")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your blood pressure reading of ${reading.systolic}/${reading.diastolic} mmHg " +
                        "indicates a hypertensive crisis. Please seek immediate medical attention. " +
                        "Tap to call emergency services."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_call,
                "Call 911",
                pendingIntent
            )
            .build()
        
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
            == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(CRISIS_NOTIFICATION_ID, notification)
        }
    }
    
    private fun sendEmergencySms(
        context: Context,
        reading: BloodPressureReading,
        profile: UserProfile
    ) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) 
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                context, 
                "SMS permission required to send emergency alerts", 
                Toast.LENGTH_LONG
            ).show()
            return
        }
        
        try {
            val message = "ALERT: ${profile.name}'s blood pressure reading is critically high: " +
                    "${reading.systolic}/${reading.diastolic} mmHg. " +
                    "This may indicate a hypertensive crisis. " +
                    "Please check on them or call emergency services."
            
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            
            smsManager.sendTextMessage(
                profile.emergencyContactPhone,
                null,
                message,
                null,
                null
            )
            
            Toast.makeText(
                context,
                "Emergency alert sent to ${profile.emergencyContactName}",
                Toast.LENGTH_SHORT
            ).show()
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "Failed to send emergency SMS: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    fun promptEmergencyCall(context: Context) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:911")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
