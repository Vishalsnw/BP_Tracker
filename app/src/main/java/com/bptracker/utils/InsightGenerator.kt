package com.bptracker.utils

import com.bptracker.data.model.BloodPressureCategory
import com.bptracker.data.model.BloodPressureReading
import com.bptracker.data.model.InsightAction
import com.bptracker.data.model.InsightCard
import com.bptracker.data.model.InsightType
import com.bptracker.data.model.TimeOfDayStats
import com.bptracker.data.model.TimePeriod
import com.bptracker.data.model.TrendDirection
import com.bptracker.data.model.WeeklySummary
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsightGenerator @Inject constructor() {
    
    fun generateInsights(
        readings: List<BloodPressureReading>,
        previousWeekReadings: List<BloodPressureReading>
    ): List<InsightCard> {
        val insights = mutableListOf<InsightCard>()
        
        if (readings.isEmpty()) {
            insights.add(createConsistencyInsight())
            return insights
        }
        
        val thisWeekAvgSystolic = readings.map { it.systolic }.average()
        val thisWeekAvgDiastolic = readings.map { it.diastolic }.average()
        
        if (previousWeekReadings.isNotEmpty()) {
            val prevWeekAvgSystolic = previousWeekReadings.map { it.systolic }.average()
            val prevWeekAvgDiastolic = previousWeekReadings.map { it.diastolic }.average()
            
            val systolicChange = thisWeekAvgSystolic - prevWeekAvgSystolic
            val diastolicChange = thisWeekAvgDiastolic - prevWeekAvgDiastolic
            
            insights.add(createTrendInsight(systolicChange, diastolicChange))
        }
        
        val timeOfDayStats = analyzeTimeOfDay(readings)
        val morningStats = timeOfDayStats.find { it.period == TimePeriod.MORNING }
        val eveningStats = timeOfDayStats.find { it.period == TimePeriod.EVENING }
        
        if (morningStats != null && eveningStats != null) {
            if (morningStats.avgSystolic > eveningStats.avgSystolic + 10) {
                insights.add(createMorningHighInsight(morningStats))
            } else if (eveningStats.avgSystolic > morningStats.avgSystolic + 10) {
                insights.add(createEveningHighInsight(eveningStats))
            }
        }
        
        val stressCorrelation = analyzeStressCorrelation(readings)
        if (stressCorrelation > 0.5) {
            insights.add(createStressCorrelationInsight())
        }
        
        val moodCorrelation = analyzeMoodCorrelation(readings)
        if (moodCorrelation > 0.5) {
            insights.add(createMoodCorrelationInsight())
        }
        
        if (readings.size < 7) {
            insights.add(createMoreDataNeededInsight(readings.size))
        }
        
        val normalPercentage = readings.count { it.category == BloodPressureCategory.NORMAL }.toFloat() / readings.size
        if (normalPercentage > 0.7) {
            insights.add(createGoodProgressInsight(normalPercentage))
        }
        
        insights.add(createRandomTipInsight())
        
        return insights.sortedByDescending { it.priority }
    }
    
    fun analyzeTimeOfDay(readings: List<BloodPressureReading>): List<TimeOfDayStats> {
        return TimePeriod.values().mapNotNull { period ->
            val periodReadings = readings.filter { reading ->
                val hour = reading.timestamp.hour
                when (period) {
                    TimePeriod.MORNING -> hour in 5..11
                    TimePeriod.AFTERNOON -> hour in 12..17
                    TimePeriod.EVENING -> hour in 18..22
                    TimePeriod.NIGHT -> hour >= 23 || hour <= 4
                }
            }
            
            if (periodReadings.isEmpty()) null
            else TimeOfDayStats(
                period = period,
                avgSystolic = periodReadings.map { it.systolic }.average(),
                avgDiastolic = periodReadings.map { it.diastolic }.average(),
                avgPulse = periodReadings.map { it.pulse }.average(),
                readingCount = periodReadings.size
            )
        }
    }
    
    fun generateWeeklySummary(
        thisWeekReadings: List<BloodPressureReading>,
        lastWeekReadings: List<BloodPressureReading>,
        weekStart: LocalDateTime,
        weekEnd: LocalDateTime
    ): WeeklySummary {
        val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")
        
        val thisWeekAvgSystolic = if (thisWeekReadings.isNotEmpty()) 
            thisWeekReadings.map { it.systolic }.average() else 0.0
        val thisWeekAvgDiastolic = if (thisWeekReadings.isNotEmpty()) 
            thisWeekReadings.map { it.diastolic }.average() else 0.0
        val thisWeekAvgPulse = if (thisWeekReadings.isNotEmpty()) 
            thisWeekReadings.map { it.pulse }.average() else 0.0
        
        val normalCount = thisWeekReadings.count { it.category == BloodPressureCategory.NORMAL }
        val normalPercentage = if (thisWeekReadings.isNotEmpty()) 
            normalCount.toFloat() / thisWeekReadings.size * 100 else 0f
        
        var trend = TrendDirection.STABLE
        var systolicChange = 0.0
        var diastolicChange = 0.0
        
        if (lastWeekReadings.isNotEmpty() && thisWeekReadings.isNotEmpty()) {
            val lastWeekAvgSystolic = lastWeekReadings.map { it.systolic }.average()
            val lastWeekAvgDiastolic = lastWeekReadings.map { it.diastolic }.average()
            
            systolicChange = thisWeekAvgSystolic - lastWeekAvgSystolic
            diastolicChange = thisWeekAvgDiastolic - lastWeekAvgDiastolic
            
            trend = when {
                systolicChange < -5 || diastolicChange < -3 -> TrendDirection.IMPROVING
                systolicChange > 5 || diastolicChange > 3 -> TrendDirection.WORSENING
                else -> TrendDirection.STABLE
            }
        }
        
        return WeeklySummary(
            weekStartDate = weekStart.format(dateFormatter),
            weekEndDate = weekEnd.format(dateFormatter),
            totalReadings = thisWeekReadings.size,
            avgSystolic = thisWeekAvgSystolic,
            avgDiastolic = thisWeekAvgDiastolic,
            avgPulse = thisWeekAvgPulse,
            normalPercentage = normalPercentage,
            trendVsPrevWeek = trend,
            systolicChange = systolicChange,
            diastolicChange = diastolicChange
        )
    }
    
    private fun analyzeStressCorrelation(readings: List<BloodPressureReading>): Double {
        if (readings.size < 5) return 0.0
        
        val highStressReadings = readings.filter { it.stressLevel >= 4 }
        val lowStressReadings = readings.filter { it.stressLevel <= 2 }
        
        if (highStressReadings.isEmpty() || lowStressReadings.isEmpty()) return 0.0
        
        val highStressAvg = highStressReadings.map { it.systolic }.average()
        val lowStressAvg = lowStressReadings.map { it.systolic }.average()
        
        return if (highStressAvg > lowStressAvg + 10) 0.8 else 0.2
    }
    
    private fun analyzeMoodCorrelation(readings: List<BloodPressureReading>): Double {
        if (readings.size < 5) return 0.0
        
        val lowMoodReadings = readings.filter { it.mood <= 2 }
        val highMoodReadings = readings.filter { it.mood >= 4 }
        
        if (lowMoodReadings.isEmpty() || highMoodReadings.isEmpty()) return 0.0
        
        val lowMoodAvg = lowMoodReadings.map { it.systolic }.average()
        val highMoodAvg = highMoodReadings.map { it.systolic }.average()
        
        return if (lowMoodAvg > highMoodAvg + 8) 0.7 else 0.2
    }
    
    private fun createTrendInsight(systolicChange: Double, diastolicChange: Double): InsightCard {
        val isImproving = systolicChange < -3 || diastolicChange < -2
        val isWorsening = systolicChange > 3 || diastolicChange > 2
        
        return if (isImproving) {
            InsightCard(
                type = InsightType.TREND_IMPROVEMENT,
                title = "Great Progress!",
                message = "Your average blood pressure has decreased by ${String.format("%.0f", -systolicChange)}/${String.format("%.0f", -diastolicChange)} mmHg compared to last week.",
                priority = 10,
                actionType = InsightAction.NAVIGATE_STATISTICS
            )
        } else if (isWorsening) {
            InsightCard(
                type = InsightType.TREND_WORSENING,
                title = "Attention Needed",
                message = "Your average blood pressure has increased by ${String.format("%.0f", systolicChange)}/${String.format("%.0f", diastolicChange)} mmHg compared to last week.",
                priority = 15,
                actionType = InsightAction.NAVIGATE_ARTICLES
            )
        } else {
            InsightCard(
                type = InsightType.TREND_IMPROVEMENT,
                title = "Stable Readings",
                message = "Your blood pressure has remained stable compared to last week. Keep up the good work!",
                priority = 5,
                actionType = InsightAction.NAVIGATE_STATISTICS
            )
        }
    }
    
    private fun createMorningHighInsight(morningStats: TimeOfDayStats): InsightCard {
        return InsightCard(
            type = InsightType.PATTERN_MORNING_HIGH,
            title = "Morning Blood Pressure Pattern",
            message = "Your blood pressure tends to be higher in the morning (avg ${String.format("%.0f", morningStats.avgSystolic)}/${String.format("%.0f", morningStats.avgDiastolic)}). This is common and known as 'morning surge'.",
            priority = 8,
            actionType = InsightAction.NAVIGATE_ARTICLES
        )
    }
    
    private fun createEveningHighInsight(eveningStats: TimeOfDayStats): InsightCard {
        return InsightCard(
            type = InsightType.PATTERN_EVENING_HIGH,
            title = "Evening Blood Pressure Pattern",
            message = "Your blood pressure tends to be higher in the evening (avg ${String.format("%.0f", eveningStats.avgSystolic)}/${String.format("%.0f", eveningStats.avgDiastolic)}). Consider relaxation techniques before bedtime.",
            priority = 7,
            actionType = InsightAction.NAVIGATE_ARTICLES
        )
    }
    
    private fun createStressCorrelationInsight(): InsightCard {
        return InsightCard(
            type = InsightType.PATTERN_STRESS_CORRELATION,
            title = "Stress Affects Your BP",
            message = "Your readings show higher blood pressure during stressful times. Try breathing exercises to help manage stress.",
            priority = 9,
            actionType = InsightAction.NAVIGATE_ARTICLES
        )
    }
    
    private fun createMoodCorrelationInsight(): InsightCard {
        return InsightCard(
            type = InsightType.PATTERN_MOOD_CORRELATION,
            title = "Mood and Blood Pressure",
            message = "Your blood pressure tends to be lower when you're in a positive mood. Focus on activities that make you happy!",
            priority = 6,
            actionType = InsightAction.NAVIGATE_ARTICLES
        )
    }
    
    private fun createConsistencyInsight(): InsightCard {
        return InsightCard(
            type = InsightType.CONSISTENCY_NEEDS_IMPROVEMENT,
            title = "Start Tracking",
            message = "Add your first blood pressure reading to start tracking your heart health journey.",
            priority = 20,
            actionType = InsightAction.NAVIGATE_ADD_READING
        )
    }
    
    private fun createMoreDataNeededInsight(currentCount: Int): InsightCard {
        return InsightCard(
            type = InsightType.CONSISTENCY_NEEDS_IMPROVEMENT,
            title = "Keep It Up!",
            message = "You have $currentCount readings this week. Aim for at least 14 readings (twice daily) for better insights.",
            priority = 4,
            actionType = InsightAction.NAVIGATE_ADD_READING
        )
    }
    
    private fun createGoodProgressInsight(normalPercentage: Float): InsightCard {
        return InsightCard(
            type = InsightType.GOAL_PROGRESS,
            title = "Excellent Control!",
            message = "${String.format("%.0f", normalPercentage * 100)}% of your recent readings are in the normal range. Great job maintaining healthy blood pressure!",
            priority = 8,
            actionType = InsightAction.NAVIGATE_STATISTICS
        )
    }
    
    private fun createRandomTipInsight(): InsightCard {
        val tips = listOf(
            Triple("Salt Intake", "Reducing sodium intake to less than 2,300mg daily can help lower blood pressure.", InsightAction.NAVIGATE_ARTICLES),
            Triple("Stay Active", "Regular physical activity of 30 minutes most days can help reduce blood pressure.", InsightAction.NAVIGATE_ARTICLES),
            Triple("Measure Right", "Always sit quietly for 5 minutes before taking a measurement for accurate results.", InsightAction.NAVIGATE_ARTICLES),
            Triple("Hydration", "Staying well-hydrated helps maintain healthy blood pressure levels.", InsightAction.NAVIGATE_ARTICLES),
            Triple("Sleep Quality", "Poor sleep can contribute to higher blood pressure. Aim for 7-8 hours nightly.", InsightAction.NAVIGATE_ARTICLES),
            Triple("Limit Caffeine", "Caffeine can cause temporary spikes in blood pressure. Monitor your intake.", InsightAction.NAVIGATE_ARTICLES),
            Triple("Potassium Rich Foods", "Foods high in potassium like bananas and spinach can help lower blood pressure.", InsightAction.NAVIGATE_ARTICLES),
            Triple("Deep Breathing", "Just 5 minutes of deep breathing can help reduce stress and lower blood pressure.", InsightAction.NAVIGATE_ARTICLES)
        )
        
        val tip = tips.random()
        return InsightCard(
            type = InsightType.TIP,
            title = tip.first,
            message = tip.second,
            priority = 2,
            actionType = tip.third
        )
    }
}
