package com.example.spendwise.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.spendwise.database.SpendWiseDatabase
import com.example.spendwise.domain.Budget
import com.example.spendwise.domain.Record
import com.example.spendwise.repository.BudgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BudgetViewModel(application: Application): AndroidViewModel(application) {

    private val repository = BudgetRepository(SpendWiseDatabase.getInstance(application).budgetDao)

    private val _budget = MutableLiveData<Budget?>()
    val budget: LiveData<Budget?>
    get() = _budget

    private val _budgetItem = MutableLiveData<Pair<Budget, Float>?>()
    val budgetItem: LiveData<Pair<Budget, Float>?>
        get() = _budgetItem


    private val _monthlyBudgets = MutableLiveData<List<Budget>?>()
    val monthlyBudgets: LiveData<List<Budget>?>
    get() = _monthlyBudgets

    val budgetCategoryAlreadyExists = MutableLiveData<Boolean>()

    fun checkIfCategoryAlreadyExists(userId: Int, category: String, period: String) {
        viewModelScope.launch {
            var categoryExists = false
            val job = launch {
                categoryExists = repository.checkIfCategoryAlreadyExists(userId, category, period)
            }
            job.join()
            withContext(Dispatchers.Main) {
                budgetCategoryAlreadyExists.value = categoryExists
            }
        }
    }

    fun insertBudget(userId: Int, budgetName: String, maxAmount: Float, category: String, period: String) {
        viewModelScope.launch {
            val job = launch {
                repository.insertBudget(Budget(userId, budgetName, maxAmount, category, period))
            }
        }
    }

    fun fetchBudgetsOfThePeriod(userId: Int, period: String) {
        viewModelScope.launch {
            var allBudgetsFetched: List<Budget>? = null
            val job = launch {
                allBudgetsFetched = repository.fetchBudgetsOfThePeriod(userId, period)
                Log.e("Budget", allBudgetsFetched.toString())
            }
            job.join()
            withContext(Dispatchers.Main) {
                _monthlyBudgets.value = allBudgetsFetched
            }
        }
    }

    // onItemClick of adapter
    fun setSelectedBudgetItem(budgetItem: Pair<Budget, Float>) {
        _budget.value = budgetItem.first
        _budgetItem.value = budgetItem
    }

    val period = MutableLiveData<String>()

    fun deleteBudget(userId: Int) {
        viewModelScope.launch {
            val job = launch {
                _budget.value?.let {
                    Log.e("Test", "deleting budget in viewmodel before repo call")
                    repository.deleteBudget(it)
                }
                period.value?.let {
                    Log.e("Test", "in deleting budget in viewmodel before  fetch budgets call")
                    fetchBudgetsOfThePeriod(userId, it)
                }
            }
            job.join()
            withContext(Dispatchers.Main) {
                /*if(filterByCategory) {
                    fetchAllRecords(user)
                    fetchRecords()
                }*/
                Log.e("Test", "in main thread deleting budget in viewmodel before updating budget value ")
                _budget.value = null
                _budgetItem.value = null
            }
        }
    }

//    val isBudgetUpdated = MutableLiveData<Boolean>()

    fun updateBudget(userId: Int, budgetName: String, budgetAmount: Float, period: String, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var updatedBudget: Budget? = null
            val job = launch {
                _budget.value?.let {
                    updatedBudget = Budget(userId = it.userId, budgetName = budgetName, maxAmount = budgetAmount, category = category, period = period).apply {
                        budgetId = it.budgetId
                    }
                    repository.updateBudget(updatedBudget!!)
                    fetchBudgetsOfThePeriod(userId, period)
                }
            }
            job.join()
            withContext(Dispatchers.Main) {
                _budgetItem.value?.let {
                    Log.e("ViewBudget", "${it.toString()} - budget item before updating budget 1 in viewmodel")
                    Log.e("Budget", "updated budget " + updatedBudget.toString())
                    if(updatedBudget != null) {
                        Log.e("ViewBudget", "${it.toString()} - budget item before updating budget 2 in viewmodel")
                        _budgetItem.value = Pair(updatedBudget!!, it.second)
                    }
                }
                isBudgetUpdated = true
                _budget.value = updatedBudget.also {
                    Log.e("ViewBudget", "${it.toString()} - budget while updating budget in viewmodel inside also")
                }
                /*_budgetItem.value?.let {
                    Log.e("Budget", "updated budget " + updatedBudget.toString())
                    if(updatedBudget != null) {
                        _budgetItem.value = Pair(updatedBudget!!, it.second)
                    }
                }*/
            }
        }
    }


    var isBudgetUpdated = false
    fun updateBudgetItemRecordsAmount(amount: Float) {
        Log.e("ViewBudget", "update budgetitem $amount in budget viewmodel" )
        _budget.value?.let {
            Log.e("ViewBudget", "update budgetitem $amount in budget viewmodel inside let 1" )
            _budgetItem.value = Pair(it, amount)
            Log.e("ViewBudget", "update budgetitem $amount in budget viewmodel inside let 2" )
        }
    }

    fun clear() {
        _budget.value = null
        _budgetItem.value = null
        _monthlyBudgets.value = null
    }
}