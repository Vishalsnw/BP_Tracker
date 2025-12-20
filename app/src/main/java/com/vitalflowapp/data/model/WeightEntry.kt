package com.vitalflowapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weightKg: Double,
    val heightCm: Double? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val notes: String = "",
    val userId: Long = 0
) {
    val bmi: Double?
        get() = if (heightCm != null && heightCm > 0) {
            val heightM = heightCm / 100.0
            weightKg / heightM.pow(2)
        } else null
    
    val bmiCategory: BMICategory?
        get() = bmi?.let { value ->
            when {
                value < 18.5 -> BMICategory.UNDERWEIGHT
                value < 25 -> BMICategory.NORMAL
                value < 30 -> BMICategory.OVERWEIGHT
                else -> BMICategory.OBESE
            }
        }
    
    val weightLbs: Double
        get() = weightKg * 2.20462
    
    val formattedDate: String
        get() = timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    
    val formattedWeight: String
        get() = "%.1f kg (%.1f lbs)".format(weightKg, weightLbs)
}

enum class BMICategory(val label: String, val description: String) {
    UNDERWEIGHT("Underweight", "BMI < 18.5"),
    NORMAL("Normal", "BMI 18.5-24.9"),
    OVERWEIGHT("Overweight", "BMI 25-29.9"),
    OBESE("Obese", "BMI 30+")
}
