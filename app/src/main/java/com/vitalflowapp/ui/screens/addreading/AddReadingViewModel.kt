package com.vitalflowapp.ui.screens.addreading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalflowapp.data.model.*
import com.vitalflowapp.data.repository.BloodPressureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class AddReadingUiState(
    val readingId: Long? = null,
    val systolic: Int = 120,
    val diastolic: Int = 80,
    val pulse: Int = 72,
    val notes: String = "",
    val tag: ReadingTag = ReadingTag.NONE,
    val armPosition: ArmPosition = ArmPosition.LEFT,
    val bodyPosition: BodyPosition = BodyPosition.SITTING,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val isFavorite: Boolean = false,
    val mood: Int = 3,
    val stressLevel: Int = 1,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val validationError: ValidationError? = null
)

data class ValidationError(
    val systolicError: String? = null,
    val diastolicError: String? = null,
    val pulseError: String? = null,
    val relationError: String? = null
)

@HiltViewModel
class AddReadingViewModel @Inject constructor(
    private val repository: BloodPressureRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddReadingUiState())
    val uiState: StateFlow<AddReadingUiState> = _uiState.asStateFlow()
    
    companion object {
        const val MIN_SYSTOLIC = 70
        const val MAX_SYSTOLIC = 250
        const val MIN_DIASTOLIC = 40
        const val MAX_DIASTOLIC = 150
        const val MIN_PULSE = 30
        const val MAX_PULSE = 220
    }
    
    fun loadReading(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val reading = repository.getReadingById(id)
            if (reading != null) {
                _uiState.update { state ->
                    state.copy(
                        readingId = reading.id,
                        systolic = reading.systolic,
                        diastolic = reading.diastolic,
                        pulse = reading.pulse,
                        notes = reading.notes,
                        tag = reading.tag,
                        armPosition = reading.armPosition,
                        bodyPosition = reading.bodyPosition,
                        timestamp = reading.timestamp,
                        isFavorite = reading.isFavorite,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Reading not found") }
            }
        }
    }
    
    fun setInitialValues(systolic: Int, diastolic: Int, pulse: Int) {
        _uiState.update { state ->
            state.copy(
                systolic = if (systolic > 0) systolic else state.systolic,
                diastolic = if (diastolic > 0) diastolic else state.diastolic,
                pulse = if (pulse > 0) pulse else state.pulse,
                notes = "Imported from Bluetooth BP Monitor"
            )
        }
    }
    
    fun updateSystolic(value: Int) {
        _uiState.update { it.copy(systolic = value, validationError = null) }
    }
    
    fun updateDiastolic(value: Int) {
        _uiState.update { it.copy(diastolic = value, validationError = null) }
    }
    
    fun updatePulse(value: Int) {
        _uiState.update { it.copy(pulse = value, validationError = null) }
    }
    
    fun updateNotes(value: String) {
        _uiState.update { it.copy(notes = value) }
    }
    
    fun updateTag(value: ReadingTag) {
        _uiState.update { it.copy(tag = value) }
    }
    
    fun updateArmPosition(value: ArmPosition) {
        _uiState.update { it.copy(armPosition = value) }
    }
    
    fun updateBodyPosition(value: BodyPosition) {
        _uiState.update { it.copy(bodyPosition = value) }
    }
    
    fun updateMood(value: Int) {
        _uiState.update { it.copy(mood = value) }
    }
    
    fun updateStressLevel(value: Int) {
        _uiState.update { it.copy(stressLevel = value) }
    }
    
    private fun validateReadings(): ValidationError? {
        val state = _uiState.value
        var error = ValidationError()
        var hasError = false
        
        if (state.systolic < MIN_SYSTOLIC || state.systolic > MAX_SYSTOLIC) {
            error = error.copy(systolicError = "Systolic should be between $MIN_SYSTOLIC-$MAX_SYSTOLIC mmHg")
            hasError = true
        }
        
        if (state.diastolic < MIN_DIASTOLIC || state.diastolic > MAX_DIASTOLIC) {
            error = error.copy(diastolicError = "Diastolic should be between $MIN_DIASTOLIC-$MAX_DIASTOLIC mmHg")
            hasError = true
        }
        
        if (state.pulse < MIN_PULSE || state.pulse > MAX_PULSE) {
            error = error.copy(pulseError = "Pulse should be between $MIN_PULSE-$MAX_PULSE bpm")
            hasError = true
        }
        
        if (state.systolic <= state.diastolic) {
            error = error.copy(relationError = "Systolic must be higher than diastolic")
            hasError = true
        }
        
        if (state.systolic - state.diastolic < 10) {
            error = error.copy(relationError = "Pulse pressure (difference) should be at least 10 mmHg")
            hasError = true
        }
        
        return if (hasError) error else null
    }
    
    fun saveReading() {
        val validationError = validateReadings()
        if (validationError != null) {
            _uiState.update { it.copy(validationError = validationError) }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val state = _uiState.value
            val reading = BloodPressureReading(
                id = state.readingId ?: 0,
                systolic = state.systolic,
                diastolic = state.diastolic,
                pulse = state.pulse,
                notes = state.notes,
                tag = state.tag,
                armPosition = state.armPosition,
                bodyPosition = state.bodyPosition,
                timestamp = if (state.readingId != null) state.timestamp else LocalDateTime.now(),
                isFavorite = state.isFavorite,
                mood = state.mood,
                stressLevel = state.stressLevel
            )
            
            if (state.readingId != null && state.readingId > 0) {
                repository.updateReading(reading)
            } else {
                repository.insertReading(reading)
            }
            
            _uiState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(validationError = null, error = null) }
    }
}
