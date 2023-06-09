package com.example.spendwise.dao

import androidx.room.*
import com.example.spendwise.domain.Goal

@Dao
interface GoalDao {

    @Insert
    fun insertGoal(goal: Goal)

    @Update
    fun updateGoal(goal: Goal)

    @Delete
    fun deleteGoal(goal: Goal)

    @Query("select * from Goal where userId = :userId")
    fun getAllGoals(userId: Int): List<Goal>

}