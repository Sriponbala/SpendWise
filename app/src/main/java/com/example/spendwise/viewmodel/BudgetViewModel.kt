package com.example.spendwise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.spendwise.database.SpendWiseDatabase
import com.example.spendwise.domain.Budget
import com.example.spendwise.repository.BudgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class BudgetViewModel(application: Application): AndroidViewModel(application) {

    private val repository = BudgetRepository(SpendWiseDatabase.getInstance(application).budgetDao)

    private val _budget = MutableLiveData<Budget?>()
    val budget: LiveData<Budget?>
    get() = _budget

    private val _budgetItem = MutableLiveData<Pair<Budget, BigDecimal>?>()
    val budgetItem: LiveData<Pair<Budget, BigDecimal>?>
        get() = _budgetItem


    private val _monthlyBudgets = MutableLiveData<List<Budget>?>()
    val monthlyBudgets: LiveData<List<Budget>?>
    get() = _monthlyBudgets

    val budgetCategoryAlreadyExists = MutableLiveData<Boolean>()

    var amountError: String = ""
    var budgetNameError = ""

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

    fun insertBudget(userId: Int, budgetName: String, maxAmount: String, category: String, period: String) {
        viewModelScope.launch {
            val job = launch {
                repository.insertBudget(Budget(userId, budgetName.trim(), maxAmount.trim(), category.trim(), period.trim()))
            }
        }
    }

    fun fetchBudgetsOfThePeriod(userId: Int, period: String) {
        viewModelScope.launch {
            var allBudgetsFetched: List<Budget>? = null
            val job = launch {
                allBudgetsFetched = repository.fetchBudgetsOfThePeriod(userId, period)
            }
            job.join()
            withContext(Dispatchers.Main) {
                _monthlyBudgets.value = allBudgetsFetched
            }
        }
    }

    fun setSelectedBudgetItem(budgetItem: Pair<Budget, BigDecimal>) {
        _budget.value = budgetItem.first
        _budgetItem.value = budgetItem
    }

    val period = MutableLiveData<String>()

    fun deleteBudget(userId: Int) {
        viewModelScope.launch {
            val job = launch {
                _budget.value?.let {
                    repository.deleteBudget(it)
                }
                period.value?.let {
                    fetchBudgetsOfThePeriod(userId, it)
                }
            }
            job.join()
            withContext(Dispatchers.Main) {
                _budget.value = null
                _budgetItem.value = null
            }
        }
    }

    fun updateBudget(userId: Int, budgetName: String, budgetAmount: String, period: String, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var updatedBudget: Budget? = null
            val job = launch {
                _budget.value?.let {
                    updatedBudget = Budget(userId = it.userId, budgetName = budgetName.trim(), maxAmount = budgetAmount.trim(), category = category.trim(), period = period.trim()).apply {
                        budgetId = it.budgetId
                    }
                    repository.updateBudget(updatedBudget!!)
                    fetchBudgetsOfThePeriod(userId, period)
                }
            }
            job.join()
            withContext(Dispatchers.Main) {
                _budgetItem.value?.let {
                    if(updatedBudget != null) {
                        _budgetItem.value = Pair(updatedBudget!!, it.second)
                    }
                }
                isBudgetUpdated = true
                _budget.value = updatedBudget
            }
        }
    }

    var isBudgetUpdated = false
    fun updateBudgetItemRecordsAmount(amount: BigDecimal) {
        _budget.value?.let {
            _budgetItem.value = Pair(it, amount)
        }
    }

    fun clear() {
        _budget.value = null
        _budgetItem.value = null
        _monthlyBudgets.value = null
    }
}