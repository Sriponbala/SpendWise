package com.example.spendwise.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class RestoreScrollPositionViewModel(application: Application): AndroidViewModel(application) {

    private val _dashboardScrollPosition = MutableLiveData<Int?>()
    val dashboardScrollPosition: LiveData<Int?>
    get() = _dashboardScrollPosition

    fun updateDashboardScrollPosition(position: Int) {
        _dashboardScrollPosition.value = position
    }

    private val _budgetScrollPosition = MutableLiveData<Int?>()
    val budgetScrollPosition: LiveData<Int?>
        get() = _budgetScrollPosition

    fun updateBudgetScrollPosition(position: Int) {
        _budgetScrollPosition.value = position
    }

    private val _statsScrollPosition = MutableLiveData<Int?>()
    val statsScrollPosition: LiveData<Int?>
        get() = _statsScrollPosition

    fun updateStatsScrollPosition(position: Int) {
        Log.e("Scroll", position.toString())
        _statsScrollPosition.value = position
        Log.e("Scroll", _statsScrollPosition.value.toString())
    }

    private val _viewRecordScrollPosition = MutableLiveData<Int?>()
    val viewRecordScrollPosition: LiveData<Int?>
        get() = _viewRecordScrollPosition

    fun updateViewRecordScrollPosition(position: Int) {
        Log.e("Scroll", position.toString())
        _viewRecordScrollPosition.value = position
        Log.e("Scroll", _viewRecordScrollPosition.value.toString())
    }

    var scrollPositionMonthlyBudgets: Int = 0
    var scrollPositionRecords: Int = 0
    var scrollPositionGoals: Int = 0
    var scrollPositionCategory: Int = 0

    fun clear() {
        _dashboardScrollPosition.value = null
        _budgetScrollPosition.value = null
        _viewRecordScrollPosition.value = null
    }

}