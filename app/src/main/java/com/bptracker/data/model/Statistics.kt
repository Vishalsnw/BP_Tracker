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
    val normalCount: Int = 0,
    val elevatedCount: Int = 0,
    val highStage1Count: Int = 0,
    val highStage2Count: Int = 0,
    val crisisCount: Int = 0
) {
    val normalPercentage: Float
        get() = if (totalReadings > 0) normalCount.toFloat() / totalReadings * 100 else 0f
    
    val elevatedPercentage: Float
        get() = if (totalReadings > 0) elevatedCount.toFloat() / totalReadings * 100 else 0f
    
    val highStage1Percentage: Float
        get() = if (totalReadings > 0) highStage1Count.toFloat() / totalReadings * 100 else 0f
    
    val highStage2Percentage: Float
        get() = if (totalReadings > 0) highStage2Count.toFloat() / totalReadings * 100 else 0f
}
