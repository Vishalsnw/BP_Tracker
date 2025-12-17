package com.bptracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bptracker.data.model.Achievement
import com.bptracker.data.model.AlertSettings
import com.bptracker.data.model.BloodPressureReading
import com.bptracker.data.model.GlucoseEntry
import com.bptracker.data.model.Goal
import com.bptracker.data.model.InsightCard
import com.bptracker.data.model.Medication
import com.bptracker.data.model.MedicationLog
import com.bptracker.data.model.Reminder
import com.bptracker.data.model.UserProfile
import com.bptracker.data.model.WeightEntry

@Database(
    entities = [
        BloodPressureReading::class,
        Reminder::class,
        Medication::class,
        MedicationLog::class,
        UserProfile::class,
        Goal::class,
        Achievement::class,
        WeightEntry::class,
        GlucoseEntry::class,
        AlertSettings::class,
        InsightCard::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BloodPressureDatabase : RoomDatabase() {
    abstract fun bloodPressureDao(): BloodPressureDao
    abstract fun reminderDao(): ReminderDao
    abstract fun medicationDao(): MedicationDao
    abstract fun profileDao(): ProfileDao
    abstract fun goalDao(): GoalDao
    abstract fun weightDao(): WeightDao
    abstract fun glucoseDao(): GlucoseDao
    abstract fun alertDao(): AlertDao
    abstract fun insightDao(): InsightDao
    
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
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS goals (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL,
                        targetSystolicMax INTEGER NOT NULL DEFAULT 120,
                        targetDiastolicMax INTEGER NOT NULL DEFAULT 80,
                        targetWeight REAL,
                        dailyReadingTarget INTEGER NOT NULL DEFAULT 2,
                        startDate TEXT NOT NULL,
                        endDate TEXT,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        userId INTEGER NOT NULL DEFAULT 0
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS achievements (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL,
                        unlockedAt INTEGER,
                        progress INTEGER NOT NULL DEFAULT 0,
                        userId INTEGER NOT NULL DEFAULT 0
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS weight_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        weightKg REAL NOT NULL,
                        heightCm REAL,
                        timestamp TEXT NOT NULL,
                        notes TEXT NOT NULL DEFAULT '',
                        userId INTEGER NOT NULL DEFAULT 0
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS glucose_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        glucoseMgDl REAL NOT NULL,
                        type TEXT NOT NULL DEFAULT 'RANDOM',
                        timestamp TEXT NOT NULL,
                        notes TEXT NOT NULL DEFAULT '',
                        userId INTEGER NOT NULL DEFAULT 0
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS alert_settings (
                        id INTEGER PRIMARY KEY NOT NULL DEFAULT 1,
                        systolicThreshold INTEGER NOT NULL DEFAULT 140,
                        diastolicThreshold INTEGER NOT NULL DEFAULT 90,
                        crisisAlertEnabled INTEGER NOT NULL DEFAULT 1,
                        thresholdAlertEnabled INTEGER NOT NULL DEFAULT 1,
                        weeklySummaryEnabled INTEGER NOT NULL DEFAULT 1,
                        weeklySummaryDay INTEGER NOT NULL DEFAULT 1,
                        weeklySummaryHour INTEGER NOT NULL DEFAULT 9,
                        missedReminderAlertEnabled INTEGER NOT NULL DEFAULT 0,
                        goalAchievedAlertEnabled INTEGER NOT NULL DEFAULT 1
                    )
                """)
                
                database.execSQL("""
                    INSERT OR IGNORE INTO alert_settings (id) VALUES (1)
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS insights (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        message TEXT NOT NULL,
                        priority INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        expiresAt INTEGER,
                        isDismissed INTEGER NOT NULL DEFAULT 0,
                        actionType TEXT,
                        actionData TEXT
                    )
                """)
            }
        }
        
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    ALTER TABLE medications ADD COLUMN startDate INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                """)
                database.execSQL("""
                    ALTER TABLE medications ADD COLUMN endDate INTEGER
                """)
                database.execSQL("""
                    ALTER TABLE medications ADD COLUMN sideEffects TEXT NOT NULL DEFAULT ''
                """)
            }
        }
    }
}
