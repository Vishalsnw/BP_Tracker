package com.vitalflowapp.ui.screens.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalflowapp.data.model.BloodPressureReading
import com.vitalflowapp.data.repository.BloodPressureRepository
import com.vitalflowapp.utils.PdfExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class HistoryUiState(
    val readings: List<BloodPressureReading> = emptyList(),
    val isLoading: Boolean = true,
    val filter: HistoryFilter = HistoryFilter.ALL
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: BloodPressureRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    private val _filter = MutableStateFlow(HistoryFilter.ALL)
    
    init {
        loadReadings()
    }
    
    private fun loadReadings() {
        viewModelScope.launch {
            combine(
                repository.getAllReadings(),
                repository.getFavoriteReadings(),
                _filter
            ) { all, favorites, filter ->
                val now = LocalDateTime.now()
                when (filter) {
                    HistoryFilter.ALL -> all
                    HistoryFilter.FAVORITES -> favorites
                    HistoryFilter.LAST_7_DAYS -> all.filter { 
                        it.timestamp.isAfter(now.minusDays(7)) 
                    }
                    HistoryFilter.LAST_30_DAYS -> all.filter { 
                        it.timestamp.isAfter(now.minusDays(30)) 
                    }
                }
            }.collect { readings ->
                _uiState.update { it.copy(readings = readings, isLoading = false) }
            }
        }
    }
    
    fun setFilter(filter: HistoryFilter) {
        _filter.value = filter
        _uiState.update { it.copy(filter = filter) }
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
    
    fun exportToPdf() {
        viewModelScope.launch {
            val readings = _uiState.value.readings
            if (readings.isNotEmpty()) {
                PdfExporter.exportReadings(context, readings)
            }
        }
    }
}
