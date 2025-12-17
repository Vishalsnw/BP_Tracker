package com.bptracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "glucose_entries")
data class GlucoseEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val glucoseMgDl: Double,
    val type: GlucoseType = GlucoseType.RANDOM,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val notes: String = "",
    val userId: Long = 0
) {
    val glucoseMmolL: Double
        get() = glucoseMgDl / 18.0
    
    val formattedDate: String
        get() = timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    
    val formattedTime: String
        get() = timestamp.format(DateTimeFormatter.ofPattern("hh:mm a"))
    
    val formattedGlucose: String
        get() = "%.0f mg/dL (%.1f mmol/L)".format(glucoseMgDl, glucoseMmolL)
    
    val category: GlucoseCategory
        get() = when (type) {
            GlucoseType.FASTING -> when {
                glucoseMgDl < 70 -> GlucoseCategory.LOW
                glucoseMgDl < 100 -> GlucoseCategory.NORMAL
                glucoseMgDl < 126 -> GlucoseCategory.PREDIABETIC
                else -> GlucoseCategory.DIABETIC
            }
            GlucoseType.POST_MEAL -> when {
                glucoseMgDl < 70 -> GlucoseCategory.LOW
                glucoseMgDl < 140 -> GlucoseCategory.NORMAL
                glucoseMgDl < 200 -> GlucoseCategory.PREDIABETIC
                else -> GlucoseCategory.DIABETIC
            }
            GlucoseType.RANDOM -> when {
                glucoseMgDl < 70 -> GlucoseCategory.LOW
                glucoseMgDl < 140 -> GlucoseCategory.NORMAL
                glucoseMgDl < 200 -> GlucoseCategory.ELEVATED
                else -> GlucoseCategory.HIGH
            }
        }
}

enum class GlucoseType(val label: String) {
    FASTING("Fasting"),
    POST_MEAL("Post-Meal"),
    RANDOM("Random")
}

enum class GlucoseCategory(val label: String, val description: String) {
    LOW("Low", "Blood glucose is below normal range"),
    NORMAL("Normal", "Blood glucose is in healthy range"),
    ELEVATED("Elevated", "Blood glucose is slightly elevated"),
    PREDIABETIC("Prediabetic", "Blood glucose indicates prediabetes risk"),
    DIABETIC("Diabetic", "Blood glucose indicates diabetes"),
    HIGH("High", "Blood glucose is high")
}
