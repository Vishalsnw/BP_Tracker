package com.bptracker.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bptracker.R
import com.bptracker.data.model.BloodPressureReading
import java.time.LocalDateTime

data class TrendAlert(
    val type: TrendAlertType,
    val message: String,
    val severity: TrendSeverity,
    val avgChange: Double
)

enum class TrendAlertType {
    RISING_SYSTOLIC,
    RISING_DIASTOLIC,
    FALLING_SYSTOLIC,
    FALLING_DIASTOLIC,
    HIGH_VARIABILITY,
    CONSISTENT_HIGH,
    CONSISTENT_LOW,
    IMPROVING
}

enum class TrendSeverity {
    INFO,
    WARNING,
    ALERT
}

object TrendAnalyzer {
    
    private const val TREND_CHANNEL_ID = "trend_alerts"
    private const val TREND_NOTIFICATION_ID = 8888
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Trend Alerts"
            val descriptionText = "Notifications for blood pressure trends"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(TREND_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun analyzeRecentTrends(readings: List<BloodPressureReading>, days: Int = 7): List<TrendAlert> {
        if (readings.size < 3) return emptyList()
        
        val alerts = mutableListOf<TrendAlert>()
        val cutoff = LocalDateTime.now().minusDays(days.toLong())
        val recentReadings = readings.filter { it.timestamp.isAfter(cutoff) }
            .sortedBy { it.timestamp }
        
        if (recentReadings.size < 3) return emptyList()
        
        val firstHalf = recentReadings.take(recentReadings.size / 2)
        val secondHalf = recentReadings.drop(recentReadings.size / 2)
        
        val firstAvgSys = firstHalf.map { it.systolic }.average()
        val secondAvgSys = secondHalf.map { it.systolic }.average()
        val sysChange = secondAvgSys - firstAvgSys
        
        val firstAvgDia = firstHalf.map { it.diastolic }.average()
        val secondAvgDia = secondHalf.map { it.diastolic }.average()
        val diaChange = secondAvgDia - firstAvgDia
        
        if (sysChange > 10) {
            alerts.add(TrendAlert(
                type = TrendAlertType.RISING_SYSTOLIC,
                message = "Your systolic blood pressure has increased by ${sysChange.toInt()} mmHg on average over the past $days days.",
                severity = if (sysChange > 20) TrendSeverity.ALERT else TrendSeverity.WARNING,
                avgChange = sysChange
            ))
        } else if (sysChange < -10) {
            alerts.add(TrendAlert(
                type = TrendAlertType.FALLING_SYSTOLIC,
                message = "Your systolic blood pressure has decreased by ${(-sysChange).toInt()} mmHg on average.",
                severity = TrendSeverity.INFO,
                avgChange = sysChange
            ))
        }
        
        if (diaChange > 8) {
            alerts.add(TrendAlert(
                type = TrendAlertType.RISING_DIASTOLIC,
                message = "Your diastolic blood pressure has increased by ${diaChange.toInt()} mmHg on average over the past $days days.",
                severity = if (diaChange > 15) TrendSeverity.ALERT else TrendSeverity.WARNING,
                avgChange = diaChange
            ))
        } else if (diaChange < -8) {
            alerts.add(TrendAlert(
                type = TrendAlertType.FALLING_DIASTOLIC,
                message = "Your diastolic blood pressure has decreased by ${(-diaChange).toInt()} mmHg on average.",
                severity = TrendSeverity.INFO,
                avgChange = diaChange
            ))
        }
        
        val sysVariance = recentReadings.map { it.systolic }.let { values ->
            val mean = values.average()
            values.map { (it - mean) * (it - mean) }.average()
        }
        val sysStdDev = kotlin.math.sqrt(sysVariance)
        
        if (sysStdDev > 15) {
            alerts.add(TrendAlert(
                type = TrendAlertType.HIGH_VARIABILITY,
                message = "Your blood pressure readings show high variability. Consider measuring at consistent times.",
                severity = TrendSeverity.INFO,
                avgChange = sysStdDev
            ))
        }
        
        val highReadings = recentReadings.filter { 
            it.systolic >= 140 || it.diastolic >= 90 
        }
        if (highReadings.size > recentReadings.size * 0.7) {
            alerts.add(TrendAlert(
                type = TrendAlertType.CONSISTENT_HIGH,
                message = "Over 70% of your recent readings are in the high blood pressure range. Consider consulting a doctor.",
                severity = TrendSeverity.WARNING,
                avgChange = 0.0
            ))
        }
        
        val normalReadings = recentReadings.filter {
            it.systolic < 120 && it.diastolic < 80
        }
        if (normalReadings.size > recentReadings.size * 0.8) {
            alerts.add(TrendAlert(
                type = TrendAlertType.IMPROVING,
                message = "Great job! Over 80% of your recent readings are in the normal range.",
                severity = TrendSeverity.INFO,
                avgChange = 0.0
            ))
        }
        
        return alerts
    }
    
    fun showTrendNotification(context: Context, alert: TrendAlert) {
        createNotificationChannel(context)
        
        val icon = when (alert.severity) {
            TrendSeverity.ALERT -> R.drawable.ic_notification
            TrendSeverity.WARNING -> R.drawable.ic_notification
            TrendSeverity.INFO -> R.drawable.ic_notification
        }
        
        val title = when (alert.type) {
            TrendAlertType.RISING_SYSTOLIC -> "Blood Pressure Rising"
            TrendAlertType.RISING_DIASTOLIC -> "Diastolic Pressure Rising"
            TrendAlertType.FALLING_SYSTOLIC -> "Blood Pressure Improving"
            TrendAlertType.FALLING_DIASTOLIC -> "Diastolic Improving"
            TrendAlertType.HIGH_VARIABILITY -> "High BP Variability"
            TrendAlertType.CONSISTENT_HIGH -> "Consistently High BP"
            TrendAlertType.CONSISTENT_LOW -> "Consistently Low BP"
            TrendAlertType.IMPROVING -> "Great Progress!"
        }
        
        val notification = NotificationCompat.Builder(context, TREND_CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(alert.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(alert.message))
            .setPriority(when (alert.severity) {
                TrendSeverity.ALERT -> NotificationCompat.PRIORITY_HIGH
                TrendSeverity.WARNING -> NotificationCompat.PRIORITY_DEFAULT
                TrendSeverity.INFO -> NotificationCompat.PRIORITY_LOW
            })
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(
                TREND_NOTIFICATION_ID + alert.type.ordinal,
                notification
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    fun calculateAverageForPeriod(
        readings: List<BloodPressureReading>,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Triple<Double, Double, Double>? {
        val periodReadings = readings.filter {
            it.timestamp.isAfter(startDate) && it.timestamp.isBefore(endDate)
        }
        
        if (periodReadings.isEmpty()) return null
        
        return Triple(
            periodReadings.map { it.systolic }.average(),
            periodReadings.map { it.diastolic }.average(),
            periodReadings.map { it.pulse }.average()
        )
    }
}
