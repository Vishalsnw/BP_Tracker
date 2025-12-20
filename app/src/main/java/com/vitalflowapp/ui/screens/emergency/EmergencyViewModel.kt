package com.vitalflowapp.ui.screens.emergency

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalflowapp.utils.CrisisResponseManager
import com.vitalflowapp.utils.CrisisSettings
import com.vitalflowapp.utils.EmergencyContact
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmergencyUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EmergencyViewModel @Inject constructor(
    private val crisisResponseManager: CrisisResponseManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()
    
    val emergencyContacts: StateFlow<List<EmergencyContact>> = 
        crisisResponseManager.getEmergencyContacts()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val crisisSettings: StateFlow<CrisisSettings> = 
        crisisResponseManager.getCrisisSettings()
            .stateIn(viewModelScope, SharingStarted.Lazily, CrisisSettings())
    
    fun addContact(name: String, phone: String, isPrimary: Boolean) {
        viewModelScope.launch {
            val currentContacts = emergencyContacts.value.toMutableList()
            
            if (isPrimary) {
                currentContacts.replaceAll { it.copy(isPrimary = false) }
            }
            
            currentContacts.add(EmergencyContact(name, phone, isPrimary))
            crisisResponseManager.saveEmergencyContacts(currentContacts)
        }
    }
    
    fun updateContact(
        oldContact: EmergencyContact,
        newName: String,
        newPhone: String,
        isPrimary: Boolean
    ) {
        viewModelScope.launch {
            val currentContacts = emergencyContacts.value.toMutableList()
            val index = currentContacts.indexOf(oldContact)
            
            if (index >= 0) {
                if (isPrimary) {
                    currentContacts.replaceAll { it.copy(isPrimary = false) }
                }
                currentContacts[index] = EmergencyContact(newName, newPhone, isPrimary)
                crisisResponseManager.saveEmergencyContacts(currentContacts)
            }
        }
    }
    
    fun removeContact(contact: EmergencyContact) {
        viewModelScope.launch {
            val currentContacts = emergencyContacts.value.toMutableList()
            currentContacts.remove(contact)
            crisisResponseManager.saveEmergencyContacts(currentContacts)
        }
    }
    
    fun setPrimaryContact(contact: EmergencyContact) {
        viewModelScope.launch {
            val currentContacts = emergencyContacts.value.map { 
                it.copy(isPrimary = it == contact) 
            }
            crisisResponseManager.saveEmergencyContacts(currentContacts)
        }
    }
    
    fun toggleCrisisAlerts(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = crisisSettings.value
            crisisResponseManager.saveCrisisSettings(
                currentSettings.copy(isEnabled = enabled)
            )
        }
    }
    
    fun toggleSmsAlerts(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = crisisSettings.value
            crisisResponseManager.saveCrisisSettings(
                currentSettings.copy(sendSmsAlert = enabled)
            )
        }
    }
    
    fun toggleAutoCall911(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = crisisSettings.value
            crisisResponseManager.saveCrisisSettings(
                currentSettings.copy(autoCall911 = enabled)
            )
        }
    }
    
    fun testEmergencyCall() {
        val intent = crisisResponseManager.createEmergencyDialIntent()
        context.startActivity(intent)
    }
    
    fun hasCallPermission(): Boolean = crisisResponseManager.hasCallPermission()
    fun hasSmsPermission(): Boolean = crisisResponseManager.hasSmsPermission()
    fun getRequiredPermissions(): Array<String> = crisisResponseManager.getRequiredPermissions()
}
