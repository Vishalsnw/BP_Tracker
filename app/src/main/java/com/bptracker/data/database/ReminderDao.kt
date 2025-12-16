package com.bptracker.data.database

import androidx.room.*
import com.bptracker.data.model.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    
    @Query("SELECT * FROM reminders ORDER BY time ASC")
    fun getAllReminders(): Flow<List<Reminder>>
    
    @Query("SELECT * FROM reminders WHERE isEnabled = 1 ORDER BY time ASC")
    fun getEnabledReminders(): Flow<List<Reminder>>
    
    @Query("SELECT * FROM reminders WHERE isEnabled = 1")
    suspend fun getEnabledRemindersSync(): List<Reminder>
    
    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): Reminder?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long
    
    @Update
    suspend fun updateReminder(reminder: Reminder)
    
    @Delete
    suspend fun deleteReminder(reminder: Reminder)
    
    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)
}
