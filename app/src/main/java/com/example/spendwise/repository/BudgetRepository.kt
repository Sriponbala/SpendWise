package com.example.spendwise.repository

import com.example.spendwise.dao.BudgetDao
import com.example.spendwise.domain.Budget
import com.example.spendwise.enums.Period
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BudgetRepository(private val budgetDao: BudgetDao) {

    suspend fun insertBudget(budget: Budget) {
        withContext(Dispatchers.IO) {
            budgetDao.insertBudget(budget)
        }
    }

    suspend fun updateBudget(budget: Budget) {
        withContext(Dispatchers.IO) {
            budgetDao.updateBudget(budget)
        }
    }

    suspend fun deleteBudget(budget: Budget) {
        withContext(Dispatchers.IO) {
            budgetDao.deleteBudget(budget)
        }
    }

    suspend fun checkIfCategoryAlreadyExists(userId: Int, category: String, period: String): Boolean {
        return withContext(Dispatchers.IO) {
            budgetDao.checkIfCategoryExists(userId, category, period)
        }
    }

    suspend fun fetchBudgetsOfThePeriod(userId: Int, period: String): List<Budget> {
        return withContext(Dispatchers.IO) {
            budgetDao.getBudgetsOfThePeriod(userId, period)
        }
    }

}