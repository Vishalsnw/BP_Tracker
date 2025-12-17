package com.bptracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "insights")
data class InsightCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: InsightType,
    val title: String,
    val message: String,
    val priority: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,
    val isDismissed: Boolean = false,
    val actionType: InsightAction? = null,
    val actionData: String? = null
)

enum class InsightType {
    TREND_IMPROVEMENT,
    TREND_WORSENING,
    PATTERN_MORNING_HIGH,
    PATTERN_EVENING_HIGH,
    PATTERN_STRESS_CORRELATION,
    PATTERN_MOOD_CORRELATION,
    CONSISTENCY_GOOD,
    CONSISTENCY_NEEDS_IMPROVEMENT,
    MILESTONE_REACHED,
    MEDICATION_EFFECTIVENESS,
    GOAL_PROGRESS,
    TIP,
    WARNING
}

enum class InsightAction {
    NAVIGATE_STATISTICS,
    NAVIGATE_HISTORY,
    NAVIGATE_ADD_READING,
    NAVIGATE_ARTICLES,
    NAVIGATE_GOALS,
    NAVIGATE_MEDICATIONS,
    DISMISS
}

data class TimeOfDayStats(
    val period: TimePeriod,
    val avgSystolic: Double,
    val avgDiastolic: Double,
    val avgPulse: Double,
    val readingCount: Int
)

enum class TimePeriod(val label: String, val startHour: Int, val endHour: Int) {
    MORNING("Morning", 5, 11),
    AFTERNOON("Afternoon", 12, 17),
    EVENING("Evening", 18, 22),
    NIGHT("Night", 23, 4)
}

data class WeeklySummary(
    val weekStartDate: String,
    val weekEndDate: String,
    val totalReadings: Int,
    val avgSystolic: Double,
    val avgDiastolic: Double,
    val avgPulse: Double,
    val normalPercentage: Float,
    val trendVsPrevWeek: TrendDirection,
    val systolicChange: Double,
    val diastolicChange: Double
)

enum class TrendDirection {
    IMPROVING,
    STABLE,
    WORSENING
}
