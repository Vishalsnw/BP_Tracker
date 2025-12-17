package com.bptracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bptracker.data.model.BloodPressureReading
import com.bptracker.data.model.Medication
import com.bptracker.data.model.MedicationLog
import com.bptracker.data.model.Reminder
import com.bptracker.data.model.UserProfile

@Database(
    entities = [
        BloodPressureReading::class, 
        Reminder::class,
        Medication::class,
        MedicationLog::class,
        UserProfile::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BloodPressureDatabase : RoomDatabase() {
    abstract fun bloodPressureDao(): BloodPressureDao
    abstract fun reminderDao(): ReminderDao
    abstract fun medicationDao(): MedicationDao
    abstract fun profileDao(): ProfileDao
    
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    ALTER TABLE blood_pressure_readings ADD COLUMN profileId INTEGER NOT NULL DEFAULT 1
                """)
                database.execSQL("""
                    ALTER TABLE blood_pressure_readings ADD COLUMN mood INTEGER NOT NULL DEFAULT 3
                """)
                database.execSQL("""
                    ALTER TABLE blood_pressure_readings ADD COLUMN stressLevel INTEGER NOT NULL DEFAULT 1
                """)
                database.execSQL("""
                    ALTER TABLE blood_pressure_readings ADD COLUMN sessionId TEXT
                """)
                database.execSQL("""
                    ALTER TABLE blood_pressure_readings ADD COLUMN isAveraged INTEGER NOT NULL DEFAULT 0
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_profiles (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        dateOfBirth TEXT,
                        gender TEXT NOT NULL DEFAULT 'PREFER_NOT_TO_SAY',
                        avatarColor INTEGER NOT NULL DEFAULT 0,
                        isDefault INTEGER NOT NULL DEFAULT 0,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        targetSystolicMin INTEGER NOT NULL DEFAULT 90,
                        targetSystolicMax INTEGER NOT NULL DEFAULT 120,
                        targetDiastolicMin INTEGER NOT NULL DEFAULT 60,
                        targetDiastolicMax INTEGER NOT NULL DEFAULT 80,
                        emergencyContactName TEXT,
                        emergencyContactPhone TEXT,
                        enableCrisisAlerts INTEGER NOT NULL DEFAULT 1
                    )
                """)
                
                database.execSQL("""
                    INSERT INTO user_profiles (id, name, isDefault, isActive) VALUES (1, 'Me', 1, 1)
                """)
            }
        }
    }
}
