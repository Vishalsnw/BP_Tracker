package com.bptracker.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bptracker.MainActivity
import com.bptracker.R
import com.bptracker.data.model.AlertSettings
import com.bptracker.data.model.AlertType
import com.bptracker.data.model.BloodPressureCategory
import com.bptracker.data.model.BloodPressureReading
import com.bptracker.data.model.WeeklySummary
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID_ALERTS = "bp_alerts"
        const val CHANNEL_ID_SUMMARY = "bp_summary"
        const val CHANNEL_ID_CRISIS = "bp_crisis"
        
        const val NOTIFICATION_ID_THRESHOLD = 1001
        const val NOTIFICATION_ID_CRISIS = 1002
        const val NOTIFICATION_ID_SUMMARY = 1003
        const val NOTIFICATION_ID_GOAL = 1004
    }
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            
            val alertChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                "Blood Pressure Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when blood pressure exceeds thresholds"
                enableVibration(true)
            }
            
            val summaryChannel = NotificationChannel(
                CHANNEL_ID_SUMMARY,
                "Weekly Summaries",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Weekly blood pressure summary notifications"
            }
            
            val crisisChannel = NotificationChannel(
                CHANNEL_ID_CRISIS,
                "Emergency Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts for hypertensive crisis"
                enableVibration(true)
            }
            
            notificationManager.createNotificationChannels(listOf(alertChannel, summaryChannel, crisisChannel))
        }
    }
    
    fun checkAndAlertReading(reading: BloodPressureReading, settings: AlertSettings) {
        if (reading.category == BloodPressureCategory.HIGH && settings.crisisAlertEnabled) {
            sendCrisisAlert(reading)
            return
        }
        
        if (settings.thresholdAlertEnabled) {
            if (reading.systolic >= settings.systolicThreshold || 
                reading.diastolic >= settings.diastolicThreshold) {
                sendThresholdExceededAlert(reading, settings)
            }
        }
    }
    
    private fun sendCrisisAlert(reading: BloodPressureReading) {
        if (!hasNotificationPermission()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_CRISIS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("HYPERTENSIVE CRISIS ALERT")
            .setContentText("Your reading ${reading.systolic}/${reading.diastolic} indicates a crisis. Seek immediate medical attention!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your blood pressure reading of ${reading.systolic}/${reading.diastolic} mmHg indicates a hypertensive crisis. " +
                        "This is a medical emergency. Please seek immediate medical attention or call emergency services."))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_CRISIS, notification)
    }
    
    private fun sendThresholdExceededAlert(reading: BloodPressureReading, settings: AlertSettings) {
        if (!hasNotificationPermission()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val exceededText = buildString {
            if (reading.systolic >= settings.systolicThreshold) {
                append("Systolic (${reading.systolic}) exceeds ${settings.systolicThreshold} mmHg. ")
            }
            if (reading.diastolic >= settings.diastolicThreshold) {
                append("Diastolic (${reading.diastolic}) exceeds ${settings.diastolicThreshold} mmHg.")
            }
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Blood Pressure Alert")
            .setContentText("Your reading ${reading.systolic}/${reading.diastolic} exceeds your thresholds")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$exceededText\n\nConsider rest and re-measure in 15 minutes. " +
                        "If readings remain high, consult your healthcare provider."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_THRESHOLD, notification)
    }
    
    fun sendWeeklySummaryNotification(summary: WeeklySummary) {
        if (!hasNotificationPermission()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val trendText = when (summary.trendVsPrevWeek) {
            com.bptracker.data.model.TrendDirection.IMPROVING -> "Your BP has improved compared to last week!"
            com.bptracker.data.model.TrendDirection.WORSENING -> "Your BP has increased compared to last week."
            com.bptracker.data.model.TrendDirection.STABLE -> "Your BP has remained stable."
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SUMMARY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Weekly BP Summary")
            .setContentText("${summary.totalReadings} readings this week. Avg: ${String.format("%.0f", summary.avgSystolic)}/${String.format("%.0f", summary.avgDiastolic)}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Week of ${summary.weekStartDate} - ${summary.weekEndDate}\n\n" +
                        "Total Readings: ${summary.totalReadings}\n" +
                        "Average: ${String.format("%.0f", summary.avgSystolic)}/${String.format("%.0f", summary.avgDiastolic)} mmHg\n" +
                        "Normal Readings: ${String.format("%.0f", summary.normalPercentage)}%\n\n" +
                        trendText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_SUMMARY, notification)
    }
    
    fun sendGoalAchievedNotification(title: String, message: String) {
        if (!hasNotificationPermission()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_GOAL, notification)
    }
    
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
