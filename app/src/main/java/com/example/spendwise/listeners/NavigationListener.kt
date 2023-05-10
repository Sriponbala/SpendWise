package com.example.spendwise.listeners

import androidx.fragment.app.Fragment
import com.example.spendwise.domain.Budget
import com.example.spendwise.domain.Record
import com.example.spendwise.enums.Period
import com.example.spendwise.enums.RecordType
import java.math.BigDecimal

interface NavigationListener {
    fun onActionReceived(destination: Fragment, title: RecordType = RecordType.ALL, period: Period = Period.MONTH, record: Record? = null, budget: Pair<Budget, BigDecimal>? = null)

    fun changeVisibilityOfFab(showFab: Boolean)

}