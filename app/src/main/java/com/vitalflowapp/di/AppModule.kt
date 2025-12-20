package com.vitalflowapp.di

import android.content.Context
import androidx.room.Room
import com.vitalflowapp.data.database.AlertDao
import com.vitalflowapp.data.database.BloodPressureDao
import com.vitalflowapp.data.database.BloodPressureDatabase
import com.vitalflowapp.data.database.GlucoseDao
import com.vitalflowapp.data.database.GoalDao
import com.vitalflowapp.data.database.InsightDao
import com.vitalflowapp.data.database.MedicationDao
import com.vitalflowapp.data.database.ProfileDao
import com.vitalflowapp.data.database.ReminderDao
import com.vitalflowapp.data.database.WeightDao
import com.vitalflowapp.utils.BluetoothBPMonitor
import com.vitalflowapp.utils.CrisisResponseManager
import com.vitalflowapp.utils.HealthConnectManager
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
    
    @Provides
    @Singleton
    fun provideHealthConnectManager(@ApplicationContext context: Context): HealthConnectManager {
        return HealthConnectManager(context)
    }
    
    @Provides
    @Singleton
    fun provideBluetoothBPMonitor(@ApplicationContext context: Context): BluetoothBPMonitor {
        return BluetoothBPMonitor(context)
    }
    
    @Provides
    @Singleton
    fun provideCrisisResponseManager(@ApplicationContext context: Context): CrisisResponseManager {
        return CrisisResponseManager(context)
    }
}
