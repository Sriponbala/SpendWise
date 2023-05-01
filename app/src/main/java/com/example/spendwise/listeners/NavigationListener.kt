package com.example.spendwise.listeners

import androidx.fragment.app.Fragment
import com.example.spendwise.enums.Period
import com.example.spendwise.enums.RecordType

interface NavigationListener {
    fun onActionReceived(destination: Fragment, title: RecordType = RecordType.ALL, period: Period = Period.MONTH)

    fun changeVisibilityOfFab(showFab: Boolean)

}