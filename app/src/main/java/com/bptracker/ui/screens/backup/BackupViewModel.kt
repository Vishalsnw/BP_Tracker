package com.bptracker.ui.screens.backup

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bptracker.data.repository.BloodPressureRepository
import com.bptracker.data.repository.GlucoseRepository
import com.bptracker.data.repository.MedicationRepository
import com.bptracker.data.repository.ProfileRepository
import com.bptracker.data.repository.ReminderRepository
import com.bptracker.data.repository.WeightRepository
import com.bptracker.utils.BackupData
import com.bptracker.utils.BackupMetadata
import com.bptracker.utils.BackupState
import com.bptracker.utils.CloudBackupManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val cloudBackupManager: CloudBackupManager,
    private val bloodPressureRepository: BloodPressureRepository,
    private val medicationRepository: MedicationRepository,
    private val reminderRepository: ReminderRepository,
    private val profileRepository: ProfileRepository,
    private val weightRepository: WeightRepository,
    private val glucoseRepository: GlucoseRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()
    
    val backupState: StateFlow<BackupState> = cloudBackupManager.state
    val backups: StateFlow<List<BackupMetadata>> = cloudBackupManager.backups
    
    fun getSignInIntent(): Intent? {
        return cloudBackupManager.getSignInIntent()
    }
    
    fun handleSignInResult(account: GoogleSignInAccount?) {
        cloudBackupManager.handleSignInResult(account)
    }
    
    fun signOut() {
        viewModelScope.launch {
            cloudBackupManager.signOut()
        }
    }
    
    fun createBackup() {
        viewModelScope.launch {
            val readings = bloodPressureRepository.getAllReadings().first()
            val medications = medicationRepository.getAllMedications().first()
            val reminders = reminderRepository.getAllReminders().first()
            val profiles = profileRepository.getAllProfiles().first()
            val weightEntries = weightRepository.getAllEntries().first()
            val glucoseEntries = glucoseRepository.getAllEntries().first()
            
            val backupData = BackupData(
                readings = readings,
                medications = medications,
                reminders = reminders,
                profiles = profiles,
                weightEntries = weightEntries,
                glucoseEntries = glucoseEntries
            )
            
            cloudBackupManager.createBackup(backupData)
        }
    }
    
    fun restoreBackup(backupId: String) {
        viewModelScope.launch {
            val backupData = cloudBackupManager.restoreBackup(backupId)
            if (backupData != null) {
                // Restore data to local database
                backupData.readings.forEach { reading ->
                    bloodPressureRepository.insertReading(reading)
                }
                backupData.medications.forEach { medication ->
                    medicationRepository.insertMedication(medication)
                }
                backupData.reminders.forEach { reminder ->
                    reminderRepository.insertReminder(reminder)
                }
                backupData.profiles.forEach { profile ->
                    profileRepository.insertProfile(profile)
                }
                backupData.weightEntries.forEach { entry ->
                    weightRepository.insertEntry(entry)
                }
                backupData.glucoseEntries.forEach { entry ->
                    glucoseRepository.insertEntry(entry)
                }
            }
        }
    }
    
    fun deleteBackup(backupId: String) {
        viewModelScope.launch {
            cloudBackupManager.deleteBackup(backupId)
        }
    }
    
    fun refreshBackups() {
        viewModelScope.launch {
            cloudBackupManager.refreshBackupList()
        }
    }
}
