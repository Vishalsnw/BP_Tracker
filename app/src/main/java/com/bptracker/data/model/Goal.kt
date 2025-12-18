package com.bptracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: GoalType,
    val targetSystolicMax: Int = 120,
    val targetDiastolicMax: Int = 80,
    val targetWeight: Double? = null,
    val dailyReadingTarget: Int = 2,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,
    val isActive: Boolean = true,
    val userId: Long = 0
) {
    val formattedStartDate: String
        get() = startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    
    val formattedEndDate: String?
        get() = endDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
}

enum class GoalType(val label: String, val description: String) {
    BLOOD_PRESSURE_TARGET("Blood Pressure Target", "Keep your BP within a healthy range"),
    DAILY_READINGS("Daily Tracking", "Take readings every day"),
    WEIGHT_TARGET("Weight Loss", "Reach your target weight"),
    CONSISTENCY_STREAK("Consistency Streak", "Build a measurement habit")
}

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: AchievementType,
    val unlockedAt: Long? = null,
    val progress: Int = 0,
    val userId: Long = 0
) {
    val isUnlocked: Boolean
        get() = unlockedAt != null
}

enum class AchievementType(
    val title: String,
    val description: String,
    val target: Int
) {
    FIRST_READING("First Step", "Record your first reading", 1),
    WEEK_STREAK("Week Warrior", "7-day measurement streak", 7),
    MONTH_STREAK("Monthly Master", "30-day measurement streak", 30),
    READINGS_10("Dedicated Tracker", "Record 10 readings", 10),
    READINGS_50("Health Champion", "Record 50 readings", 50),
    READINGS_100("BP Expert", "Record 100 readings", 100),
    NORMAL_STREAK_7("Healthy Week", "7 consecutive normal readings", 7),
    BREATHING_5("Mindful Breather", "Complete 5 breathing exercises", 5),
    WEIGHT_LOGGED("Weight Watcher", "Log your weight", 1)
}
