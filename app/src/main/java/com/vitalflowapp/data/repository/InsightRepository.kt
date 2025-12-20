package com.vitalflowapp.data.repository

import com.vitalflowapp.data.database.InsightDao
import com.vitalflowapp.data.model.InsightCard
import com.vitalflowapp.data.model.InsightType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsightRepository @Inject constructor(
    private val insightDao: InsightDao
) {
    fun getActiveInsights(): Flow<List<InsightCard>> = insightDao.getActiveInsights()
    
    fun getAllInsights(): Flow<List<InsightCard>> = insightDao.getAllInsights()
    
    fun getTopInsights(limit: Int = 3): Flow<List<InsightCard>> = insightDao.getTopInsights(limit)
    
    suspend fun getInsightById(id: Long): InsightCard? = insightDao.getInsightById(id)
    
    suspend fun insertInsight(insight: InsightCard): Long = insightDao.insertInsight(insight)
    
    suspend fun updateInsight(insight: InsightCard) = insightDao.updateInsight(insight)
    
    suspend fun deleteInsight(insight: InsightCard) = insightDao.deleteInsight(insight)
    
    suspend fun dismissInsight(id: Long) = insightDao.dismissInsight(id)
    
    suspend fun deleteOldInsights(olderThanDays: Int = 30) {
        val timestamp = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
        insightDao.deleteOldInsights(timestamp)
    }
    
    suspend fun deleteInsightsByType(type: InsightType) = insightDao.deleteInsightsByType(type.name)
}
