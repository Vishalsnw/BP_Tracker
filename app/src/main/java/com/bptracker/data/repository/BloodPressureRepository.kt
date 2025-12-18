package com.bptracker.data.repository

import com.bptracker.data.database.BloodPressureDao
import com.bptracker.data.model.BloodPressureCategory
import com.bptracker.data.model.BloodPressureReading
import com.bptracker.data.model.Statistics
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BloodPressureRepository @Inject constructor(
    private val bloodPressureDao: BloodPressureDao
) {
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    private fun LocalDateTime.toDbString(): String = this.format(dateTimeFormatter)
    
    fun getAllReadings(): Flow<List<BloodPressureReading>> = bloodPressureDao.getAllReadings()
    
    fun getRecentReadings(limit: Int = 10): Flow<List<BloodPressureReading>> = 
        bloodPressureDao.getRecentReadings(limit)
    
    fun getReadingsInRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<BloodPressureReading>> =
        bloodPressureDao.getReadingsInRange(startDate.toDbString(), endDate.toDbString())
    
    suspend fun getReadingsInRangeSync(startDate: LocalDateTime, endDate: LocalDateTime): List<BloodPressureReading> =
        bloodPressureDao.getReadingsInRangeSync(startDate.toDbString(), endDate.toDbString())
    
    fun getFavoriteReadings(): Flow<List<BloodPressureReading>> = bloodPressureDao.getFavoriteReadings()
    
    suspend fun getReadingById(id: Long): BloodPressureReading? = bloodPressureDao.getReadingById(id)
    
    suspend fun insertReading(reading: BloodPressureReading): Long = bloodPressureDao.insertReading(reading)
    
    suspend fun updateReading(reading: BloodPressureReading) = bloodPressureDao.updateReading(reading)
    
    suspend fun deleteReading(reading: BloodPressureReading) = bloodPressureDao.deleteReading(reading)
    
    suspend fun deleteReadingById(id: Long) = bloodPressureDao.deleteReadingById(id)
    
    suspend fun getTotalReadingCount(): Int = bloodPressureDao.getTotalReadingCount()
    
    suspend fun getTodayReadings(): List<BloodPressureReading> {
        val startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay()
        val endOfDay = startOfDay.plusDays(1)
        return bloodPressureDao.getReadingsInRangeSync(startOfDay.toDbString(), endOfDay.toDbString())
    }
    
    suspend fun getStatistics(startDate: LocalDateTime): Statistics {
        val startDateStr = startDate.toDbString()
        val endDateStr = LocalDateTime.now().toDbString()
        val readings = bloodPressureDao.getReadingsInRangeSync(startDateStr, endDateStr)
        
        if (readings.isEmpty()) return Statistics()
        
        val categoryCount = readings.groupBy { it.category }
        
        return Statistics(
            averageSystolic = bloodPressureDao.getAverageSystolic(startDateStr) ?: 0.0,
            averageDiastolic = bloodPressureDao.getAverageDiastolic(startDateStr) ?: 0.0,
            averagePulse = bloodPressureDao.getAveragePulse(startDateStr) ?: 0.0,
            maxSystolic = bloodPressureDao.getMaxSystolic(startDateStr) ?: 0,
            maxDiastolic = bloodPressureDao.getMaxDiastolic(startDateStr) ?: 0,
            minSystolic = bloodPressureDao.getMinSystolic(startDateStr) ?: 0,
            minDiastolic = bloodPressureDao.getMinDiastolic(startDateStr) ?: 0,
            totalReadings = readings.size,
            normalCount = categoryCount[BloodPressureCategory.NORMAL]?.size ?: 0,
            elevatedCount = categoryCount[BloodPressureCategory.ELEVATED]?.size ?: 0,
            highStage1Count = categoryCount[BloodPressureCategory.HIGH_STAGE_1]?.size ?: 0,
            highStage2Count = categoryCount[BloodPressureCategory.HIGH_STAGE_2]?.size ?: 0,
            crisisCount = categoryCount[BloodPressureCategory.HYPERTENSIVE_CRISIS]?.size ?: 0
        )
    }
}
