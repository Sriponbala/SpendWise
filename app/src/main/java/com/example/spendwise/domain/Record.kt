package com.example.spendwise.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "record_table")
data class Record(
    @PrimaryKey(autoGenerate = true)
    val recordId: Int,
    val userId: Int,
    val category: String,
    val amount: Float,
    val type: String,
    val date: String
)
