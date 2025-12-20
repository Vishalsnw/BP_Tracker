package com.vitalflowapp.data.repository

import com.vitalflowapp.data.database.AlertDao
import com.vitalflowapp.data.model.AlertSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepository @Inject constructor(
    private val alertDao: AlertDao
) {
    fun getAlertSettings(): Flow<AlertSettings?> = alertDao.getAlertSettings()
    
    suspend fun getAlertSettingsSync(): AlertSettings? = alertDao.getAlertSettingsSync()
    
    suspend fun insertAlertSettings(settings: AlertSettings) = alertDao.insertAlertSettings(settings)
    
    suspend fun updateAlertSettings(settings: AlertSettings) = alertDao.updateAlertSettings(settings)
    
    suspend fun getOrCreateAlertSettings(): AlertSettings {
        return alertDao.getAlertSettingsSync() ?: AlertSettings().also {
            alertDao.insertAlertSettings(it)
        }
    }
}
