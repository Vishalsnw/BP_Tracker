package com.vitalflowapp.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Parcelize
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val time: LocalTime,
    val daysOfWeek: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val isEnabled: Boolean = true,
    val label: String = "Measure Blood Pressure"
) : Parcelable {
    
    val formattedTime: String
        get() = time.format(DateTimeFormatter.ofPattern("hh:mm a"))
    
    val formattedDays: String
        get() = when {
            daysOfWeek.size == 7 -> "Every day"
            daysOfWeek.size == 5 && !daysOfWeek.contains(DayOfWeek.SATURDAY) && !daysOfWeek.contains(DayOfWeek.SUNDAY) -> "Weekdays"
            daysOfWeek.size == 2 && daysOfWeek.contains(DayOfWeek.SATURDAY) && daysOfWeek.contains(DayOfWeek.SUNDAY) -> "Weekends"
            else -> daysOfWeek.sortedBy { it.value }.joinToString(", ") { 
                it.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() }
            }
        }
}
