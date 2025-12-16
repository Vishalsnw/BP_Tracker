package com.bptracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bptracker.data.model.BloodPressureReading
import com.bptracker.data.model.Reminder

@Database(
    entities = [BloodPressureReading::class, Reminder::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BloodPressureDatabase : RoomDatabase() {
    abstract fun bloodPressureDao(): BloodPressureDao
    abstract fun reminderDao(): ReminderDao
}
