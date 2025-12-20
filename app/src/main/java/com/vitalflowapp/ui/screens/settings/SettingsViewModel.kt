package com.vitalflowapp.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalflowapp.data.database.BloodPressureDao
import com.vitalflowapp.data.repository.BloodPressureRepository
import com.vitalflowapp.data.repository.MedicationRepository
import com.vitalflowapp.data.repository.ProfileRepository
import com.vitalflowapp.utils.CsvExporter
import com.vitalflowapp.utils.DoctorReportGenerator
import com.vitalflowapp.utils.PdfExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: BloodPressureRepository,
    private val medicationRepository: MedicationRepository,
    private val profileRepository: ProfileRepository,
    private val bloodPressureDao: BloodPressureDao,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    fun deleteAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            bloodPressureDao.deleteAllReadings()
            _uiState.update { it.copy(isLoading = false, message = "All data deleted") }
        }
    }
    
    fun exportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getAllReadings().first().let { readings ->
                if (readings.isNotEmpty()) {
                    PdfExporter.exportReadings(context, readings)
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    fun exportCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getAllReadings().first().let { readings ->
                if (readings.isNotEmpty()) {
                    CsvExporter.exportReadings(context, readings)
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    fun generateDoctorReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val readings = repository.getAllReadings().first()
            val medications = medicationRepository.getActiveMedications().first()
            val profile = profileRepository.getActiveProfile().first()
            
            if (readings.isNotEmpty()) {
                DoctorReportGenerator.generateDoctorVisitReport(
                    context = context,
                    profile = profile,
                    readings = readings,
                    medications = medications,
                    periodDays = 30
                )
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
