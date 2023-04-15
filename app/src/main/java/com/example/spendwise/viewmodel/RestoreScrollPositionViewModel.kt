package com.example.spendwise.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RestoreScrollPositionViewModel(application: Application): AndroidViewModel(application) {

    private val _dashboardScrollPosition = MutableLiveData<Int>()
    val dashboardScrollPosition: LiveData<Int>
    get() = _dashboardScrollPosition

    fun updateDashboardScrollPosition(position: Int) {
        Log.e("Scroll", position.toString())
        _dashboardScrollPosition.value = position
        Log.e("Scroll", _dashboardScrollPosition.value.toString())
    }

}