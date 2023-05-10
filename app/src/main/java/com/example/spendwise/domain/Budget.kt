package com.example.spendwise.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Budget(
    val userId: Int,
    val budgetName: String,
    val maxAmount: String,
    val category: String,
    val period: String
) {
    @PrimaryKey(autoGenerate = true)
    var budgetId: Int = 0
}
