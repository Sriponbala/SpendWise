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
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.adapter.BudgetRecyclerViewAdapter
import com.example.spendwise.databinding.FragmentBudgetBinding
import com.example.spendwise.domain.Budget
import com.example.spendwise.enums.Period
import com.example.spendwise.enums.RecordType
import com.example.spendwise.listeners.NavigationListener
import com.example.spendwise.viewmodel.BudgetViewModel
import com.example.spendwise.viewmodel.RecordViewModel
import com.example.spendwise.viewmodelfactory.BudgetViewModelFactory
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory

class BudgetFragment : Fragment() {

    private lateinit var binding: FragmentBudgetBinding
    private lateinit var budgetViewModel: BudgetViewModel
    private lateinit var recordViewModel: RecordViewModel
    private lateinit var navigationListener: NavigationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = BudgetViewModelFactory(requireActivity().application)
        budgetViewModel = ViewModelProvider(requireActivity(), factory)[BudgetViewModel::class.java]
        val recordViewModelFactory = RecordViewModelFactory(requireActivity().application)
        recordViewModel = ViewModelProvider(requireActivity(), recordViewModelFactory)[RecordViewModel::class.java]

        val month = Calendar.getInstance().get(Calendar.MONTH) + 1
        val year = Calendar.getInstance().get(Calendar.YEAR)
        recordViewModel.month.value = month
        recordViewModel.year.value = year
        recordViewModel.recordType.value = RecordType.EXPENSE.value
        Log.e("Budget", "onCreate ")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (activity as MainActivity).supportActionBar?.apply {
            title = "Budgets"
            setDisplayHomeAsUpEnabled(false)
        }
        binding = FragmentBudgetBinding.inflate(inflater, container, false)
        navigationListener = parentFragment?.parentFragment as HomePageFragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
        Log.e("userId budget", userId.toString())
//        recordViewModel.fetchAllRecords(userId)
        binding.showMonthlyBudgets.setOnClickListener {
            moveToNextFragment(Period.MONTH)
        }
        val month = Calendar.getInstance().get(Calendar.MONTH) + 1
        val year = Calendar.getInstance().get(Calendar.YEAR)
        recordViewModel.month.value = month
        recordViewModel.year.value = year
        recordViewModel.recordType.value = RecordType.EXPENSE.value
        Log.e("Budget", "onCreate ")
        recordViewModel.fetchRecords()

        budgetViewModel.fetchBudgetsOfThePeriod(userId, Period.MONTH.value)
        budgetViewModel.monthlyBudgets.observe(viewLifecycleOwner, Observer { listOfBudgets ->
            if(listOfBudgets != null) {
                if(listOfBudgets.isNotEmpty()) {
                    binding.thisMonthNoBudgetsTv.visibility = View.GONE
                    binding.thisMonthBudgetsList.visibility = View.VISIBLE
                    var adapterData = mutableListOf<Pair<Budget, Float>>()

                    /*recordViewModel.allRecords.observe(viewLifecycleOwner, Observer {
                        Log.e("Budget", userId.toString() + " monthly budgets allrecords obs ")
                        recordViewModel.fetchRecords()
                    })*/

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
                    binding.thisMonthNoBudgetsTv.visibility = View.VISIBLE
                    binding.thisMonthBudgetsList.visibility = View.GONE
                    Log.e("Budget", "budgets empty")
                }
            } else {
                binding.thisMonthNoBudgetsTv.visibility = View.VISIBLE
                binding.thisMonthBudgetsList.visibility = View.GONE
                Log.e("Budget", "budgets - Null")
            }
        })

    }

    private fun setAdapter(adapterData: List<Pair<Budget, Float>>) {
        val adapter = BudgetRecyclerViewAdapter(adapterData)
        binding.thisMonthBudgetsList.adapter = adapter
        binding.thisMonthBudgetsList.layoutManager = LinearLayoutManager(this.requireContext())
    }

    private fun moveToNextFragment(period: Period) {
        navigationListener.onActionReceived(MonthlyBudgetsFragment(), title = RecordType.EXPENSE, period = period)
    }

}