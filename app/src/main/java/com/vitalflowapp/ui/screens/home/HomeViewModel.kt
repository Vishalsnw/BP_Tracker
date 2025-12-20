package com.vitalflowapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalflowapp.data.model.BloodPressureReading
import com.vitalflowapp.data.repository.BloodPressureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class HomeUiState(
    val recentReadings: List<BloodPressureReading> = emptyList(),
    val totalReadings: Int = 0,
    val avgSystolic: Double = 0.0,
    val avgDiastolic: Double = 0.0,
    val avgPulse: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: BloodPressureRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            repository.getRecentReadings(10).collect { readings ->
                val sevenDaysAgo = LocalDateTime.now().minusDays(7)
                val recentForStats = readings.filter { it.timestamp.isAfter(sevenDaysAgo) }
                
                _uiState.update { state ->
                    state.copy(
                        recentReadings = readings,
                        totalReadings = repository.getTotalReadingCount(),
                        avgSystolic = if (recentForStats.isNotEmpty()) 
                            recentForStats.map { it.systolic }.average() else 0.0,
                        avgDiastolic = if (recentForStats.isNotEmpty()) 
                            recentForStats.map { it.diastolic }.average() else 0.0,
                        avgPulse = if (recentForStats.isNotEmpty()) 
                            recentForStats.map { it.pulse }.average() else 0.0,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun deleteReading(reading: BloodPressureReading) {
        viewModelScope.launch {
            repository.deleteReading(reading)
        }
    }
    
    fun toggleFavorite(reading: BloodPressureReading) {
        viewModelScope.launch {
            repository.updateReading(reading.copy(isFavorite = !reading.isFavorite))
        }
    }
}
