package com.example.spendwise.viewmodel

import android.app.Application
import android.content.res.Resources
import android.view.Menu
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.spendwise.Helper
import com.example.spendwise.R
import com.example.spendwise.database.SpendWiseDatabase
import com.example.spendwise.domain.Goal
import com.example.spendwise.enums.GoalStatus
import com.example.spendwise.repository.GoalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class GoalViewModel(application: Application, private val resources: Resources): AndroidViewModel(application) {

    var menu: Menu? = null

    private val repository = GoalRepository(SpendWiseDatabase.getInstance(application).goalDao)

    private val _goal = MutableLiveData<Goal?>()
    val goal: LiveData<Goal?>
        get() = _goal

    private val _goals = MutableLiveData<List<Goal>?>()
    val goals: LiveData<List<Goal>?>
        get() = _goals

    var savedAmountError = ""
    var targetAmountError = ""
    var goalNameError = ""

    fun insertGoal(userId: Int, goalName: String, targetAmount: String, savedAmount: String = resources.getString(
        R.string.zero), goalColor: Int = 0, goalIcon: Int = 0, desiredDate: String = "") {
        viewModelScope.launch {
                val goal = Goal(userId, goalName.trim(), Helper.formatDecimalToThreePlaces(BigDecimal(targetAmount.trim()))).apply {
                    this.savedAmount = Helper.formatDecimalToThreePlaces(BigDecimal(savedAmount.trim()))
                    this.goalColor = goalColor
                    this.goalIcon = goalIcon
                    this.desiredDate = desiredDate.trim()
                }
                repository.insertGoal(goal)

        }
    }

    fun setSelectedGoalItem(goal: Goal) {
        _goal.value = goal
    }

    fun deleteGoal() {
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

    fun updateGoal(userId: Int, goalName: String, targetAmount: String, savedAmount: String = resources.getString(
        R.string.zero), goalColor: Int = 0, goalIcon: Int = 0, desiredDate: String = "", goalStatus: String = GoalStatus.ACTIVE.value) {
        viewModelScope.launch(Dispatchers.IO) {
            var updatedGoal: Goal? = null
            val job = launch {
                _goal.value?.let {
                    updatedGoal = Goal(userId, goalName.trim(), Helper.formatDecimalToThreePlaces(BigDecimal(targetAmount.trim()))).apply {
                        this.savedAmount = Helper.formatDecimalToThreePlaces(BigDecimal(savedAmount.trim()))
                        this.goalColor = goalColor
                        this.goalIcon = goalIcon
                        this.desiredDate = desiredDate.trim()
                        this.goalId = it.goalId
                        this.goalStatus = goalStatus.trim()
                    }
                    repository.updateGoal(updatedGoal!!)
                    fetchAllGoals(userId)
                }
            }
            job.join()
            withContext(Dispatchers.Main) {
                _goal.value = updatedGoal
            }
        }
    }

    var goalColor: Int = 0
    var goalIcon: Int = 0

    fun fetchAllGoals(userId: Int) {
        viewModelScope.launch {
            var allGoalsFetched: List<Goal>? = null
            val job = viewModelScope.launch {
                allGoalsFetched = repository.fetchGoals(userId)
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

    fun updateGoalStatus() {
        _goal.value?.let {
            updateGoal(it.userId, it.goalName, it.targetAmount, it.savedAmount, it.goalColor, it.goalIcon, it.desiredDate, GoalStatus.CLOSED.value)
        }
    }

}