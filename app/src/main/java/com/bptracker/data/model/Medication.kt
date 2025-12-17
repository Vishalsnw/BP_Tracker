package com.bptracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dosage: String,
    val frequency: MedicationFrequency = MedicationFrequency.DAILY,
    val times: List<LocalTime> = listOf(LocalTime.of(8, 0)),
    val notes: String = "",
    val isActive: Boolean = true,
    val userId: Long = 0,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val sideEffects: String = ""
) {
    val formattedTimes: String
        get() = times.joinToString(", ") { 
            it.format(DateTimeFormatter.ofPattern("hh:mm a")) 
        }
    
    val formattedStartDate: String
        get() = java.time.Instant.ofEpochMilli(startDate)
            .atZone(java.time.ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    
    val formattedEndDate: String?
        get() = endDate?.let {
            java.time.Instant.ofEpochMilli(it)
                .atZone(java.time.ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        }
}

enum class MedicationFrequency(val label: String) {
    DAILY("Daily"),
    TWICE_DAILY("Twice Daily"),
    THREE_TIMES_DAILY("Three Times Daily"),
    WEEKLY("Weekly"),
    AS_NEEDED("As Needed")
}

@Entity(tableName = "medication_logs")
data class MedicationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val takenAt: Long = System.currentTimeMillis(),
    val skipped: Boolean = false,
    val notes: String = ""
)
