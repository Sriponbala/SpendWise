package com.example.spendwise.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.spendwise.domain.Record

@Dao
interface RecordDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertRecord(record: Record)

    @Query("select * from Record where userId like:userId order by date desc")
    fun getAllRecords(userId: Int): LiveData<List<Record>>

    @Query("select * from Record where userId like:userId order by date")
    fun getAllUserRecords(userId: Int): List<Record>

    @Query("update Record set category=:category, amount=:amount, type=:type, date=:date where recordId like :recordId")
    fun updateRecord(recordId: Int, category: String, amount: Float, type: String, date: String)

    @Update
    fun update(record: Record)

    @Delete
    fun delete(record: Record)

    @Query("SELECT * FROM Record WHERE strftime('%m %Y', date) = '4'")
    fun getAprilRecords(): List<Record>

}