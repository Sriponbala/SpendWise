package com.example.spendwise.viewmodelfactory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.spendwise.repository.UserAccountRepository
import com.example.spendwise.viewmodel.UserViewModel

class UserViewModelFactory(
    private val application: Application
   ): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserViewModel(application) as T
    }
}