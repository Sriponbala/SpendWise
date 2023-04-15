package com.example.spendwise.repository

import com.example.spendwise.dao.GoalDao
import com.example.spendwise.domain.Goal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoalRepository(private val goalDao: GoalDao) {

    suspend fun insertGoal(goal: Goal) {
        withContext(Dispatchers.IO) {
            goalDao.insertGoal(goal)
        }
    }

    suspend fun updateGoal(goal: Goal) {
        withContext(Dispatchers.IO) {
            goalDao.updateGoal(goal)
        }
    }

    suspend fun deleteGoal(goal: Goal) {
        withContext(Dispatchers.IO) {
            goalDao.deleteGoal(goal)
        }
    }

    suspend fun fetchGoals(userId: Int): List<Goal> {
        return withContext(Dispatchers.IO) {
            goalDao.getAllGoals(userId)
        }
    }

}