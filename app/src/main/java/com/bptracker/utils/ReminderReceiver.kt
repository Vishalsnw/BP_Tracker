package com.bptracker.utils

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bptracker.BloodPressureApp
import com.bptracker.MainActivity
import com.bptracker.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", 0)
        val label = intent.getStringExtra("reminder_label") ?: "Time to measure your blood pressure"
        val hour = intent.getIntExtra("reminder_hour", 8)
        val minute = intent.getIntExtra("reminder_minute", 0)
        val daysArray = intent.getIntArrayExtra("reminder_days")
        
        showNotification(context, reminderId, label)
        
        if (daysArray != null && daysArray.isNotEmpty()) {
            scheduleNextOccurrence(context, reminderId, label, hour, minute, daysArray)
        }
    }
    
    private fun showNotification(context: Context, reminderId: Long, label: String) {
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, BloodPressureApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Blood Pressure Reminder")
            .setContentText(label)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(reminderId.toInt(), notification)
    }
    
    private fun scheduleNextOccurrence(
        context: Context,
        reminderId: Long,
        label: String,
        hour: Int,
        minute: Int,
        daysArray: IntArray
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val daysOfWeek = daysArray.map { DayOfWeek.of(it) }.toSet()
        val reminderTime = LocalTime.of(hour, minute)
        
        val today = LocalDate.now()
        val currentDayOfWeek = today.dayOfWeek
        
        val nextDay = daysOfWeek
            .map { day ->
                val daysUntil = (day.value - currentDayOfWeek.value + 7) % 7
                if (daysUntil == 0) {
                    7
                } else {
                    daysUntil
                }
            }
            .minOrNull() ?: return
        
        val nextDate = today.plusDays(nextDay.toLong())
        val nextDateTime = LocalDateTime.of(nextDate, reminderTime)
        val triggerTime = nextDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val newIntent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminderId)
            putExtra("reminder_label", label)
            putExtra("reminder_hour", hour)
            putExtra("reminder_minute", minute)
            putExtra("reminder_days", daysArray)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            newIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
}
