package com.vitalflowapp.data.database

import androidx.room.*
import com.vitalflowapp.data.model.WeightEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    
    @Query("SELECT * FROM weight_entries ORDER BY timestamp DESC")
    fun getAllWeightEntries(): Flow<List<WeightEntry>>
    
    @Query("SELECT * FROM weight_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentWeightEntries(limit: Int): Flow<List<WeightEntry>>
    
    @Query("SELECT * FROM weight_entries WHERE timestamp >= :startDate ORDER BY timestamp ASC")
    suspend fun getWeightEntriesAfter(startDate: String): List<WeightEntry>
    
    @Query("SELECT * FROM weight_entries ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestWeight(): WeightEntry?
    
    @Query("SELECT * FROM weight_entries WHERE id = :id")
    suspend fun getWeightById(id: Long): WeightEntry?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(weight: WeightEntry): Long
    
    @Update
    suspend fun updateWeight(weight: WeightEntry)
    
    @Delete
    suspend fun deleteWeight(weight: WeightEntry)
    
    @Query("SELECT AVG(weightKg) FROM weight_entries WHERE timestamp >= :startDate")
    suspend fun getAverageWeight(startDate: String): Double?
    
    @Query("SELECT COUNT(*) FROM weight_entries")
    suspend fun getTotalWeightEntries(): Int
}
