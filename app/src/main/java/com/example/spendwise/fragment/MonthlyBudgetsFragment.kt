package com.example.spendwise.fragment

import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.example.spendwise.viewmodelfactory.BudgetViewModelFactory
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory

class MonthlyBudgetsFragment : Fragment(), FilterViewDelegate {

    private lateinit var binding: FragmentMonthlyBudgetsBinding
    private lateinit var budgetViewModel: BudgetViewModel
    private lateinit var recordViewModel: RecordViewModel
    private var filterView: FilterView? = null  // FilterView(this, requireActivity().application)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = BudgetViewModelFactory(requireActivity().application)
        budgetViewModel = ViewModelProvider(requireActivity(), factory)[BudgetViewModel::class.java]
        val recordViewModelFactory = RecordViewModelFactory(requireActivity().application)
        recordViewModel = ViewModelProvider(requireActivity(), recordViewModelFactory)[RecordViewModel::class.java]

        if(savedInstanceState == null) {
            val month = Calendar.getInstance().get(Calendar.MONTH) + 1
            val year = Calendar.getInstance().get(Calendar.YEAR)
            recordViewModel.month.value = month
            recordViewModel.year.value = year
            recordViewModel.recordType.value = RecordType.EXPENSE.value
            Log.e("Landscape", "monthly budgtes frag onCreate")
        } else {
            Log.e("Landscape", "monthly budget in onCreate - month - ${recordViewModel.month.value}")
            Log.e("Landscape", "monthly budget in onCreate - year - ${recordViewModel.year.value}")
            Log.e("Landscape", "monthly budget in onCreate - type - ${recordViewModel.recordType.value}")
        }
        val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
        recordViewModel.fetchAllRecords(userId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("Landscape", "monthly budgets frag onDestroy")
        /*filterView?.clear()
        filterView = null*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (activity as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back_arrow)
        }
        Log.e("Landscape", "monthly budgtes frag onCreateView")
        Log.e("Landscape", "monthly budgtes frag onCreateView ${recordViewModel.month.value}")
        Log.e("Landscape", "monthly budgtes frag onCreateView ${recordViewModel.year.value}")
        Log.e("Landscape", "monthly budgtes frag oncreateView${recordViewModel.recordType.value}")
        binding = FragmentMonthlyBudgetsBinding.inflate(inflater, container, false)
        filterView = FilterView(recordViewModel.also { Log.e("Landscape", "blah blah ${it.month.value}") }, binding.budgetFragmentFilter, this)
        filterView?.setMonthYearValue()
        Log.e("Landscape", "monthly budgtes frag onCreateView")
        Log.e("Landscape", "monthly budgtes frag onCreateView ${recordViewModel.month.value}")
        Log.e("Landscape", "monthly budgtes frag onCreateView ${recordViewModel.year.value}")
        Log.e("Landscape", "monthly budgtes frag oncreateView${recordViewModel.recordType.value}")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        Log.e("Landscape", "monthly budgtes frag onViewCreate")
        Log.e("Landscape", "monthly budgtes frag ${recordViewModel.month.value}")
        Log.e("Landscape", "monthly budgtes frag ${recordViewModel.year.value}")
        Log.e("Landscape", "monthly budgtes frag ${recordViewModel.recordType.value}")

        val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
        Log.e("userId monthly budgets", userId.toString())

        binding.budgetFragmentFilter.monthAndYearTv.setOnClickListener {
            filterView?.showMonthYearPicker(requireContext())
        }


       // val monthName = Month.values()[month-1].value
        /*binding.budgetFragmentFilter.apply {
            monthAndYearTv.text = "$monthName $year"
        }*/

        budgetViewModel.fetchBudgetsOfThePeriod(userId, Period.MONTH.value)
        budgetViewModel.monthlyBudgets.observe(viewLifecycleOwner, Observer { listOfBudgets ->
            if(listOfBudgets != null) {
                if(listOfBudgets.isNotEmpty()) {
                    var adapterData = mutableListOf<Pair<Budget, Float>>()
                    binding.budgetFragmentFilter.apply {
                        spinner.visibility = View.GONE
                        recordTypeTv.apply {
                            text = Period.MONTH.value
                            visibility = View.VISIBLE
                        }
                    }

                    recordViewModel.allRecords.observe(viewLifecycleOwner, Observer {
                        Log.e("Budget", userId.toString() + " monthly budgets allrecords obs ")
                        recordViewModel.fetchRecords()
                    })

                    /*restoreScrollPositionViewModel.dashboardScrollPosition.observe(viewLifecycleOwner, Observer {
                        Log.e("Scroll", it.toString() + "observe")
                        binding.rootScrollView.scrollTo(0, it)
                    })*/

                    recordViewModel.filteredRecords.observe(viewLifecycleOwner, Observer { listOfRecords ->
                        if(listOfRecords != null) {
                            Log.e("Budget", "monthly budget - filtered rec obs " + listOfRecords.size.toString() )
                            for(i in listOfRecords) {
                                Log.e("Budget", "monthly budget - filtered rec obs $i")
                            }
                            if(listOfRecords.isEmpty()) {
                                Log.e("Budget", "records empty")
                                val data = mutableListOf<Pair<Budget, Float>>()
                                listOfBudgets.forEach {
                                    data.add(Pair(it, 0f))
                                }
                                adapterData = data
                            } else {
                                Log.e("Budget", "records not empty")
                                val data = mutableListOf<Pair<Budget, Float>>()
                                listOfBudgets.forEach { budget ->
                                    var total = 0f
                                    listOfRecords.forEach { record ->
                                        if(budget.category == record.category) {
                                            total += record.amount
                                        }
                                    }
                                    data.add(Pair(budget, total))
                                }
                                adapterData = data
                            }
                        } else {
                            Log.e("Budget", "records - Null")
                            listOfBudgets.forEach {
                                adapterData.add(Pair(it, 0f))
                            }
                        }

                    })
                    setAdapter(adapterData)
                } else {
                    Log.e("Budget", "budgets empty")
                }
            } else Log.e("Budget", "budgets - Null")
        })

        recordViewModel.month.observe(viewLifecycleOwner, Observer {
            Log.e("month", it.toString())
            Log.e("Landscape", "monthly budget in obs - month - $it")
            budgetViewModel.fetchBudgetsOfThePeriod(userId, Period.MONTH.value)
            recordViewModel.fetchRecords()
        })

        recordViewModel.year.observe(viewLifecycleOwner, Observer {
            Log.e("year", it.toString())
            Log.e("Landscape", "monthly budget in obs - year - $it")
            budgetViewModel.fetchBudgetsOfThePeriod(userId, Period.MONTH.value)
            recordViewModel.fetchRecords()
        })

        recordViewModel.fetchAllRecords(userId)
    }

    private fun setAdapter(adapterData: List<Pair<Budget, Float>>) {
        val adapter = BudgetRecyclerViewAdapter(adapterData)
        adapter.onItemClick = {
            budgetViewModel.setSelectedBudgetItem(it)
            val action = MonthlyBudgetsFragmentDirections.actionMonthlyBudgetsFragmentToViewBudgetFragment()
            findNavController().navigate(action)
        }
        binding.budgetsRecycler.adapter = adapter
        binding.budgetsRecycler.layoutManager = LinearLayoutManager(this.requireContext())
    }

    override fun intimateSelectedDate(month: Int, year: Int) {
        recordViewModel.month.value = month.also {
            Log.e("Landscape", "monthly budget in intimateSelectedDate - month - $it")
        }
        recordViewModel.year.value = year.also {
            Log.e("Landscape", "monthly budget in intimateSelectedDate - year - $it")
        }
    }

    override fun intimateSelectedRecordType(recordType: String) {

    }

}