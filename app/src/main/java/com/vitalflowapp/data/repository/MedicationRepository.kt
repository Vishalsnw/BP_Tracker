package com.vitalflowapp.data.repository

import com.vitalflowapp.data.database.MedicationDao
import com.vitalflowapp.data.model.Medication
import com.vitalflowapp.data.model.MedicationLog
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepository @Inject constructor(
    private val medicationDao: MedicationDao
) {
    fun getAllMedications(): Flow<List<Medication>> = medicationDao.getAllMedications()
    
    fun getActiveMedications(): Flow<List<Medication>> = medicationDao.getActiveMedications()
    
    suspend fun getMedicationById(id: Long): Medication? = medicationDao.getMedicationById(id)
    
    suspend fun insertMedication(medication: Medication): Long = 
        medicationDao.insertMedication(medication)
    
    suspend fun updateMedication(medication: Medication) = 
        medicationDao.updateMedication(medication)
    
    suspend fun deleteMedication(medication: Medication) = 
        medicationDao.deleteMedication(medication)
    
    suspend fun insertMedicationLog(log: MedicationLog): Long = 
        medicationDao.insertMedicationLog(log)
    
    fun getMedicationLogs(medicationId: Long): Flow<List<MedicationLog>> = 
        medicationDao.getMedicationLogs(medicationId)
}
