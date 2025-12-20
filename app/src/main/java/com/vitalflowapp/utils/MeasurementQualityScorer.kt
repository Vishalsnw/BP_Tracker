package com.vitalflowapp.utils

import com.vitalflowapp.data.model.BloodPressureReading
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

data class MeasurementQuality(
    val overallScore: Int,
    val consistencyScore: Int,
    val frequencyScore: Int,
    val techniqueScore: Int,
    val tips: List<String>,
    val grade: QualityGrade
)

enum class QualityGrade(val label: String, val description: String) {
    EXCELLENT("Excellent", "Your measurement practices are outstanding"),
    GOOD("Good", "Your measurement practices are solid"),
    FAIR("Fair", "Some improvements could help accuracy"),
    NEEDS_IMPROVEMENT("Needs Improvement", "Several aspects need attention")
}

@Singleton
class MeasurementQualityScorer @Inject constructor() {
    
    fun calculateQualityScore(readings: List<BloodPressureReading>): MeasurementQuality {
        if (readings.isEmpty()) {
            return MeasurementQuality(
                overallScore = 0,
                consistencyScore = 0,
                frequencyScore = 0,
                techniqueScore = 0,
                tips = listOf("Start adding readings to track your measurement quality"),
                grade = QualityGrade.NEEDS_IMPROVEMENT
            )
        }
        
        val consistencyScore = calculateConsistencyScore(readings)
        val frequencyScore = calculateFrequencyScore(readings)
        val techniqueScore = calculateTechniqueScore(readings)
        
        val overallScore = ((consistencyScore * 0.4) + (frequencyScore * 0.3) + (techniqueScore * 0.3)).toInt()
        
        val tips = generateTips(readings, consistencyScore, frequencyScore, techniqueScore)
        
        val grade = when {
            overallScore >= 80 -> QualityGrade.EXCELLENT
            overallScore >= 60 -> QualityGrade.GOOD
            overallScore >= 40 -> QualityGrade.FAIR
            else -> QualityGrade.NEEDS_IMPROVEMENT
        }
        
        return MeasurementQuality(
            overallScore = overallScore,
            consistencyScore = consistencyScore,
            frequencyScore = frequencyScore,
            techniqueScore = techniqueScore,
            tips = tips,
            grade = grade
        )
    }
    
    private fun calculateConsistencyScore(readings: List<BloodPressureReading>): Int {
        if (readings.size < 3) return 50
        
        val sortedReadings = readings.sortedBy { it.timestamp }
        var consistentPairs = 0
        var totalPairs = 0
        
        for (i in 0 until sortedReadings.size - 1) {
            val timeDiff = Duration.between(
                sortedReadings[i].timestamp,
                sortedReadings[i + 1].timestamp
            ).toMinutes()
            
            if (timeDiff <= 5) {
                totalPairs++
                val systolicDiff = kotlin.math.abs(
                    sortedReadings[i].systolic - sortedReadings[i + 1].systolic
                )
                val diastolicDiff = kotlin.math.abs(
                    sortedReadings[i].diastolic - sortedReadings[i + 1].diastolic
                )
                
                if (systolicDiff <= 10 && diastolicDiff <= 5) {
                    consistentPairs++
                }
            }
        }
        
        return if (totalPairs > 0) {
            ((consistentPairs.toDouble() / totalPairs) * 100).toInt().coerceIn(0, 100)
        } else {
            70
        }
    }
    
    private fun calculateFrequencyScore(readings: List<BloodPressureReading>): Int {
        if (readings.isEmpty()) return 0
        
        val sortedReadings = readings.sortedBy { it.timestamp }
        val firstReading = sortedReadings.first().timestamp
        val lastReading = sortedReadings.last().timestamp
        
        val daysBetween = Duration.between(firstReading, lastReading).toDays() + 1
        if (daysBetween <= 0) return 50
        
        val readingsPerDay = readings.size.toDouble() / daysBetween
        
        return when {
            readingsPerDay >= 2.0 -> 100
            readingsPerDay >= 1.5 -> 90
            readingsPerDay >= 1.0 -> 80
            readingsPerDay >= 0.7 -> 60
            readingsPerDay >= 0.5 -> 40
            else -> 20
        }
    }
    
    private fun calculateTechniqueScore(readings: List<BloodPressureReading>): Int {
        if (readings.isEmpty()) return 0
        
        var score = 60
        
        val sittingReadings = readings.filter { 
            it.bodyPosition == com.vitalflowapp.data.model.BodyPosition.SITTING 
        }
        val sittingPercentage = sittingReadings.size.toDouble() / readings.size
        if (sittingPercentage >= 0.8) score += 15
        else if (sittingPercentage >= 0.5) score += 10
        
        val consistentArm = readings.groupBy { it.armPosition }
            .maxByOrNull { it.value.size }
        val armConsistency = (consistentArm?.value?.size?.toDouble() ?: 0.0) / readings.size
        if (armConsistency >= 0.9) score += 15
        else if (armConsistency >= 0.7) score += 10
        
        val morningReadings = readings.filter { it.timestamp.hour in 5..10 }
        if (morningReadings.isNotEmpty()) score += 10
        
        return score.coerceIn(0, 100)
    }
    
    private fun generateTips(
        readings: List<BloodPressureReading>,
        consistencyScore: Int,
        frequencyScore: Int,
        techniqueScore: Int
    ): List<String> {
        val tips = mutableListOf<String>()
        
        if (frequencyScore < 60) {
            tips.add("Try to take readings at least twice daily for better tracking")
        }
        
        if (consistencyScore < 60) {
            tips.add("Take 2-3 readings within 5 minutes and average them for accuracy")
        }
        
        val sittingCount = readings.count { 
            it.bodyPosition == com.vitalflowapp.data.model.BodyPosition.SITTING 
        }
        if (sittingCount.toDouble() / readings.size.coerceAtLeast(1) < 0.8) {
            tips.add("Sit quietly for 5 minutes before measuring for best results")
        }
        
        val armCounts = readings.groupBy { it.armPosition }
        if (armCounts.size > 1) {
            val dominant = armCounts.maxByOrNull { it.value.size }
            tips.add("Try to use the same arm (${dominant?.key?.label}) consistently")
        }
        
        val morningReadings = readings.filter { it.timestamp.hour in 5..10 }
        if (morningReadings.isEmpty() && readings.size >= 7) {
            tips.add("Add morning readings to track your 'morning surge' pattern")
        }
        
        if (tips.isEmpty()) {
            tips.add("Great technique! Keep up your consistent measurement habits")
        }
        
        return tips.take(3)
    }
    
    fun validateReading(reading: BloodPressureReading): List<String> {
        val issues = mutableListOf<String>()
        
        if (reading.systolic < 70 || reading.systolic > 250) {
            issues.add("Systolic value (${reading.systolic}) seems unusual")
        }
        
        if (reading.diastolic < 40 || reading.diastolic > 150) {
            issues.add("Diastolic value (${reading.diastolic}) seems unusual")
        }
        
        if (reading.systolic <= reading.diastolic) {
            issues.add("Systolic should be higher than diastolic")
        }
        
        if (reading.pulse < 40 || reading.pulse > 200) {
            issues.add("Pulse value (${reading.pulse}) seems unusual")
        }
        
        val pulsePressure = reading.systolic - reading.diastolic
        if (pulsePressure < 20 || pulsePressure > 100) {
            issues.add("Pulse pressure seems unusual - consider remeasuring")
        }
        
        return issues
    }
}
