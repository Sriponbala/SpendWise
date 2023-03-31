package com.example.spendwise.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.spendwise.domain.Record

@Dao
interface RecordDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertRecord(record: Record)

 /*   @Query("update record_table set category=:category, amount=:amount, type=:type, userId=:userId, date=:date where recordId like :recordId")
    fun updateRecord(recordId: Int, category: String, amount: Float, type: String, userId: Int, date: String)

    @Query("delete from record_table where recordId like :recordId")
    fun deleteRecord(recordId: Int)

    @Query("select * from record_table")
    fun getAllRecords(): LiveData<List<Record>>

    @Query("select * from record_table where type like :type and strftime('%m', date) = :month")
    fun getExpensesOfTheMonth(type: String, month: String): LiveData<List<Record>>

    @Query("select * from record_table where type=:type and category=:category")
    fun getExpensesForTheCategory(type: String, category: String): LiveData<List<Record>>

    @Query("select * from record_table where recordId=:recordId")
    fun getARecord(recordId: Int): Record*/

}