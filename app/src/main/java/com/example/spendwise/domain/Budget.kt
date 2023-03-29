package com.example.spendwise.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val budgetId: Int,
    val userId: Int,
    val budgetName: String,
    val maxAmount: Float,
    val category: String,
    val period: String
)
