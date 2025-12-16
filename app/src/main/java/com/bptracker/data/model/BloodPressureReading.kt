package com.bptracker.data.model

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
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val notes: String = "",
    val tag: ReadingTag = ReadingTag.NONE,
    val armPosition: ArmPosition = ArmPosition.LEFT,
    val bodyPosition: BodyPosition = BodyPosition.SITTING,
    val isFavorite: Boolean = false
) : Parcelable {
    
    val category: BloodPressureCategory
        get() = when {
            systolic < 120 && diastolic < 80 -> BloodPressureCategory.NORMAL
            systolic in 120..129 && diastolic < 80 -> BloodPressureCategory.ELEVATED
            systolic in 130..139 || diastolic in 80..89 -> BloodPressureCategory.HIGH_STAGE_1
            systolic >= 140 || diastolic >= 90 -> BloodPressureCategory.HIGH_STAGE_2
            systolic > 180 || diastolic > 120 -> BloodPressureCategory.HYPERTENSIVE_CRISIS
            else -> BloodPressureCategory.NORMAL
        }
    
    val formattedDate: String
        get() = timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    
    val formattedTime: String
        get() = timestamp.format(DateTimeFormatter.ofPattern("hh:mm a"))
    
    val formattedDateTime: String
        get() = timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a"))
}

enum class BloodPressureCategory(val label: String, val description: String) {
    NORMAL("Normal", "Your blood pressure is in the normal range."),
    ELEVATED("Elevated", "Your blood pressure is slightly elevated. Consider lifestyle changes."),
    HIGH_STAGE_1("High - Stage 1", "You have stage 1 hypertension. Consult your doctor."),
    HIGH_STAGE_2("High - Stage 2", "You have stage 2 hypertension. Seek medical advice."),
    HYPERTENSIVE_CRISIS("Crisis", "Seek emergency medical attention immediately!")
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
