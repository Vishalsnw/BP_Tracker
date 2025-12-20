package com.vitalflowapp.data.database

import androidx.room.*
import com.vitalflowapp.data.model.InsightCard
import kotlinx.coroutines.flow.Flow

@Dao
interface InsightDao {
    
    @Query("SELECT * FROM insights WHERE isDismissed = 0 ORDER BY priority DESC, createdAt DESC")
    fun getActiveInsights(): Flow<List<InsightCard>>
    
    @Query("SELECT * FROM insights ORDER BY createdAt DESC")
    fun getAllInsights(): Flow<List<InsightCard>>
    
    @Query("SELECT * FROM insights WHERE isDismissed = 0 ORDER BY priority DESC, createdAt DESC LIMIT :limit")
    fun getTopInsights(limit: Int): Flow<List<InsightCard>>
    
    @Query("SELECT * FROM insights WHERE id = :id")
    suspend fun getInsightById(id: Long): InsightCard?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: InsightCard): Long
    
    @Update
    suspend fun updateInsight(insight: InsightCard)
    
    @Delete
    suspend fun deleteInsight(insight: InsightCard)
    
    @Query("UPDATE insights SET isDismissed = 1 WHERE id = :id")
    suspend fun dismissInsight(id: Long)
    
    @Query("DELETE FROM insights WHERE createdAt < :timestamp")
    suspend fun deleteOldInsights(timestamp: Long)
    
    @Query("DELETE FROM insights WHERE type = :type")
    suspend fun deleteInsightsByType(type: String)
}
