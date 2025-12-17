package com.bptracker.data.database

import androidx.room.*
import com.bptracker.data.model.GlucoseEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface GlucoseDao {
    
    @Query("SELECT * FROM glucose_entries ORDER BY timestamp DESC")
    fun getAllGlucoseEntries(): Flow<List<GlucoseEntry>>
    
    @Query("SELECT * FROM glucose_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentGlucoseEntries(limit: Int): Flow<List<GlucoseEntry>>
    
    @Query("SELECT * FROM glucose_entries WHERE timestamp >= :startDate ORDER BY timestamp ASC")
    suspend fun getGlucoseEntriesAfter(startDate: String): List<GlucoseEntry>
    
    @Query("SELECT * FROM glucose_entries ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestGlucose(): GlucoseEntry?
    
    @Query("SELECT * FROM glucose_entries WHERE id = :id")
    suspend fun getGlucoseById(id: Long): GlucoseEntry?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGlucose(glucose: GlucoseEntry): Long
    
    @Update
    suspend fun updateGlucose(glucose: GlucoseEntry)
    
    @Delete
    suspend fun deleteGlucose(glucose: GlucoseEntry)
    
    @Query("SELECT AVG(glucoseMgDl) FROM glucose_entries WHERE timestamp >= :startDate")
    suspend fun getAverageGlucose(startDate: String): Double?
    
    @Query("SELECT AVG(glucoseMgDl) FROM glucose_entries WHERE type = :type AND timestamp >= :startDate")
    suspend fun getAverageGlucoseByType(type: String, startDate: String): Double?
    
    @Query("SELECT COUNT(*) FROM glucose_entries")
    suspend fun getTotalGlucoseEntries(): Int
}
