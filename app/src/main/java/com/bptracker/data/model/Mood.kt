package com.bptracker.data.model

enum class Mood(val label: String, val emoji: String, val value: Int) {
    VERY_HAPPY("Very Happy", "😄", 5),
    HAPPY("Happy", "🙂", 4),
    NEUTRAL("Neutral", "😐", 3),
    STRESSED("Stressed", "😟", 2),
    VERY_STRESSED("Very Stressed", "😰", 1)
}

enum class StressLevel(val label: String, val value: Int) {
    NONE("None", 1),
    LOW("Low", 2),
    MODERATE("Moderate", 3),
    HIGH("High", 4),
    SEVERE("Severe", 5)
}
