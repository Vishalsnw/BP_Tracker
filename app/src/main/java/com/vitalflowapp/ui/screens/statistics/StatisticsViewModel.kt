package com.vitalflowapp.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalflowapp.data.model.Statistics
import com.vitalflowapp.data.repository.BloodPressureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class StatisticsUiState(
    val statistics: Statistics = Statistics(),
    val isLoading: Boolean = true
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: BloodPressureRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    
    init {
        loadStatistics(7)
    }
    
    fun loadStatistics(days: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val startDate = LocalDateTime.now().minusDays(days.toLong())
            val stats = repository.getStatistics(startDate)
            _uiState.update { it.copy(statistics = stats, isLoading = false) }
        }
    }
}
