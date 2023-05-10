package com.example.spendwise.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.spendwise.enums.GoalStatus

@Entity
data class Goal(
    val userId: Int,
    val goalName: String,
    val targetAmount: String,
) {
    @PrimaryKey(autoGenerate = true)
    var goalId: Int = 0
    var savedAmount: String = "0.00"
    var goalColor: Int = 0
    var goalIcon: Int = 0
    var desiredDate: String = ""
    var goalStatus: String = GoalStatus.ACTIVE.value
}
