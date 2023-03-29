package com.example.spendwise.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserPassword(
    @PrimaryKey
    val userId: Int,
    var password: String
)