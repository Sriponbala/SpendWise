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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.adapter.BudgetRecyclerViewAdapter
import com.example.spendwise.databinding.FragmentMonthlyBudgetsBinding
import com.example.spendwise.domain.Budget
import com.example.spendwise.enums.Period
import com.example.spendwise.enums.RecordType
import com.example.spendwise.interfaces.FilterViewDelegate
import com.example.spendwise.viewmodel.BudgetViewModel
import com.example.spendwise.viewmodel.RecordViewModel
import com.example.spendwise.viewmodel.RestoreScrollPositionViewModel
import com.example.spendwise.viewmodelfactory.BudgetViewModelFactory
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory
import com.example.spendwise.viewmodelfactory.RestoreScrollPositionViewModelFactory
import java.math.BigDecimal

class MonthlyBudgetsFragment : Fragment(), FilterViewDelegate {

    private lateinit var binding: FragmentMonthlyBudgetsBinding
    private lateinit var budgetViewModel: BudgetViewModel
    private lateinit var recordViewModel: RecordViewModel
    private lateinit var restoreScrollPositionViewModel: RestoreScrollPositionViewModel
    private var filterView: FilterView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = BudgetViewModelFactory(requireActivity().application)
        budgetViewModel = ViewModelProvider(requireActivity(), factory)[BudgetViewModel::class.java]
        val recordViewModelFactory = RecordViewModelFactory(requireActivity().application)
        recordViewModel = ViewModelProvider(requireActivity(), recordViewModelFactory)[RecordViewModel::class.java]
        val restoreScrollPositionFactory = RestoreScrollPositionViewModelFactory(requireActivity().application)
        restoreScrollPositionViewModel = ViewModelProvider(this, restoreScrollPositionFactory)[RestoreScrollPositionViewModel::class.java]

        if(savedInstanceState == null) {
            val month = Calendar.getInstance().get(Calendar.MONTH) + 1
            val year = Calendar.getInstance().get(Calendar.YEAR)
            recordViewModel.month.value = month
            recordViewModel.year.value = year
            recordViewModel.recordType.value = RecordType.EXPENSE.value
        }
        val userId = (activity as MainActivity).getSharedPreferences(resources.getString(R.string.loginStatus), Context.MODE_PRIVATE).getInt(resources.getString(R.string.userId), 0)
        recordViewModel.fetchAllRecords(userId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMonthlyBudgetsBinding.inflate(inflater, container, false)

        binding.toolbarMonthlyBudgets.apply {
            setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }
        }

        filterView = FilterView(recordViewModel, binding.budgetFragmentFilter, this, resources)
        filterView?.setMonthYearValue()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.budgetsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    restoreScrollPositionViewModel.scrollPositionMonthlyBudgets = (binding.budgetsRecycler.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
                }
            }
        })

        val userId = (activity as MainActivity).getSharedPreferences(resources.getString(R.string.loginStatus), Context.MODE_PRIVATE).getInt(resources.getString(R.string.userId), 0)

        binding.budgetFragmentFilter.monthAndYearTv.setOnClickListener {
            filterView?.showMonthYearPicker(requireContext())
        }

        budgetViewModel.fetchBudgetsOfThePeriod(userId, Period.MONTH.value)
        budgetViewModel.monthlyBudgets.observe(viewLifecycleOwner) { listOfBudgets ->
            if (listOfBudgets != null) {
                if (listOfBudgets.isNotEmpty()) {
                    binding.budgetFragmentFilter.filterLayoutLinear.visibility = View.VISIBLE
                    binding.scrollViewEmptyDataMonthlyBudgets.visibility = View.GONE
                    binding.budgetsRecycler.visibility = View.VISIBLE
                    var adapterData = mutableListOf<Pair<Budget, BigDecimal>>()
                    binding.budgetFragmentFilter.apply {
                        spinner.visibility = View.GONE
                        recordTypeTv.apply {
                            text = Period.MONTH.value
                            visibility = View.VISIBLE
                        }
                    }

                    recordViewModel.allRecords.observe(viewLifecycleOwner) {
                        recordViewModel.fetchRecords()
                    }

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
                    binding.scrollViewEmptyDataMonthlyBudgets.visibility = View.VISIBLE
                    binding.emptyMonthlyBudgets.emptyDataImage.setImageResource(R.drawable.schedule)
                    binding.emptyMonthlyBudgets.emptyDataText.text = resources.getString(R.string.no_monthly_budgets_info)
                    binding.budgetFragmentFilter.filterLayoutLinear.visibility = View.GONE
                    setAdapter(emptyList())
                }
            } else {
                binding.scrollViewEmptyDataMonthlyBudgets.visibility = View.VISIBLE
                binding.emptyMonthlyBudgets.emptyDataImage.setImageResource(R.drawable.schedule)
                binding.emptyMonthlyBudgets.emptyDataText.text = resources.getString(R.string.no_monthly_budgets_info)
                binding.budgetFragmentFilter.filterLayoutLinear.visibility = View.GONE
                setAdapter(emptyList())
            }
        }

        recordViewModel.month.observe(viewLifecycleOwner) {
            budgetViewModel.fetchBudgetsOfThePeriod(userId, Period.MONTH.value)
            recordViewModel.fetchRecords()
        }

        recordViewModel.year.observe(viewLifecycleOwner) {
            budgetViewModel.fetchBudgetsOfThePeriod(userId, Period.MONTH.value)
            recordViewModel.fetchRecords()
        }

        recordViewModel.fetchAllRecords(userId)

        binding.fabMonthlyBudgetsPage.setOnClickListener {
            findNavController().navigate(R.id.action_monthlyBudgetsFragment_to_addBudgetFragment)
        }
    }

    private fun setAdapter(adapterData: List<Pair<Budget, BigDecimal>>) {
        val adapter = BudgetRecyclerViewAdapter(adapterData)
        adapter.setTheFragment(this)
        adapter.onItemClick = {
            budgetViewModel.setSelectedBudgetItem(it)
            val action = MonthlyBudgetsFragmentDirections.actionMonthlyBudgetsFragmentToViewBudgetFragment()
            findNavController().navigate(action)
        }
        binding.budgetsRecycler.adapter = adapter
        val spanCount = if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
                resources.configuration.screenWidthDp >= 600) {
            binding.budgetsRecycler.setBackgroundColor(resources.getColor(R.color.behindScreen))
            2
        } else {
            binding.budgetsRecycler.setBackgroundColor(resources.getColor(R.color.recordPage))
            1
        }
        binding.budgetsRecycler.layoutManager = GridLayoutManager(this.context, spanCount)
        (binding.budgetsRecycler.layoutManager as GridLayoutManager).scrollToPosition(restoreScrollPositionViewModel.scrollPositionMonthlyBudgets)
    }

    override fun intimateSelectedDate(month: Int, year: Int) {
        recordViewModel.month.value = month
        recordViewModel.year.value = year
    }

    override fun intimateSelectedRecordType(recordType: String) {}

}