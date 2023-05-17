package com.example.spendwise.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    var email: String,
) {
    @PrimaryKey(autoGenerate = true)
    var userId: Int = 0
    var firstName: String? = null
    var lastName: String? = null
    var mobile: String? = null
    var gender: String? = null
}

