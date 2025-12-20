package com.vitalflowapp.utils

import com.vitalflowapp.data.model.BloodPressureReading
import com.vitalflowapp.data.model.Medication
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

data class MedicationEffectiveness(
    val medication: Medication,
    val avgSystolicBefore: Double,
    val avgDiastolicBefore: Double,
    val avgSystolicAfter: Double,
    val avgDiastolicAfter: Double,
    val systolicChange: Double,
    val diastolicChange: Double,
    val readingsBeforeCount: Int,
    val readingsAfterCount: Int,
    val isEffective: Boolean,
    val effectivenessPercentage: Double
) {
    val summary: String
        get() {
            return when {
                readingsAfterCount < 5 -> "Not enough data after starting medication"
                readingsBeforeCount < 5 -> "Not enough data before starting medication"
                isEffective -> "Blood pressure improved by ${String.format("%.1f", -systolicChange)}/${String.format("%.1f", -diastolicChange)} mmHg"
                systolicChange > 0 || diastolicChange > 0 -> "Blood pressure increased - consult your doctor"
                else -> "Blood pressure remained stable"
            }
        }
}

@Singleton
class MedicationEffectivenessAnalyzer @Inject constructor() {
    
    fun analyzeMedicationEffectiveness(
        medication: Medication,
        allReadings: List<BloodPressureReading>
    ): MedicationEffectiveness? {
        val medicationStartDate = Instant.ofEpochMilli(medication.startDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        
        val readingsBefore = allReadings.filter { it.timestamp.isBefore(medicationStartDate) }
        val readingsAfter = if (medication.endDate != null) {
            val medicationEndDate = Instant.ofEpochMilli(medication.endDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
            allReadings.filter { 
                it.timestamp.isAfter(medicationStartDate) && 
                it.timestamp.isBefore(medicationEndDate) 
            }
        } else {
            allReadings.filter { it.timestamp.isAfter(medicationStartDate) }
        }
        
        if (readingsBefore.isEmpty() && readingsAfter.isEmpty()) {
            return null
        }
        
        val avgSystolicBefore = if (readingsBefore.isNotEmpty()) 
            readingsBefore.map { it.systolic }.average() else 0.0
        val avgDiastolicBefore = if (readingsBefore.isNotEmpty()) 
            readingsBefore.map { it.diastolic }.average() else 0.0
        val avgSystolicAfter = if (readingsAfter.isNotEmpty()) 
            readingsAfter.map { it.systolic }.average() else 0.0
        val avgDiastolicAfter = if (readingsAfter.isNotEmpty()) 
            readingsAfter.map { it.diastolic }.average() else 0.0
        
        val systolicChange = avgSystolicAfter - avgSystolicBefore
        val diastolicChange = avgDiastolicAfter - avgDiastolicBefore
        
        val isEffective = systolicChange < -5 || diastolicChange < -3
        
        val effectivenessPercentage = if (avgSystolicBefore > 0) {
            ((avgSystolicBefore - avgSystolicAfter) / avgSystolicBefore * 100).coerceIn(-100.0, 100.0)
        } else 0.0
        
        return MedicationEffectiveness(
            medication = medication,
            avgSystolicBefore = avgSystolicBefore,
            avgDiastolicBefore = avgDiastolicBefore,
            avgSystolicAfter = avgSystolicAfter,
            avgDiastolicAfter = avgDiastolicAfter,
            systolicChange = systolicChange,
            diastolicChange = diastolicChange,
            readingsBeforeCount = readingsBefore.size,
            readingsAfterCount = readingsAfter.size,
            isEffective = isEffective,
            effectivenessPercentage = effectivenessPercentage
        )
    }
    
    fun analyzeAllMedications(
        medications: List<Medication>,
        allReadings: List<BloodPressureReading>
    ): List<MedicationEffectiveness> {
        return medications.mapNotNull { medication ->
            analyzeMedicationEffectiveness(medication, allReadings)
        }
    }
    
    fun getMedicationRecommendations(
        effectivenessResults: List<MedicationEffectiveness>
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        effectivenessResults.forEach { result ->
            when {
                result.readingsAfterCount < 5 -> {
                    recommendations.add("Continue taking ${result.medication.name} and add more readings for better analysis")
                }
                result.isEffective -> {
                    recommendations.add("${result.medication.name} appears to be working well - continue as prescribed")
                }
                result.systolicChange > 10 || result.diastolicChange > 5 -> {
                    recommendations.add("Discuss ${result.medication.name} with your doctor - BP has increased")
                }
                else -> {
                    recommendations.add("${result.medication.name} shows stable results - consult doctor for review")
                }
            }
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Add medications and readings to track effectiveness")
        }
        
        return recommendations
    }
}
