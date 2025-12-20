package com.vitalflowapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mood: MoodLevel,
    val stressLevel: StressLevel,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val notes: String = "",
    val userId: Long = 0
) {
    val formattedDate: String
        get() = timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    
    val formattedTime: String
        get() = timestamp.format(DateTimeFormatter.ofPattern("hh:mm a"))
}

enum class MoodLevel(val label: String, val emoji: String) {
    VERY_BAD("Very Bad", "1"),
    BAD("Bad", "2"),
    NEUTRAL("Neutral", "3"),
    GOOD("Good", "4"),
    VERY_GOOD("Very Good", "5")
}
