package com.example.spendwise.repository

import android.util.Log
import com.example.spendwise.dao.RecordDao
import com.example.spendwise.domain.Record
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecordRepository(private val recordDao: RecordDao) {

    suspend fun insertRecord(record: Record) {
        withContext(Dispatchers.IO) {
            Log.e("Record", "Success")
            recordDao.insertRecord(record)
        }
    }
}