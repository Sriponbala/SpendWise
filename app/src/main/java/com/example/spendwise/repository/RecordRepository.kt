package com.example.spendwise.repository

import com.example.spendwise.dao.RecordDao
import com.example.spendwise.domain.Record
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecordRepository(private val recordDao: RecordDao) {

    suspend fun insertRecord(record: Record) {
        withContext(Dispatchers.IO) {
            recordDao.insertRecord(record)
        }
    }

    suspend fun getAllUserRecords(userId: Int): List<Record> {
        return withContext(Dispatchers.IO) {
            val list = recordDao.getAllUserRecords(userId)
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

    suspend fun getAprilRecords(): List<Record> {
        return withContext(Dispatchers.IO) {
            recordDao.getAprilRecords()
        }
    }
}