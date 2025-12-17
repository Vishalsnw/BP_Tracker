package com.bptracker.data.database

import androidx.room.*
import com.bptracker.data.model.Medication
import com.bptracker.data.model.MedicationLog
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    
    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAllMedications(): Flow<List<Medication>>
    
    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveMedications(): Flow<List<Medication>>
    
    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: Long): Medication?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication): Long
    
    @Update
    suspend fun updateMedication(medication: Medication)
    
    @Delete
    suspend fun deleteMedication(medication: Medication)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationLog(log: MedicationLog): Long
    
    @Query("SELECT * FROM medication_logs WHERE medicationId = :medicationId ORDER BY takenAt DESC")
    fun getMedicationLogs(medicationId: Long): Flow<List<MedicationLog>>
    
    @Query("DELETE FROM medication_logs WHERE medicationId = :medicationId")
    suspend fun deleteMedicationLogs(medicationId: Long)
}
