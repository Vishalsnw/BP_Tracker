package com.bptracker.di

import android.content.Context
import androidx.room.Room
import com.bptracker.data.database.AlertDao
import com.bptracker.data.database.BloodPressureDao
import com.bptracker.data.database.BloodPressureDatabase
import com.bptracker.data.database.GlucoseDao
import com.bptracker.data.database.GoalDao
import com.bptracker.data.database.InsightDao
import com.bptracker.data.database.MedicationDao
import com.bptracker.data.database.ProfileDao
import com.bptracker.data.database.ReminderDao
import com.bptracker.data.database.WeightDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BloodPressureDatabase {
        return Room.databaseBuilder(
            context,
            BloodPressureDatabase::class.java,
            "blood_pressure_database"
        )
            .addMigrations(
                BloodPressureDatabase.MIGRATION_1_2,
                BloodPressureDatabase.MIGRATION_2_3,
                BloodPressureDatabase.MIGRATION_3_4
            )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideBloodPressureDao(database: BloodPressureDatabase): BloodPressureDao {
        return database.bloodPressureDao()
    }
    
    @Provides
    @Singleton
    fun provideReminderDao(database: BloodPressureDatabase): ReminderDao {
        return database.reminderDao()
    }
    
    @Provides
    @Singleton
    fun provideMedicationDao(database: BloodPressureDatabase): MedicationDao {
        return database.medicationDao()
    }
    
    @Provides
    @Singleton
    fun provideProfileDao(database: BloodPressureDatabase): ProfileDao {
        return database.profileDao()
    }
    
    @Provides
    @Singleton
    fun provideGoalDao(database: BloodPressureDatabase): GoalDao {
        return database.goalDao()
    }
    
    @Provides
    @Singleton
    fun provideWeightDao(database: BloodPressureDatabase): WeightDao {
        return database.weightDao()
    }
    
    @Provides
    @Singleton
    fun provideGlucoseDao(database: BloodPressureDatabase): GlucoseDao {
        return database.glucoseDao()
    }
    
    @Provides
    @Singleton
    fun provideAlertDao(database: BloodPressureDatabase): AlertDao {
        return database.alertDao()
    }
    
    @Provides
    @Singleton
    fun provideInsightDao(database: BloodPressureDatabase): InsightDao {
        return database.insightDao()
    }
}
