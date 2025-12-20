package com.bptracker.data.model

data class Statistics(
    val averageSystolic: Double = 0.0,
    val averageDiastolic: Double = 0.0,
    val averagePulse: Double = 0.0,
    val maxSystolic: Int = 0,
    val maxDiastolic: Int = 0,
    val minSystolic: Int = 0,
    val minDiastolic: Int = 0,
    val totalReadings: Int = 0,
    val lowCount: Int = 0,
    val idealCount: Int = 0,
    val preHighCount: Int = 0,
    val highCount: Int = 0
) {
    val lowPercentage: Float
        get() = if (totalReadings > 0) lowCount.toFloat() / totalReadings * 100 else 0f
    
    val idealPercentage: Float
        get() = if (totalReadings > 0) idealCount.toFloat() / totalReadings * 100 else 0f
    
    val preHighPercentage: Float
        get() = if (totalReadings > 0) preHighCount.toFloat() / totalReadings * 100 else 0f
    
    val highPercentage: Float
        get() = if (totalReadings > 0) highCount.toFloat() / totalReadings * 100 else 0f
}
