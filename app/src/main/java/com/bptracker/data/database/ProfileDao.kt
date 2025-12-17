package com.bptracker.data.database

import androidx.room.*
import com.bptracker.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    
    @Query("SELECT * FROM user_profiles ORDER BY isDefault DESC, name ASC")
    fun getAllProfiles(): Flow<List<UserProfile>>
    
    @Query("SELECT * FROM user_profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfile(): Flow<UserProfile?>
    
    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): UserProfile?
    
    @Query("SELECT COUNT(*) FROM user_profiles")
    suspend fun getProfileCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile): Long
    
    @Update
    suspend fun updateProfile(profile: UserProfile)
    
    @Delete
    suspend fun deleteProfile(profile: UserProfile)
    
    @Query("UPDATE user_profiles SET isActive = 0")
    suspend fun deactivateAllProfiles()
    
    @Query("UPDATE user_profiles SET isActive = 1 WHERE id = :profileId")
    suspend fun activateProfile(profileId: Long)
}
