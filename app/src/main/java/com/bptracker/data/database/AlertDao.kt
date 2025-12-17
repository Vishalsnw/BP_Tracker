package com.bptracker.data.database

import androidx.room.*
import com.bptracker.data.model.AlertSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    
    @Query("SELECT * FROM alert_settings WHERE id = 1")
    fun getAlertSettings(): Flow<AlertSettings?>
    
    @Query("SELECT * FROM alert_settings WHERE id = 1")
    suspend fun getAlertSettingsSync(): AlertSettings?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlertSettings(settings: AlertSettings)
    
    @Update
    suspend fun updateAlertSettings(settings: AlertSettings)
}
