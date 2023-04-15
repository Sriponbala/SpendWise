package com.example.spendwise.viewmodelfactory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.spendwise.viewmodel.BudgetViewModel
import com.example.spendwise.viewmodel.GoalViewModel

class GoalViewModelFactory(private val application: Application): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GoalViewModel(application) as T
    }

}