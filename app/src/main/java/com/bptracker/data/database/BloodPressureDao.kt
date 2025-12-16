package com.bptracker.data.database

import androidx.room.*
import com.bptracker.data.model.BloodPressureReading
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface BloodPressureDao {
    
    @Query("SELECT * FROM blood_pressure_readings ORDER BY timestamp DESC")
    fun getAllReadings(): Flow<List<BloodPressureReading>>
    
    @Query("SELECT * FROM blood_pressure_readings ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentReadings(limit: Int): Flow<List<BloodPressureReading>>
    
    @Query("SELECT * FROM blood_pressure_readings WHERE timestamp >= :startDate AND timestamp <= :endDate ORDER BY timestamp DESC")
    fun getReadingsInRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<BloodPressureReading>>
    
    @Query("SELECT * FROM blood_pressure_readings WHERE timestamp >= :startDate AND timestamp <= :endDate ORDER BY timestamp ASC")
    suspend fun getReadingsInRangeSync(startDate: LocalDateTime, endDate: LocalDateTime): List<BloodPressureReading>
    
    @Query("SELECT * FROM blood_pressure_readings WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteReadings(): Flow<List<BloodPressureReading>>
    
    @Query("SELECT * FROM blood_pressure_readings WHERE id = :id")
    suspend fun getReadingById(id: Long): BloodPressureReading?
    
    @Query("SELECT AVG(systolic) FROM blood_pressure_readings WHERE timestamp >= :startDate")
    suspend fun getAverageSystolic(startDate: LocalDateTime): Double?
    
    @Query("SELECT AVG(diastolic) FROM blood_pressure_readings WHERE timestamp >= :startDate")
    suspend fun getAverageDiastolic(startDate: LocalDateTime): Double?
    
    @Query("SELECT AVG(pulse) FROM blood_pressure_readings WHERE timestamp >= :startDate")
    suspend fun getAveragePulse(startDate: LocalDateTime): Double?
    
    @Query("SELECT MAX(systolic) FROM blood_pressure_readings WHERE timestamp >= :startDate")
    suspend fun getMaxSystolic(startDate: LocalDateTime): Int?
    
    @Query("SELECT MAX(diastolic) FROM blood_pressure_readings WHERE timestamp >= :startDate")
    suspend fun getMaxDiastolic(startDate: LocalDateTime): Int?
    
    @Query("SELECT MIN(systolic) FROM blood_pressure_readings WHERE timestamp >= :startDate")
    suspend fun getMinSystolic(startDate: LocalDateTime): Int?
    
    @Query("SELECT MIN(diastolic) FROM blood_pressure_readings WHERE timestamp >= :startDate")
    suspend fun getMinDiastolic(startDate: LocalDateTime): Int?
    
    @Query("SELECT COUNT(*) FROM blood_pressure_readings WHERE timestamp >= :startDate")
    suspend fun getReadingCount(startDate: LocalDateTime): Int
    
    @Query("SELECT COUNT(*) FROM blood_pressure_readings")
    suspend fun getTotalReadingCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: BloodPressureReading): Long
    
    @Update
    suspend fun updateReading(reading: BloodPressureReading)
    
    @Delete
    suspend fun deleteReading(reading: BloodPressureReading)
    
    @Query("DELETE FROM blood_pressure_readings WHERE id = :id")
    suspend fun deleteReadingById(id: Long)
    
    @Query("DELETE FROM blood_pressure_readings")
    suspend fun deleteAllReadings()
}
