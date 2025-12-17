package com.bptracker.ui.screens.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bptracker.data.model.Medication
import com.bptracker.data.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MedicationUiState(
    val medications: List<Medication> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val repository: MedicationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MedicationUiState())
    val uiState: StateFlow<MedicationUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            repository.getAllMedications().collect { medications ->
                _uiState.update { it.copy(medications = medications, isLoading = false) }
            }
        }
    }
    
    fun addMedication(medication: Medication) {
        viewModelScope.launch {
            repository.insertMedication(medication)
        }
    }
    
    fun updateMedication(medication: Medication) {
        viewModelScope.launch {
            repository.updateMedication(medication)
        }
    }
    
    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            repository.deleteMedication(medication)
        }
    }
    
    fun toggleMedicationActive(medication: Medication) {
        viewModelScope.launch {
            repository.updateMedication(medication.copy(isActive = !medication.isActive))
        }
    }
}
