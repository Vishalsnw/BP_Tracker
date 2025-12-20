package com.vitalflowapp.ui.screens.glucose

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalflowapp.data.model.GlucoseEntry
import com.vitalflowapp.data.model.GlucoseType
import com.vitalflowapp.data.repository.GlucoseRepository
import com.vitalflowapp.utils.CsvExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class GlucoseUiState(
    val entries: List<GlucoseEntry> = emptyList(),
    val latestGlucose: GlucoseEntry? = null,
    val averageFasting: Double? = null,
    val averagePostMeal: Double? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class GlucoseViewModel @Inject constructor(
    private val repository: GlucoseRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GlucoseUiState())
    val uiState: StateFlow<GlucoseUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            repository.getAllGlucoseEntries().collect { entries ->
                val thirtyDaysAgo = LocalDateTime.now().minusDays(30)
                val avgFasting = repository.getAverageGlucoseByType(GlucoseType.FASTING, thirtyDaysAgo)
                val avgPostMeal = repository.getAverageGlucoseByType(GlucoseType.POST_MEAL, thirtyDaysAgo)
                
                _uiState.update {
                    it.copy(
                        entries = entries,
                        latestGlucose = entries.firstOrNull(),
                        averageFasting = avgFasting,
                        averagePostMeal = avgPostMeal,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun addGlucose(glucose: Double, type: GlucoseType, notes: String) {
        viewModelScope.launch {
            val entry = GlucoseEntry(
                glucoseMgDl = glucose,
                type = type,
                notes = notes,
                timestamp = LocalDateTime.now()
            )
            repository.insertGlucose(entry)
        }
    }
    
    fun deleteGlucose(entry: GlucoseEntry) {
        viewModelScope.launch {
            repository.deleteGlucose(entry)
        }
    }
    
    fun exportToCsv() {
        viewModelScope.launch {
            val entries = _uiState.value.entries
            if (entries.isNotEmpty()) {
                CsvExporter.exportGlucoseEntries(context, entries)
            }
        }
    }
}
