package com.bptracker.data.database

import androidx.room.*
import com.bptracker.data.model.BloodPressureReading
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodPressureDao {
    
    @Query("SELECT * FROM blood_pressure_readings ORDER BY timestamp DESC")
    fun getAllReadings(): Flow<List<BloodPressureReading>>
    
    @Query("SELECT * FROM blood_pressure_readings ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentReadings(limit: Int): Flow<List<BloodPressureReading>>
    
    @Query("SELECT * FROM blood_pressure_readings ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentReadingsSync(limit: Int): List<BloodPressureReading>
    
    @Query("SELECT * FROM blood_pressure_readings WHERE timestamp >= :startDate AND timestamp <= :endDate ORDER BY timestamp DESC")
    fun getReadingsInRange(startDate: String, endDate: String): Flow<List<BloodPressureReading>>
    
    @Query("SELECT * FROM blood_pressure_readings WHERE timestamp >= :startDate AND timestamp <= :endDate ORDER BY timestamp ASC")
    suspend fun getReadingsInRangeSync(startDate: String, endDate: String): List<BloodPressureReading>
    
    @Query("SELECT * FROM blood_pressure_readings WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteReadings(): Flow<List<BloodPressureReading>>
    
    @Query("SELECT * FROM blood_pressure_readings WHERE id = :id")
    suspend fun getReadingById(id: Long): BloodPressureReading?
    
    @Query("SELECT AVG(systolic) FROM blood_pressure_readings WHERE timestamp >= :startDate")
    suspend fun getAverageSystolic(startDate: String): Double?
    
    @Query("SELECT AVG(diastolic) FROM blood_pressure_readings WHERE timestamp >= :startDate")
    suspend fun getAverageDiastolic(startDate: String): Double?
    
    @Query("SELECT AVG(pulse) FROM blood_pressure_readings WHERE timestamp >= :startDate")
    suspend fun getAveragePulse(startDate: String): Double?
    
    @Query("SELECT MAX(systolic) FROM blood_pressure_readings WHERE timestamp >= :startDate")
    suspend fun getMaxSystolic(startDate: String): Int?
    
    @Query("SELECT MAX(diastolic) FROM blood_pressure_readings WHERE timestamp >= :startDate")
    suspend fun getMaxDiastolic(startDate: String): Int?
    
    @Query("SELECT MIN(systolic) FROM blood_pressure_readings WHERE timestamp >= :startDate")
    suspend fun getMinSystolic(startDate: String): Int?
    
    @Query("SELECT MIN(diastolic) FROM blood_pressure_readings WHERE timestamp >= :startDate")
    suspend fun getMinDiastolic(startDate: String): Int?
    
    @Query("SELECT COUNT(*) FROM blood_pressure_readings WHERE timestamp >= :startDate")
    suspend fun getReadingCount(startDate: String): Int
    
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
