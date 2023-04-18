package com.example.spendwise.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.spendwise.enums.GoalStatus

@Entity
data class Goal(
    val userId: Int,
    val goalName: String,
    val targetAmount: Float,
) {
    @PrimaryKey(autoGenerate = true)
    var goalId: Int = 0
    var savedAmount: Float = 0f
    var goalColor: Int = 0
    var goalIcon: Int = 0
    var desiredDate: String = ""
    var goalStatus: String = GoalStatus.ACTIVE.value
}
