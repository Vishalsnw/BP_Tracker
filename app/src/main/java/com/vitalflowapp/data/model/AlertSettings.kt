package com.vitalflowapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alert_settings")
data class AlertSettings(
    @PrimaryKey
    val id: Long = 1,
    val systolicThreshold: Int = 140,
    val diastolicThreshold: Int = 90,
    val crisisAlertEnabled: Boolean = true,
    val thresholdAlertEnabled: Boolean = true,
    val weeklySummaryEnabled: Boolean = true,
    val weeklySummaryDay: Int = 1,
    val weeklySummaryHour: Int = 9,
    val missedReminderAlertEnabled: Boolean = false,
    val goalAchievedAlertEnabled: Boolean = true
)

data class AlertData(
    val type: AlertType,
    val title: String,
    val message: String,
    val reading: BloodPressureReading? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class AlertType {
    THRESHOLD_EXCEEDED,
    HYPERTENSIVE_CRISIS,
    WEEKLY_SUMMARY,
    GOAL_ACHIEVED,
    MISSED_REMINDER,
    STREAK_MILESTONE
}
