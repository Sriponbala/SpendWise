package com.example.spendwise.dao

import androidx.room.*
import com.example.spendwise.domain.Budget
import com.example.spendwise.domain.Category
import com.example.spendwise.enums.Period

@Dao
interface BudgetDao {

    @Insert
    fun insertBudget(budget: Budget)

    @Update
    fun updateBudget(budget: Budget)

    @Delete
    fun deleteBudget(budget: Budget)

    @Query("select * from Budget where userId like :userId and period like :period")
    fun getBudgetsOfThePeriod(userId: Int, period: String): List<Budget>

    @Query("select exists(select * from Budget where category like :category and period like :period and userId like :userId)")
    fun checkIfCategoryExists(userId: Int, category: String, period: String): Boolean
}