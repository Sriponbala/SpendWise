package com.example.spendwise.fragment

import android.content.Context
import android.content.res.Configuration
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.adapter.BudgetRecyclerViewAdapter
import com.example.spendwise.databinding.FragmentBudgetBinding
import com.example.spendwise.domain.Budget
import com.example.spendwise.enums.Period
import com.example.spendwise.enums.RecordType
import com.example.spendwise.listeners.NavigationListener
import com.example.spendwise.viewmodel.BudgetViewModel
import com.example.spendwise.viewmodel.RecordViewModel
import com.example.spendwise.viewmodel.RestoreScrollPositionViewModel
import com.example.spendwise.viewmodelfactory.BudgetViewModelFactory
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory
import com.example.spendwise.viewmodelfactory.RestoreScrollPositionViewModelFactory
import java.math.BigDecimal

class BudgetFragment : Fragment() {

    private lateinit var binding: FragmentBudgetBinding
    private lateinit var budgetViewModel: BudgetViewModel
    private lateinit var recordViewModel: RecordViewModel
    private lateinit var navigationListener: NavigationListener
    private lateinit var restoreScrollPositionViewModel: RestoreScrollPositionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = BudgetViewModelFactory(requireActivity().application)
        budgetViewModel = ViewModelProvider(requireActivity(), factory)[BudgetViewModel::class.java]
        val recordViewModelFactory = RecordViewModelFactory(requireActivity().application)
        recordViewModel = ViewModelProvider(this, recordViewModelFactory)[RecordViewModel::class.java]
        val month = Calendar.getInstance().get(Calendar.MONTH) + 1
        val year = Calendar.getInstance().get(Calendar.YEAR)
        recordViewModel.month.value = month
        recordViewModel.year.value = year
        recordViewModel.recordType.value = RecordType.EXPENSE.value
        val userId = (activity as MainActivity).getSharedPreferences(resources.getString(R.string.loginStatus), Context.MODE_PRIVATE).getInt(resources.getString(R.string.userId), 0)
        recordViewModel.fetchAllRecords(userId)
        val restoreScrollPositionFactory = RestoreScrollPositionViewModelFactory(requireActivity().application)
        restoreScrollPositionViewModel = ViewModelProvider(this, restoreScrollPositionFactory)[RestoreScrollPositionViewModel::class.java]
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBudgetBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        binding.toolbarBudgets.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        navigationListener = parentFragment?.parentFragment as HomePageFragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        restoreScrollPositionViewModel.budgetScrollPosition.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.rootScrollViewBudget.scrollTo(0, it)
            }
        }

        val userId = (activity as MainActivity).getSharedPreferences(resources.getString(R.string.loginStatus), Context.MODE_PRIVATE).getInt(resources.getString(R.string.userId), 0)
        binding.showMonthlyBudgets.setOnClickListener {
            moveToNextFragment(Period.MONTH)
        }
        recordViewModel.allRecords.observe(viewLifecycleOwner) {
            recordViewModel.fetchRecords()
        }

        budgetViewModel.fetchBudgetsOfThePeriod(userId, Period.MONTH.value)
        budgetViewModel.monthlyBudgets.observe(viewLifecycleOwner) { listOfBudgets ->
            if (listOfBudgets != null) {
                if (listOfBudgets.isNotEmpty()) {
                    binding.thisMonthNoBudgetsTv.visibility = View.GONE
                    binding.thisMonthBudgetsList.visibility = View.VISIBLE
                    var adapterData = mutableListOf<Pair<Budget, BigDecimal>>()

                    recordViewModel.filteredRecords.observe(
                        viewLifecycleOwner
                    ) { listOfRecords ->
                        if (listOfRecords != null) {
                            if (listOfRecords.isEmpty()) {
                                val data = mutableListOf<Pair<Budget, BigDecimal>>()
                                listOfBudgets.forEach {
                                    data.add(Pair(it, BigDecimal(0)))
                                }
                                adapterData = data
                            } else {
                                val data = mutableListOf<Pair<Budget, BigDecimal>>()
                                listOfBudgets.forEach { budget ->
                                    var total = BigDecimal(0)
                                    listOfRecords.forEach { record ->
                                        if (budget.category == record.category) {
                                            total += (record.amount).toBigDecimal()
                                        }
                                    }
                                    data.add(Pair(budget, total))
                                }
                                adapterData = data
                            }
                        } else {
                            listOfBudgets.forEach {
                                adapterData.add(Pair(it, BigDecimal(0)))
                            }
                        }

                    }
                    setAdapter(adapterData)
                } else {
                    binding.thisMonthNoBudgetsTv.visibility = View.VISIBLE
                    setAdapter(emptyList())
                }
            } else {
                binding.thisMonthNoBudgetsTv.visibility = View.VISIBLE
                setAdapter(emptyList())
            }
        }

    }

    override fun onPause() {
        super.onPause()
        restoreScrollPositionViewModel.updateBudgetScrollPosition(binding.rootScrollViewBudget.scrollY)
    }

    private fun setAdapter(adapterData: List<Pair<Budget, BigDecimal>>) {
        val adapter = BudgetRecyclerViewAdapter(adapterData)
        adapter.setTheFragment(this)
        adapter.onItemClick = {
            navigationListener.onActionReceived(ViewBudgetFragment(), budget = it)
        }
        binding.thisMonthBudgetsList.adapter = adapter
        val spanCount = if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
            resources.configuration.screenWidthDp >= 600) {
            binding.thisMonthBudgetsList.setBackgroundColor(resources.getColor(R.color.behindScreen))
            2
        } else {
            1
        }
        binding.thisMonthBudgetsList.layoutManager = GridLayoutManager(this.context, spanCount)
    }

    private fun moveToNextFragment(period: Period) {
        navigationListener.onActionReceived(MonthlyBudgetsFragment(), title = RecordType.EXPENSE, period = period)
    }

}