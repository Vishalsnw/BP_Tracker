package com.vitalflowapp.ui.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalflowapp.data.model.Achievement
import com.vitalflowapp.data.model.Goal
import com.vitalflowapp.data.model.GoalType
import com.vitalflowapp.data.repository.BloodPressureRepository
import com.vitalflowapp.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class GoalsUiState(
    val activeGoals: List<Goal> = emptyList(),
    val goalProgress: Map<Long, Float> = emptyMap(),
    val unlockedAchievements: List<Achievement> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val bpRepository: BloodPressureRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            combine(
                goalRepository.getActiveGoals(),
                goalRepository.getUnlockedAchievements()
            ) { goals, achievements ->
                Pair(goals, achievements)
            }.collect { (goals, achievements) ->
                val progress = calculateProgress(goals)
                
                _uiState.update {
                    it.copy(
                        activeGoals = goals,
                        goalProgress = progress,
                        unlockedAchievements = achievements,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    private suspend fun calculateProgress(goals: List<Goal>): Map<Long, Float> {
        return goals.associate { goal ->
            val progress = when (goal.type) {
                GoalType.BLOOD_PRESSURE_TARGET -> {
                    val readings = bpRepository.getTodayReadings()
                    if (readings.isEmpty()) 0f
                    else {
                        val normalCount = readings.count { 
                            it.systolic <= goal.targetSystolicMax && 
                            it.diastolic <= goal.targetDiastolicMax 
                        }
                        normalCount.toFloat() / readings.size
                    }
                }
                GoalType.DAILY_READINGS -> {
                    val todayReadings = bpRepository.getTodayReadings()
                    (todayReadings.size.toFloat() / goal.dailyReadingTarget).coerceAtMost(1f)
                }
                GoalType.WEIGHT_TARGET -> {
                    0.5f
                }
                GoalType.CONSISTENCY_STREAK -> {
                    0.3f
                }
            }
            goal.id to progress
        }
    }
    
    fun addGoal(type: GoalType, systolicMax: Int, diastolicMax: Int, dailyTarget: Int) {
        viewModelScope.launch {
            val goal = Goal(
                type = type,
                targetSystolicMax = systolicMax,
                targetDiastolicMax = diastolicMax,
                dailyReadingTarget = dailyTarget,
                startDate = LocalDate.now(),
                isActive = true
            )
            goalRepository.insertGoal(goal)
        }
    }
    
    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            goalRepository.deleteGoal(goal)
        }
    }
}
