package com.vitalflowapp.data.repository

import com.vitalflowapp.data.database.GoalDao
import com.vitalflowapp.data.model.Achievement
import com.vitalflowapp.data.model.Goal
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao
) {
    fun getActiveGoals(): Flow<List<Goal>> = goalDao.getActiveGoals()
    
    fun getAllGoals(): Flow<List<Goal>> = goalDao.getAllGoals()
    
    suspend fun getGoalById(id: Long): Goal? = goalDao.getGoalById(id)
    
    suspend fun insertGoal(goal: Goal): Long = goalDao.insertGoal(goal)
    
    suspend fun updateGoal(goal: Goal) = goalDao.updateGoal(goal)
    
    suspend fun deleteGoal(goal: Goal) = goalDao.deleteGoal(goal)
    
    fun getAllAchievements(): Flow<List<Achievement>> = goalDao.getAllAchievements()
    
    fun getUnlockedAchievements(): Flow<List<Achievement>> = goalDao.getUnlockedAchievements()
    
    suspend fun insertAchievement(achievement: Achievement): Long = goalDao.insertAchievement(achievement)
    
    suspend fun updateAchievement(achievement: Achievement) = goalDao.updateAchievement(achievement)
}
