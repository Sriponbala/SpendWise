package com.example.spendwise.viewmodelfactory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.spendwise.viewmodel.RestoreScrollPositionViewModel

class RestoreScrollPositionViewModelFactory(private val application: Application): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RestoreScrollPositionViewModel(application) as T
    }

}