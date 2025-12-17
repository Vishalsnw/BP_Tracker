package com.bptracker.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bptracker.data.model.InsightCard
import com.bptracker.data.model.TimeOfDayStats
import com.bptracker.data.model.WeeklySummary
import com.bptracker.data.repository.BloodPressureRepository
import com.bptracker.data.repository.InsightRepository
import com.bptracker.utils.InsightGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class InsightsUiState(
    val insights: List<InsightCard> = emptyList(),
    val weeklySummary: WeeklySummary? = null,
    val timeOfDayStats: List<TimeOfDayStats> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val insightRepository: InsightRepository,
    private val bpRepository: BloodPressureRepository,
    private val insightGenerator: InsightGenerator
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            val now = LocalDateTime.now()
            val weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay()
            val weekEnd = weekStart.plusDays(6).plusHours(23).plusMinutes(59)
            val lastWeekStart = weekStart.minusDays(7)
            val lastWeekEnd = weekStart.minusSeconds(1)
            
            val thisWeekReadings = bpRepository.getReadingsInRange(weekStart, weekEnd)
            val lastWeekReadings = bpRepository.getReadingsInRange(lastWeekStart, lastWeekEnd)
            
            val weeklySummary = if (thisWeekReadings.isNotEmpty()) {
                insightGenerator.generateWeeklySummary(
                    thisWeekReadings, 
                    lastWeekReadings,
                    weekStart,
                    weekEnd
                )
            } else null
            
            val timeOfDayStats = insightGenerator.analyzeTimeOfDay(thisWeekReadings)
            
            insightRepository.getActiveInsights().collect { savedInsights ->
                val generatedInsights = insightGenerator.generateInsights(thisWeekReadings, lastWeekReadings)
                
                for (insight in generatedInsights) {
                    if (savedInsights.none { it.type == insight.type && !it.isDismissed }) {
                        insightRepository.insertInsight(insight)
                    }
                }
                
                _uiState.update {
                    it.copy(
                        insights = savedInsights.filter { !it.isDismissed },
                        weeklySummary = weeklySummary,
                        timeOfDayStats = timeOfDayStats,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun refreshInsights() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            insightRepository.deleteOldInsights(7)
            
            loadData()
        }
    }
    
    fun dismissInsight(id: Long) {
        viewModelScope.launch {
            insightRepository.dismissInsight(id)
        }
    }
}
