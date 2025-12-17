package com.bptracker.di

import android.content.Context
import androidx.room.Room
import com.bptracker.data.database.BloodPressureDao
import com.bptracker.data.database.BloodPressureDatabase
import com.bptracker.data.database.MedicationDao
import com.bptracker.data.database.ProfileDao
import com.bptracker.data.database.ReminderDao
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
            .addMigrations(BloodPressureDatabase.MIGRATION_1_2)
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
}
