package com.bptracker.data.repository

import com.bptracker.data.database.BloodPressureDao
import com.bptracker.data.model.BloodPressureCategory
import com.bptracker.data.model.BloodPressureReading
import com.bptracker.data.model.Statistics
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BloodPressureRepository @Inject constructor(
    private val bloodPressureDao: BloodPressureDao
) {
    
    fun getAllReadings(): Flow<List<BloodPressureReading>> = bloodPressureDao.getAllReadings()
    
    fun getRecentReadings(limit: Int = 10): Flow<List<BloodPressureReading>> = 
        bloodPressureDao.getRecentReadings(limit)
    
    fun getReadingsInRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<BloodPressureReading>> =
        bloodPressureDao.getReadingsInRange(startDate, endDate)
    
    suspend fun getReadingsInRangeSync(startDate: LocalDateTime, endDate: LocalDateTime): List<BloodPressureReading> =
        bloodPressureDao.getReadingsInRangeSync(startDate, endDate)
    
    fun getFavoriteReadings(): Flow<List<BloodPressureReading>> = bloodPressureDao.getFavoriteReadings()
    
    suspend fun getReadingById(id: Long): BloodPressureReading? = bloodPressureDao.getReadingById(id)
    
    suspend fun insertReading(reading: BloodPressureReading): Long = bloodPressureDao.insertReading(reading)
    
    suspend fun updateReading(reading: BloodPressureReading) = bloodPressureDao.updateReading(reading)
    
    suspend fun deleteReading(reading: BloodPressureReading) = bloodPressureDao.deleteReading(reading)
    
    suspend fun deleteReadingById(id: Long) = bloodPressureDao.deleteReadingById(id)
    
    suspend fun getTotalReadingCount(): Int = bloodPressureDao.getTotalReadingCount()
    
    suspend fun getStatistics(startDate: LocalDateTime): Statistics {
        val readings = bloodPressureDao.getReadingsInRangeSync(startDate, LocalDateTime.now())
        
        if (readings.isEmpty()) return Statistics()
        
        val categoryCount = readings.groupBy { it.category }
        
        return Statistics(
            averageSystolic = bloodPressureDao.getAverageSystolic(startDate) ?: 0.0,
            averageDiastolic = bloodPressureDao.getAverageDiastolic(startDate) ?: 0.0,
            averagePulse = bloodPressureDao.getAveragePulse(startDate) ?: 0.0,
            maxSystolic = bloodPressureDao.getMaxSystolic(startDate) ?: 0,
            maxDiastolic = bloodPressureDao.getMaxDiastolic(startDate) ?: 0,
            minSystolic = bloodPressureDao.getMinSystolic(startDate) ?: 0,
            minDiastolic = bloodPressureDao.getMinDiastolic(startDate) ?: 0,
            totalReadings = readings.size,
            normalCount = categoryCount[BloodPressureCategory.NORMAL]?.size ?: 0,
            elevatedCount = categoryCount[BloodPressureCategory.ELEVATED]?.size ?: 0,
            highStage1Count = categoryCount[BloodPressureCategory.HIGH_STAGE_1]?.size ?: 0,
            highStage2Count = categoryCount[BloodPressureCategory.HIGH_STAGE_2]?.size ?: 0,
            crisisCount = categoryCount[BloodPressureCategory.HYPERTENSIVE_CRISIS]?.size ?: 0
        )
    }
}
