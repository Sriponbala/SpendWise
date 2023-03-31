package com.example.spendwise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendwise.database.SpendWiseDatabase
import com.example.spendwise.domain.Record
import com.example.spendwise.repository.RecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecordViewModel(
    application: Application): AndroidViewModel(application) {

    private val repository = RecordRepository(SpendWiseDatabase.getInstance(application).recordDao)

    fun insertRecord(userId: Int, category: String, amount: String, type: String, date: String, _note: String, desc: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val job = launch {
                val record = Record(userId, category, amount.toFloat(), type, date).apply {
                    note = _note
                    description = desc
                }
                repository.insertRecord(record)
            }
        }
    }
}