package com.vitalflowapp.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalflowapp.data.model.UserProfile
import com.vitalflowapp.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val profiles: List<UserProfile> = emptyList(),
    val activeProfileId: Long = 1,
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            repository.getAllProfiles().collect { profiles ->
                val activeProfile = profiles.find { it.isActive } ?: profiles.firstOrNull()
                _uiState.update { 
                    it.copy(
                        profiles = profiles, 
                        activeProfileId = activeProfile?.id ?: 1,
                        isLoading = false
                    ) 
                }
            }
        }
    }
    
    fun addProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.insertProfile(profile)
        }
    }
    
    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }
    
    fun deleteProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
        }
    }
    
    fun setActiveProfile(profileId: Long) {
        viewModelScope.launch {
            repository.setActiveProfile(profileId)
            _uiState.update { it.copy(activeProfileId = profileId) }
        }
    }
}
