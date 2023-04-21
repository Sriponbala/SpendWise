package com.example.spendwise.fragment

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendwise.Categories
import com.example.spendwise.Helper
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.adapter.RecordRecyclerViewAdapter
import com.example.spendwise.databinding.FragmentDashboardBinding
import com.example.spendwise.domain.Category
import com.example.spendwise.enums.RecordType
import com.example.spendwise.listeners.NavigationListener
import com.example.spendwise.viewmodel.QuoteViewModel
import com.example.spendwise.viewmodel.RecordViewModel
import com.example.spendwise.viewmodel.RestoreScrollPositionViewModel
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory
import com.example.spendwise.viewmodelfactory.RestoreScrollPositionViewModelFactory
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.color.MaterialColors

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var navigationListener: NavigationListener
    private lateinit var recordViewModel: RecordViewModel
    private lateinit var restoreScrollPositionViewModel: RestoreScrollPositionViewModel
    private lateinit var adapter: RecordRecyclerViewAdapter
    private val quoteViewModel: QuoteViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("Scroll", "onCreate")
        Log.e("Landscape", "dashboard onCreate")
        val recordViewModelFactory = RecordViewModelFactory(requireActivity().application)
        recordViewModel = ViewModelProvider(this, recordViewModelFactory)[RecordViewModel::class.java]
        Log.e("Landscape", "dashboard onCreate - ${recordViewModel.month.value} - ${recordViewModel.month.value}")
        val factory = RestoreScrollPositionViewModelFactory(requireActivity().application)
        restoreScrollPositionViewModel = ViewModelProvider(this, factory)[RestoreScrollPositionViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.e("Scroll", "onCreateView")
        (activity as MainActivity).supportActionBar?.apply {
            title = "Dashboard"
            setDisplayHomeAsUpEnabled(false)
        }

        Log.e("Landscape", "dashboard onCreateView - ${recordViewModel.month.value} - ${recordViewModel.year.value}")

        navigationListener = parentFragment?.parentFragment as HomePageFragment
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        if(savedInstanceState == null) {
            quoteViewModel.getRandomQuote()
        }

        quoteViewModel.quote.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                val quoteText = """|${resources.getString(R.string.openingDoubleQuotes)} ${it.text} ${resources.getString(R.string.closingDoubleQuotes)}
                    """.trimMargin()
                binding.quoteLayout.quote.text = quoteText
                binding.quoteLayout.quoteAuthor.text = "- ${it.author}"
                binding.quoteLayout.quoteAuthor.visibility = View.VISIBLE
                Log.e("Quote", it.toString())
            } else {
                binding.quoteLayout.quote.text = "Check your network connectivity"
                binding.quoteLayout.quoteAuthor.visibility = View.GONE

                Log.e("Quote", "Quote is null")
            }
        })

        binding.showRecords.setOnClickListener {
            navigationListener.onActionReceived(RecordsFragment())
        }

        binding.showIncomeStats.setOnClickListener {
            navigationListener.onActionReceived(StatisticsFragment(), RecordType.INCOME)
        }

        binding.showExpenseStats.setOnClickListener {
            navigationListener.onActionReceived(StatisticsFragment(), RecordType.EXPENSE)
        }

        val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
        Log.e("userId dashboard", userId.toString())
        recordViewModel.fetchAllRecords(userId)

        val month = Calendar.getInstance().get(Calendar.MONTH) + 1
        val year = Calendar.getInstance().get(Calendar.YEAR)
        recordViewModel.month.value = month
        recordViewModel.year.value = year
        recordViewModel.recordType.value = RecordType.ALL.value

        recordViewModel.allRecords.observe(viewLifecycleOwner, Observer {
            Log.e("Record", userId.toString() + " dashboard allrecords obs ")
            recordViewModel.fetchRecords()
        })

        restoreScrollPositionViewModel.dashboardScrollPosition.observe(viewLifecycleOwner, Observer {
            Log.e("Scroll", it.toString() + "observe")
            if (it != null) {
                binding.rootScrollView.scrollTo(0, it)
            }
        })

        recordViewModel.filteredRecords.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                Log.e("Record", "filtered rec obs " + it.size.toString() )
                for(i in it) {
                    Log.e("Record", "filtered rec obs " + i.toString() )
                }
                if(it.isEmpty()) {
                    binding.currentMonthIncomeText.text = Helper.formatNumberToIndianStyle(0f)
                    binding.incomeAmount.text = "${resources.getString(R.string.rupee_symbol)} ${Helper.formatNumberToIndianStyle(0f)}"
                    binding.expenseAmount.text = "${resources.getString(R.string.rupee_symbol)} ${Helper.formatNumberToIndianStyle(0f)}"
                    binding.recordsOverviewNoRecords.visibility = View.VISIBLE
                    binding.noIncomeTv.visibility = View.VISIBLE
                    binding.noExpenseTv.visibility = View.VISIBLE
                    binding.incomeChart.visibility = View.GONE
                    binding.expenseChart.visibility = View.GONE
                } else {
                    binding.recordsOverviewNoRecords.visibility = View.GONE
                    recordViewModel.getIncomeOfTheMonth()
                    recordViewModel.getExpenseOfTheMonth()
                    recordViewModel.getTotalBalanceOfTheMonth()
                    binding.recordsOverViewList.visibility = View.VISIBLE
                    adapter = RecordRecyclerViewAdapter(it.takeLast(4).reversed(), Categories.categoryList)
                    adapter.setTheFragment(this)
                    binding.recordsOverViewList.adapter = adapter
                    val layoutManager = LinearLayoutManager(this.context)
                    binding.recordsOverViewList.layoutManager = layoutManager
                    recordViewModel.getDataTransformed(it, RecordType.INCOME)
                    /*recordViewModel.incomeStatsDone.observe(viewLifecycleOwner, Observer { incomeDone ->
                        if(incomeDone != null) {
                            if(incomeDone) {
                                recordViewModel.getDataTransformed(it, RecordType.EXPENSE)
                            }
                            recordViewModel.incomeStatsDone.value = null
                        }

                    })*/

                    recordViewModel.getDataTransformed(it, RecordType.EXPENSE)
                }
            } else Log.e("Error dashboard", "Null")

        })

        recordViewModel.isIncomeDataUpdated.observe(viewLifecycleOwner, Observer { flag ->
            if(flag != null) {
                if(flag) {
                    recordViewModel.dataForIncomePieChart.observe(viewLifecycleOwner, Observer {
                        if(it != null && it.isNotEmpty()) {
                            binding.noIncomeTv.visibility = View.GONE
                            binding.incomeChart.visibility = View.VISIBLE
                            Log.e("Record", "income data for pie chart")
                            Log.e("Record", "income list - $it")
                            drawPieChart(RecordType.INCOME, it, binding.incomeChart)
                        }
                    })

                }
                recordViewModel.isIncomeDataUpdated.value = null
            }
        })

        recordViewModel.isExpenseDataUpdated.observe(viewLifecycleOwner, Observer { flag ->
            if(flag != null) {
                if(flag) {
                    recordViewModel.dataForExpensePieChart.observe(viewLifecycleOwner, Observer {
                        if(it != null && it.isNotEmpty()) {
                            binding.noExpenseTv.visibility = View.GONE
                            binding.expenseChart.visibility = View.VISIBLE
                            Log.e("Record", "expense data for piechart")
                            Log.e("Record", "expense list - $it")
                            drawPieChart(RecordType.EXPENSE, it, binding.expenseChart)
                        }
                    })

                }
                recordViewModel.isExpenseDataUpdated.value = null
            }
        })

/*
        recordViewModel.isDataForPieChartUpdated.observe(viewLifecycleOwner, Observer { flag ->
            if(flag != null) {
                if(flag) {
                    recordViewModel.dataForPieChart.observe(viewLifecycleOwner, Observer {
                        if(it != null && it.isNotEmpty()) {
                            if(it[0].first.recordType == RecordType.INCOME) {
                                binding.noIncomeTv.visibility = View.GONE
                                binding.incomeChart.visibility = View.VISIBLE
                                Log.e("Record", "income data for pie chart")
                                drawPieChart(RecordType.INCOME, it, binding.incomeChart)
                                recordViewModel.incomeStatsDone.value = true
                            } else {
                                binding.noExpenseTv.visibility = View.GONE
                                binding.expenseChart.visibility = View.VISIBLE
                                Log.e("Record", "expense data for piechart")
                                drawPieChart(RecordType.EXPENSE, it, binding.expenseChart)
                                recordViewModel.incomeStatsDone.value = false
                            }
                        } else {
                            recordViewModel.incomeStatsDone.value = true
                        }
                    })

                }
                recordViewModel.isDataForPieChartUpdated.value = null
            }
        })
*/

        recordViewModel.incomeOfTheMonth.observe(viewLifecycleOwner, Observer {
            Log.e("Income", it.toString())
            if(it != null) {
                Log.e("Income", it.toString())
                val amt = Helper.formatNumberToIndianStyle(it)
                Log.e("Income", amt.toString())
                binding.currentMonthIncomeText.text = "$amt"
                binding.incomeAmount.text = "${resources.getString(R.string.rupee_symbol)} $amt"
            }
        })
        recordViewModel.expenseOfTheMonth.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                val amt = Helper.formatNumberToIndianStyle(it)
                binding.currentMonthExpenseText.text = "$amt"
                binding.expenseAmount.text = "${resources.getString(R.string.rupee_symbol)} $amt"
            }
        })
        recordViewModel.totalBalanceOfTheMonth.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                val amt = Helper.formatNumberToIndianStyle(it)
                binding.currentMonthTotalText.text = "$amt"
              //  binding.totalAmountTV.text = "${resources.getString(R.string.rupee_symbol)} $it"
            }
        })

    }

    private fun drawPieChart(recordType: RecordType, list: List<Pair<Category, Float>>, pieChart: PieChart) {

        var total = 0f
        val data = ArrayList<PieEntry>()
        val sliceColors = mutableListOf<Int>()
        list.forEach { value ->
            data.add(PieEntry(value.second, value.first.title))
            sliceColors.add(resources.getColor(value.first.bgColor))
            total += value.second
        }

        val pieDataSet = PieDataSet(data, "List")

        pieDataSet.colors = sliceColors
        pieDataSet.valueTextSize = 1f
        pieDataSet.valueTextColor = Color.BLACK
        pieDataSet.setDrawValues(true)

        val pieData = PieData(pieDataSet)

        pieChart.setDrawEntryLabels(false)
        pieChart.isHighlightPerTapEnabled = false
        pieChart.centerText = """${recordType.value}
            |₹ ${Helper.formatNumberToIndianStyle(total)}
        """.trimMargin()
        val centerOfChartColor = view?.let { MaterialColors.getColor(it, com.google.android.material.R.attr.colorTertiaryContainer) }
        if (centerOfChartColor != null) {
            pieChart.setHoleColor(centerOfChartColor)
        }
        pieChart.setCenterTextColor(resources.getColor(R.color.textColor))
//        pieChart.setOnChartValueSelectedListener(null)
//        binding.pieChart.description.textAlign = Paint.Align.RIGHT
        pieChart.data = pieData
        pieChart.description.text = ""
//        binding.pieChart.description.textSize = 25f
//        pieChart.centerText = recordType
        pieChart.holeRadius = 60f
        pieChart.transparentCircleRadius = 0f
//        pieChart.animateY(1000)
        pieChart.legend.isEnabled = false
        pieChart.invalidate()
    }

    override fun onPause() {

        super.onPause()
        Log.e("Scroll", "onPause")
        restoreScrollPositionViewModel.updateDashboardScrollPosition(binding.rootScrollView.scrollY)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("Scroll", "onDestroy")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e("Scroll", "onDestroyView")
    }

    override fun onDetach() {
        super.onDetach()
        Log.e("Scroll", "onDetach")
    }
}