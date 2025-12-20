package com.vitalflowapp.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Parcelize
@Entity(tableName = "blood_pressure_readings")
data class BloodPressureReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long = 1,
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val notes: String = "",
    val tag: ReadingTag = ReadingTag.NONE,
    val armPosition: ArmPosition = ArmPosition.LEFT,
    val bodyPosition: BodyPosition = BodyPosition.SITTING,
    val isFavorite: Boolean = false,
    val mood: Int = 3,
    val stressLevel: Int = 1,
    val sessionId: String? = null,
    val isAveraged: Boolean = false
) : Parcelable {
    
    val category: BloodPressureCategory
        get() = when {
            systolic >= 180 || diastolic >= 120 -> BloodPressureCategory.CRISIS
            systolic >= 140 || diastolic >= 90 -> BloodPressureCategory.HIGH
            systolic in 120..139 || diastolic in 80..89 -> BloodPressureCategory.PRE_HIGH
            systolic < 120 && diastolic < 80 -> BloodPressureCategory.IDEAL
            systolic < 90 && diastolic < 60 -> BloodPressureCategory.LOW
            else -> BloodPressureCategory.IDEAL
        }
    
    val meanArterialPressure: Double
        get() = ((2.0 * diastolic) + systolic) / 3.0
    
    val pulsePressure: Int
        get() = systolic - diastolic
    
    val isCrisis: Boolean
        get() = category == BloodPressureCategory.CRISIS
    
    val formattedDate: String
        get() = timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    
    val formattedTime: String
        get() = timestamp.format(DateTimeFormatter.ofPattern("hh:mm a"))
    
    val formattedDateTime: String
        get() = timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a"))
    
    val formattedMAP: String
        get() = "%.0f".format(meanArterialPressure)
}

enum class BloodPressureCategory(val label: String, val description: String) {
    LOW("Low", "Your blood pressure is low. Ensure you're well hydrated and rested."),
    IDEAL("Ideal", "Your blood pressure is in the ideal range."),
    PRE_HIGH("Pre-high", "Your blood pressure is elevated. Monitor it regularly."),
    HIGH("High", "Your blood pressure is high. Consult your doctor."),
    CRISIS("Crisis", "Your blood pressure is critically high. Seek immediate medical attention!")
}

enum class ReadingTag(val label: String) {
    NONE("None"),
    MORNING("Morning"),
    AFTERNOON("Afternoon"),
    EVENING("Evening"),
    BEFORE_MEAL("Before Meal"),
    AFTER_MEAL("After Meal"),
    BEFORE_EXERCISE("Before Exercise"),
    AFTER_EXERCISE("After Exercise"),
    BEFORE_MEDICATION("Before Medication"),
    AFTER_MEDICATION("After Medication")
}

enum class ArmPosition(val label: String) {
    LEFT("Left Arm"),
    RIGHT("Right Arm")
}

enum class BodyPosition(val label: String) {
    SITTING("Sitting"),
    STANDING("Standing"),
    LYING_DOWN("Lying Down")
}
