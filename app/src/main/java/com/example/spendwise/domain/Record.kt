package com.example.spendwise.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Record(
    var userId: Int,
    var category: String,
    var amount: String,
    var type: String,
    var date: Date
) {
    @PrimaryKey(autoGenerate = true)
    var recordId: Int = 0
    var note: String = ""
    var description = ""
}
