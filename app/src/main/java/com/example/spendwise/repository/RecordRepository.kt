package com.example.spendwise.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.spendwise.dao.RecordDao
import com.example.spendwise.domain.Record
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class RecordRepository(private val recordDao: RecordDao) {

    suspend fun insertRecord(record: Record) {
        withContext(Dispatchers.IO) {
            Log.e("Record", "Success ${record.type}")
            recordDao.insertRecord(record)
        }
    }

    fun getAllRecords(userId: Int): LiveData<List<Record>> {
        Log.e("RecordRepo", userId.toString())
        return recordDao.getAllRecords(userId)
    }

    suspend fun getAllUserRecords(userId: Int): List<Record> {
        return withContext(Dispatchers.IO) {
            val list = recordDao.getAllUserRecords(userId)
            Log.e("Coroutine", userId.toString() + " repo getusers")
            list
        }
    }

    suspend fun updateRecord(record: Record) {
       withContext(Dispatchers.IO) {
           recordDao.update(record)
       }
    }

    suspend fun deleteRecord(record: Record) {
        withContext(Dispatchers.IO) {
            recordDao.delete(record)
        }
    }

    /*suspend fun deleteAllRecords() {
        withContext(Dispatchers.IO) {
            recordDao.deleteAllRecords()
        }
    }*/
}