package com.vitalflowapp.data.repository

import com.vitalflowapp.data.database.GlucoseDao
import com.vitalflowapp.data.model.GlucoseEntry
import com.vitalflowapp.data.model.GlucoseType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlucoseRepository @Inject constructor(
    private val glucoseDao: GlucoseDao
) {
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    private fun LocalDateTime.toDbString(): String = this.format(dateTimeFormatter)
    
    fun getAllGlucoseEntries(): Flow<List<GlucoseEntry>> = glucoseDao.getAllGlucoseEntries()
    
    fun getAllEntries(): Flow<List<GlucoseEntry>> = getAllGlucoseEntries()
    
    suspend fun insertEntry(entry: GlucoseEntry): Long = insertGlucose(entry)
    
    fun getRecentGlucoseEntries(limit: Int = 10): Flow<List<GlucoseEntry>> = 
        glucoseDao.getRecentGlucoseEntries(limit)
    
    suspend fun getGlucoseEntriesAfter(startDate: LocalDateTime): List<GlucoseEntry> =
        glucoseDao.getGlucoseEntriesAfter(startDate.toDbString())
    
    suspend fun getLatestGlucose(): GlucoseEntry? = glucoseDao.getLatestGlucose()
    
    suspend fun getGlucoseById(id: Long): GlucoseEntry? = glucoseDao.getGlucoseById(id)
    
    suspend fun insertGlucose(glucose: GlucoseEntry): Long = glucoseDao.insertGlucose(glucose)
    
    suspend fun updateGlucose(glucose: GlucoseEntry) = glucoseDao.updateGlucose(glucose)
    
    suspend fun deleteGlucose(glucose: GlucoseEntry) = glucoseDao.deleteGlucose(glucose)
    
    suspend fun getAverageGlucose(startDate: LocalDateTime): Double? = 
        glucoseDao.getAverageGlucose(startDate.toDbString())
    
    suspend fun getAverageGlucoseByType(type: GlucoseType, startDate: LocalDateTime): Double? = 
        glucoseDao.getAverageGlucoseByType(type.name, startDate.toDbString())
    
    suspend fun getTotalGlucoseEntries(): Int = glucoseDao.getTotalGlucoseEntries()
}
