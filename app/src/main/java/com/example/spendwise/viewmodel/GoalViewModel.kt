package com.example.spendwise.viewmodel

import android.app.Application
import android.util.Log
import android.view.Menu
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.PrimaryKey
import com.example.spendwise.database.SpendWiseDatabase
import com.example.spendwise.domain.Budget
import com.example.spendwise.domain.Goal
import com.example.spendwise.enums.GoalStatus
import com.example.spendwise.repository.BudgetRepository
import com.example.spendwise.repository.GoalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoalViewModel(application: Application): AndroidViewModel(application) {

    var menu: Menu? = null

    private val repository = GoalRepository(SpendWiseDatabase.getInstance(application).goalDao)

    private val _goal = MutableLiveData<Goal?>()
    val goal: LiveData<Goal?>
        get() = _goal

    private val _goals = MutableLiveData<List<Goal>?>()
    val goals: LiveData<List<Goal>?>
        get() = _goals

    fun insertGoal(userId: Int, goalName: String, targetAmount: Float, savedAmount: Float = 0f, goalColor: Int = 0, goalIcon: Int = 0, desiredDate: String = "") {
        viewModelScope.launch {
            val job = launch {
                val goal = Goal(userId, goalName, targetAmount).apply {
                    this.savedAmount = savedAmount
                    this.goalColor = goalColor
                    this.goalIcon = goalIcon
                    this.desiredDate = desiredDate
                }
            }
        }
    }

    // onItemClick of adapter
    fun setSelectedGoalItem(goal: Goal) {
        _goal.value = goal
    }

    fun deleteGoal(userId: Int) {
        viewModelScope.launch {
            val job = launch {
                _goal.value?.let {
                    repository.deleteGoal(it)
                }
            }
            job.join()
            withContext(Dispatchers.Main) {
                _goal.value = null
            }
        }
    }

    fun updateGoal(userId: Int, goalName: String, targetAmount: Float, savedAmount: Float = 0f, goalColor: Int = 0, goalIcon: Int = 0, desiredDate: String = "", goalStatus: String = GoalStatus.ACTIVE.value) {
        viewModelScope.launch(Dispatchers.IO) {
            var updatedGoal: Goal? = null
            val job = launch {
                _goal.value?.let {
                    updatedGoal = Goal(userId, goalName, targetAmount).apply {
                        this.savedAmount = savedAmount
                        this.goalColor = goalColor
                        this.goalIcon = goalIcon
                        this.desiredDate = desiredDate
                        this.goalId = it.goalId
                        this.goalStatus = goalStatus
                    }
                    repository.updateGoal(updatedGoal!!)
                    fetchAllGoals(userId)
                }
            }
            job.join()
            withContext(Dispatchers.Main) {
                _goal.value = updatedGoal.also {
                    Log.e("Goal","updated goal " + updatedGoal.toString())
                }
            }
        }
    }

    var goalColor: Int = 0
    var goalIcon: Int = 0

    fun fetchAllGoals(userId: Int) {
        viewModelScope.launch {
            var allGoalsFetched: List<Goal>? = null
            val job = launch {
                allGoalsFetched = repository.fetchGoals(userId)
                Log.e("Goal", allGoalsFetched.toString())
            }
            job.join()
            withContext(Dispatchers.Main) {
                _goals.value = allGoalsFetched
            }
        }
    }

    fun clear() {
        _goal.value = null
        _goals.value = null
    }

    fun updateGoalStatus(goalStatus: String) {
        _goal.value?.let {
            updateGoal(it.userId, it.goalName, it.targetAmount, it.savedAmount, it.goalColor, it.goalIcon, it.desiredDate, GoalStatus.COMPLETED.value)
        }
    }

}