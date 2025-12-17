package com.bptracker.data.repository

import com.bptracker.data.database.ProfileDao
import com.bptracker.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: ProfileDao
) {
    fun getAllProfiles(): Flow<List<UserProfile>> = profileDao.getAllProfiles()
    
    fun getActiveProfile(): Flow<UserProfile?> = profileDao.getActiveProfile()
    
    suspend fun getProfileById(id: Long): UserProfile? = profileDao.getProfileById(id)
    
    suspend fun insertProfile(profile: UserProfile): Long = 
        profileDao.insertProfile(profile)
    
    suspend fun updateProfile(profile: UserProfile) = 
        profileDao.updateProfile(profile)
    
    suspend fun deleteProfile(profile: UserProfile) = 
        profileDao.deleteProfile(profile)
    
    suspend fun setActiveProfile(profileId: Long) {
        profileDao.deactivateAllProfiles()
        profileDao.activateProfile(profileId)
    }
    
    suspend fun ensureDefaultProfile() {
        val profiles = profileDao.getProfileCount()
        if (profiles == 0) {
            profileDao.insertProfile(
                UserProfile(
                    name = "Me",
                    isDefault = true,
                    isActive = true,
                    avatarColor = 0
                )
            )
        }
    }
}
