package com.example.spendwise.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
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
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
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
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.color.MaterialColors
import java.math.BigDecimal

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var navigationListener: NavigationListener
    private lateinit var recordViewModel: RecordViewModel
    private lateinit var restoreScrollPositionViewModel: RestoreScrollPositionViewModel
    private lateinit var adapter: RecordRecyclerViewAdapter
    private val quoteViewModel: QuoteViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("Test", "onCreate dashboard")
        Log.e("Landscape", "dashboard onCreate")
        /*navigationListener = parentFragment?.parentFragment as HomePageFragment
        navigationListener.changeVisibilityOfFab(true)*/
        val recordViewModelFactory = RecordViewModelFactory(requireActivity().application)
        recordViewModel = ViewModelProvider(this, recordViewModelFactory)[RecordViewModel::class.java]
        Log.e("Landscape", "dashboard onCreate - ${recordViewModel.month.value} - ${recordViewModel.month.value}")
        val factory = RestoreScrollPositionViewModelFactory(requireActivity().application)
        restoreScrollPositionViewModel = ViewModelProvider(this, factory)[RestoreScrollPositionViewModel::class.java]
        val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
//        recordViewModel.fetchAllRecords(userId)
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
            Log.e("Coroutine", "$userId dashboard allrecords obs $it")
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
                Log.e("Coroutine", "$userId dashboard filtered rec obs $it")
                Log.e("Record", "filtered rec obs " + it.size.toString() )
                for(i in it) {
                    Log.e("Coroutine", "$userId dashboard filtered rec for loop $i")
                    Log.e("Record", "filtered rec obs " + i.toString() )
                }
                if(it.isEmpty()) {
                    binding.currentMonthIncomeText.text = "--"//Helper.formatNumberToIndianStyle(0f)
                    binding.currentMonthExpenseText.text = "--"//Helper.formatNumberToIndianStyle(0f)
                    binding.currentMonthTotalText.text = "--"//Helper.formatNumberToIndianStyle(0f)
//                    binding.incomeAmount.text = "${resources.getString(R.string.rupee_symbol)} ${Helper.formatNumberToIndianStyle(0f)}"
                    binding.incomeAmount.visibility = View.GONE
                    binding.expenseAmount.visibility = View.GONE
//                    binding.expenseAmount.text = "${resources.getString(R.string.rupee_symbol)} ${Helper.formatNumberToIndianStyle(0f)}"
                    binding.recordsOverviewNoRecords.visibility = View.VISIBLE
                    binding.noIncomeTv.visibility = View.VISIBLE
                    binding.noExpenseTv.visibility = View.VISIBLE
                    binding.incomeChart.visibility = View.GONE
                    binding.expenseChart.visibility = View.GONE
                    adapter = RecordRecyclerViewAdapter(it, Categories.categoryList)
                    adapter.setTheFragment(this)
                    binding.recordsOverViewList.adapter = adapter
                    val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
                        resources.configuration.screenWidthDp >= 600) {
//                        binding.recordsOverViewList.setBackgroundColor(resources.getColor(R.color.behindScreen))
                        2
                    } else {
//                        binding.recordsOverViewList.setBackgroundColor(resources.getColor(R.color.recordPage))
                        1
                    }
                    val layoutManager = GridLayoutManager(this.context, spanCount)//LinearLayoutManager(this.context)
                    binding.recordsOverViewList.layoutManager = layoutManager
                } else {
                    binding.recordsOverviewNoRecords.visibility = View.GONE
                    binding.incomeAmount.visibility = View.VISIBLE
                    binding.expenseAmount.visibility = View.VISIBLE
                    recordViewModel.getIncomeOfTheMonth()
                    recordViewModel.getExpenseOfTheMonth()
                    recordViewModel.getTotalBalanceOfTheMonth()
                    binding.recordsOverViewList.visibility = View.VISIBLE
                    adapter = RecordRecyclerViewAdapter(it.takeLast(4).reversed(), Categories.categoryList)
                    adapter.setTheFragment(this)
                    val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
                        resources.configuration.screenWidthDp >= 600) {
//                        binding.recordsOverViewList.setBackgroundColor(resources.getColor(R.color.behindScreen))
                        2
                    } else {
//                        binding.recordsOverViewList.setBackgroundColor(resources.getColor(R.color.recordPage))
                        1
                    }
                    binding.recordsOverViewList.adapter = adapter
                    val layoutManager = GridLayoutManager(this.context, spanCount)//LinearLayoutManager(this.context)
                    binding.recordsOverViewList.layoutManager = layoutManager
                    adapter.onItemClick = { record ->
                        Log.e("Coroutine", "selected record dashboard $record")
                        navigationListener.onActionReceived(ViewRecordFragment(), record = record)
                    }
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
                        } else {
                            binding.noIncomeTv.visibility = View.VISIBLE
                            binding.incomeChart.visibility = View.GONE
                            Log.e("Record", "income data for pie chart")
                            Log.e("Record", "income list - $it")
                            drawPieChart(RecordType.INCOME, emptyList(), binding.incomeChart)
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
                        } else {
                            binding.noExpenseTv.visibility = View.VISIBLE
                            binding.expenseChart.visibility = View.GONE
                            Log.e("Record", "expense data for piechart")
                            Log.e("Record", "expense list - $it")
                            drawPieChart(RecordType.EXPENSE, emptyList(), binding.expenseChart)
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
                //Helper.adjustFontSizeToTextView(binding.currentMonthIncomeText, "$amt")
                if(amt == "0.00") {
                    binding.incomeAmount.visibility = View.GONE
                    binding.currentMonthIncomeText.text = "--"
                } else {
                    binding.incomeAmount.visibility = View.VISIBLE
                    binding.incomeAmount.text = "${resources.getString(R.string.rupee_symbol)} $amt"
                    binding.currentMonthIncomeText.text = "$amt"
                }

            }
        })
        recordViewModel.expenseOfTheMonth.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                val amt = Helper.formatNumberToIndianStyle(it)
                if(amt == "0.00") {
                    binding.expenseAmount.visibility = View.GONE
                    binding.currentMonthExpenseText.text = "--"
                } else {
                    binding.expenseAmount.visibility = View.VISIBLE
                    binding.expenseAmount.text = "${resources.getString(R.string.rupee_symbol)} $amt"
                    binding.currentMonthExpenseText.text = "$amt"
                }
//                binding.expenseAmount.text = "${resources.getString(R.string.rupee_symbol)} $amt"
            }
        })
        recordViewModel.totalBalanceOfTheMonth.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                val amt = Helper.formatNumberToIndianStyle(it)
                Log.e("Income", "net balance" + amt.toString())
                binding.currentMonthTotalText.text = "$amt"
              //  binding.totalAmountTV.text = "${resources.getString(R.string.rupee_symbol)} $it"
            }
        })

        binding.currentMonthTotalText.setOnClickListener {
            if(it != null && binding.currentMonthTotalText.text.toString().isNotEmpty()) {
                if(binding.currentMonthTotalText.text.toString() == "--") {
                    showDialog("Net Balance", binding.currentMonthTotalText.text.toString())
                } else {
                    showDialog("Net Balance", "₹ ${binding.currentMonthTotalText.text.toString()}")
                }
            }
        }

        binding.currentMonthExpenseText.setOnClickListener {
            if(it != null && binding.currentMonthExpenseText.text.toString().isNotEmpty()) {
                if(binding.currentMonthExpenseText.text.toString() == "--") {
                    showDialog("Expense", binding.currentMonthExpenseText.text.toString())
                } else {
                    showDialog("Expense", "₹ ${binding.currentMonthExpenseText.text.toString()}")
                }
            }
        }

        binding.currentMonthIncomeText.setOnClickListener {
            if(it != null && binding.currentMonthIncomeText.text.toString().isNotEmpty()) {
                if(binding.currentMonthIncomeText.text.toString() == "--") {
                    showDialog("Income", binding.currentMonthIncomeText.text.toString())
                } else {
                    showDialog("Income", "₹ ${binding.currentMonthIncomeText.text.toString()}")
                }
            }
        }
    }

    private fun drawPieChart(recordType: RecordType, list: List<Pair<Category, BigDecimal>>, pieChart: PieChart) {
        try {
            var total = BigDecimal(0)
            val data = ArrayList<PieEntry>()
            val sliceColors = mutableListOf<Int>()
            list.forEach { value ->
                data.add(PieEntry(value.second.toFloat(), value.first.title))
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
            /*pieChart.centerText = """${recordType.value}
                |₹ ${Helper.formatNumberToIndianStyle(total)}
            """.trimMargin()*/
            pieChart.centerText = """${recordType.value}""".trimMargin()
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
        } catch (exception: Exception) {
            Log.e("Error", "nep: ${exception.localizedMessage}")
        }

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

    @SuppressLint("MissingInflatedId")
    private fun showDialog(title: String, amount: String) {
       // val dialogView = LayoutInflater.from(context).inflate(R.layout.alert_text_view, null)
       // val alertTextView = dialogView.findViewById<TextView>(R.id.alertTextView)
        val dialog = AlertDialog.Builder(context).setTitle(title).setMessage(amount)
            .setPositiveButton("Close", null)
            .setNegativeButton("", null)
            .create()

        dialog.show()
    }
}