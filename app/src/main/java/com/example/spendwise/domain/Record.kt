package com.example.spendwise.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Record(
    var userId: Int,
    var category: String,
    var amount: Float,
    var type: String,
    var date: String
) {
    @PrimaryKey(autoGenerate = true)
    var recordId: Int = 0
    var note: String = ""
    var description = ""
}
