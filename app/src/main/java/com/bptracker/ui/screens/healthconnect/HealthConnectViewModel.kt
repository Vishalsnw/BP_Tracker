package com.bptracker.ui.screens.healthconnect

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bptracker.data.repository.BloodPressureRepository
import com.bptracker.utils.HealthConnectAvailability
import com.bptracker.utils.HealthConnectManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.healthConnectDataStore by preferencesDataStore(name = "health_connect_prefs")

data class HealthConnectUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null
)

@HiltViewModel
class HealthConnectViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val bloodPressureRepository: BloodPressureRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    companion object {
        private val SYNC_ENABLED_KEY = booleanPreferencesKey("health_connect_sync_enabled")
    }
    
    private val _uiState = MutableStateFlow(HealthConnectUiState())
    val uiState: StateFlow<HealthConnectUiState> = _uiState.asStateFlow()
    
    private val _availability = MutableStateFlow<HealthConnectAvailability>(HealthConnectAvailability.NotSupported)
    val availability: StateFlow<HealthConnectAvailability> = _availability.asStateFlow()
    
    private val _hasPermissions = MutableStateFlow(false)
    val hasPermissions: StateFlow<Boolean> = _hasPermissions.asStateFlow()
    
    val syncEnabled: StateFlow<Boolean> = context.healthConnectDataStore.data
        .map { prefs -> prefs[SYNC_ENABLED_KEY] ?: false }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)
    
    init {
        checkAvailability()
    }
    
    private fun checkAvailability() {
        _availability.value = healthConnectManager.checkAvailability()
        
        if (_availability.value == HealthConnectAvailability.Available) {
            viewModelScope.launch {
                _hasPermissions.value = healthConnectManager.hasAllPermissions()
            }
        }
    }
    
    fun getPermissionContract(): ActivityResultContract<Set<String>, Set<String>> {
        return healthConnectManager.createPermissionRequestContract()
    }
    
    fun getPermissions(): Set<String> = healthConnectManager.permissions
    
    fun onPermissionsResult(granted: Set<String>) {
        viewModelScope.launch {
            _hasPermissions.value = healthConnectManager.hasAllPermissions()
        }
    }
    
    fun openHealthConnectInstall() {
        val intent = healthConnectManager.getHealthConnectInstallIntent()
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
    
    fun toggleSync(enabled: Boolean) {
        viewModelScope.launch {
            context.healthConnectDataStore.edit { prefs ->
                prefs[SYNC_ENABLED_KEY] = enabled
            }
        }
    }
    
    fun syncAllReadings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncMessage = null) }
            
            val readings = bloodPressureRepository.getAllReadings().first()
            val result = healthConnectManager.syncAllReadings(readings)
            
            _uiState.update { 
                it.copy(
                    isSyncing = false,
                    syncMessage = if (result.success) {
                        "Successfully synced ${result.syncedReadings} readings"
                    } else {
                        result.errorMessage ?: "Sync failed"
                    }
                )
            }
        }
    }
}
