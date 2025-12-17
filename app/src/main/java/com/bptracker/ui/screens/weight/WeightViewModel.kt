package com.bptracker.ui.screens.weight

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bptracker.data.model.WeightEntry
import com.bptracker.data.repository.WeightRepository
import com.bptracker.utils.CsvExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class WeightUiState(
    val entries: List<WeightEntry> = emptyList(),
    val latestWeight: WeightEntry? = null,
    val averageWeight: Double? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class WeightViewModel @Inject constructor(
    private val repository: WeightRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WeightUiState())
    val uiState: StateFlow<WeightUiState> = _uiState.asStateFlow()
    
    private var savedHeight: Double? = null
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            repository.getAllWeightEntries().collect { entries ->
                val thirtyDaysAgo = LocalDateTime.now().minusDays(30)
                val avg = repository.getAverageWeight(thirtyDaysAgo)
                
                savedHeight = entries.firstOrNull { it.heightCm != null }?.heightCm
                
                _uiState.update {
                    it.copy(
                        entries = entries,
                        latestWeight = entries.firstOrNull(),
                        averageWeight = avg,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun addWeight(weight: Double, height: Double?, notes: String) {
        viewModelScope.launch {
            val entry = WeightEntry(
                weightKg = weight,
                heightCm = height ?: savedHeight,
                notes = notes,
                timestamp = LocalDateTime.now()
            )
            repository.insertWeight(entry)
            
            if (height != null) {
                savedHeight = height
            }
        }
    }
    
    fun deleteWeight(entry: WeightEntry) {
        viewModelScope.launch {
            repository.deleteWeight(entry)
        }
    }
    
    fun exportToCsv() {
        viewModelScope.launch {
            val entries = _uiState.value.entries
            if (entries.isNotEmpty()) {
                CsvExporter.exportWeightEntries(context, entries)
            }
        }
    }
}
