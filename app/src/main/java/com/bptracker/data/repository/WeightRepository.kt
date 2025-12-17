package com.bptracker.data.repository

import com.bptracker.data.database.WeightDao
import com.bptracker.data.model.WeightEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightRepository @Inject constructor(
    private val weightDao: WeightDao
) {
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    private fun LocalDateTime.toDbString(): String = this.format(dateTimeFormatter)
    
    fun getAllWeightEntries(): Flow<List<WeightEntry>> = weightDao.getAllWeightEntries()
    
    fun getRecentWeightEntries(limit: Int = 10): Flow<List<WeightEntry>> = 
        weightDao.getRecentWeightEntries(limit)
    
    suspend fun getWeightEntriesAfter(startDate: LocalDateTime): List<WeightEntry> =
        weightDao.getWeightEntriesAfter(startDate.toDbString())
    
    suspend fun getLatestWeight(): WeightEntry? = weightDao.getLatestWeight()
    
    suspend fun getWeightById(id: Long): WeightEntry? = weightDao.getWeightById(id)
    
    suspend fun insertWeight(weight: WeightEntry): Long = weightDao.insertWeight(weight)
    
    suspend fun updateWeight(weight: WeightEntry) = weightDao.updateWeight(weight)
    
    suspend fun deleteWeight(weight: WeightEntry) = weightDao.deleteWeight(weight)
    
    suspend fun getAverageWeight(startDate: LocalDateTime): Double? = 
        weightDao.getAverageWeight(startDate.toDbString())
    
    suspend fun getTotalWeightEntries(): Int = weightDao.getTotalWeightEntries()
}
