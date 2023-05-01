package com.example.spendwise.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RestoreScrollPositionViewModel(application: Application): AndroidViewModel(application) {

    private val _dashboardScrollPosition = MutableLiveData<Int?>()
    val dashboardScrollPosition: LiveData<Int?>
    get() = _dashboardScrollPosition

    fun updateDashboardScrollPosition(position: Int) {
        Log.e("Scroll", position.toString())
        _dashboardScrollPosition.value = position
        Log.e("Scroll", _dashboardScrollPosition.value.toString())
    }

    private val _budgetScrollPosition = MutableLiveData<Int?>()
    val budgetScrollPosition: LiveData<Int?>
        get() = _budgetScrollPosition

    fun updateBudgetScrollPosition(position: Int) {
        Log.e("Scroll", position.toString())
        _budgetScrollPosition.value = position
        Log.e("Scroll", _budgetScrollPosition.value.toString())
    }

    private val _statsScrollPosition = MutableLiveData<Int?>()
    val statsScrollPosition: LiveData<Int?>
        get() = _statsScrollPosition

    fun updateStatsScrollPosition(position: Int) {
        Log.e("Scroll", position.toString())
        _statsScrollPosition.value = position
        Log.e("Scroll", _statsScrollPosition.value.toString())
    }

    var scrollPositionMonthlyBudgets: Int = 0
    var scrollPositionRecords: Int = 0
    var scrollPositionGoals: Int = 0
    var scrollPositionCategory: Int = 0

    fun clear() {
        _dashboardScrollPosition.value = null
        _budgetScrollPosition.value = null
    }

}