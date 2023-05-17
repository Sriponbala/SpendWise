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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
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
        val recordViewModelFactory = RecordViewModelFactory(requireActivity().application)
        recordViewModel = ViewModelProvider(this, recordViewModelFactory)[RecordViewModel::class.java]
        val factory = RestoreScrollPositionViewModelFactory(requireActivity().application)
        restoreScrollPositionViewModel = ViewModelProvider(this, factory)[RestoreScrollPositionViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).supportActionBar?.apply {
            title = resources.getString(R.string.dashboard)
            setDisplayHomeAsUpEnabled(false)
        }

        navigationListener = parentFragment?.parentFragment as HomePageFragment
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        if(savedInstanceState == null) {
            quoteViewModel.getRandomQuote()
        }

        quoteViewModel.quote.observe(viewLifecycleOwner) {
            if (it != null) {
                val quoteText =
                    """|${resources.getString(R.string.openingDoubleQuotes)} ${it.text} ${
                        resources.getString(R.string.closingDoubleQuotes)
                    }
                    """.trimMargin()
                binding.quoteLayout.quote.text = quoteText
                binding.quoteLayout.quoteAuthor.text = resources.getString(R.string.two_strings_concate, resources.getString(R.string.empty_data_fill_in_value), it.author)
                binding.quoteLayout.quoteAuthor.visibility = View.VISIBLE
            } else {
                binding.quoteLayout.quote.text = resources.getString(R.string.no_network)
                binding.quoteLayout.quoteAuthor.visibility = View.GONE
            }
        }

        binding.showRecords.setOnClickListener {
            navigationListener.onActionReceived(RecordsFragment())
        }

        binding.showIncomeStats.setOnClickListener {
            navigationListener.onActionReceived(StatisticsFragment(), RecordType.INCOME)
        }

        binding.showExpenseStats.setOnClickListener {
            navigationListener.onActionReceived(StatisticsFragment(), RecordType.EXPENSE)
        }

        val userId = (activity as MainActivity).getSharedPreferences(resources.getString(R.string.loginStatus), Context.MODE_PRIVATE).getInt(resources.getString(R.string.userId), 0)
        recordViewModel.fetchAllRecords(userId)

        val month = Calendar.getInstance().get(Calendar.MONTH) + 1
        val year = Calendar.getInstance().get(Calendar.YEAR)
        recordViewModel.month.value = month
        recordViewModel.year.value = year
        recordViewModel.recordType.value = RecordType.ALL.value

        recordViewModel.allRecords.observe(viewLifecycleOwner) {
            recordViewModel.fetchRecords()
        }

        restoreScrollPositionViewModel.dashboardScrollPosition.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.rootScrollView.scrollTo(0, it)
            }
        }

        recordViewModel.filteredRecords.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.isEmpty()) {
                    binding.currentMonthIncomeText.text = resources.getString(R.string.no_data_value)
                    binding.currentMonthExpenseText.text = resources.getString(R.string.no_data_value)
                    binding.currentMonthTotalText.text = resources.getString(R.string.no_data_value)
                    binding.incomeAmount.visibility = View.GONE
                    binding.expenseAmount.visibility = View.GONE
                    binding.recordsOverviewNoRecords.visibility = View.VISIBLE
                    binding.noIncomeTv.visibility = View.VISIBLE
                    binding.noExpenseTv.visibility = View.VISIBLE
                    binding.incomeChart.visibility = View.GONE
                    binding.expenseChart.visibility = View.GONE
                    adapter = RecordRecyclerViewAdapter(it, Categories.categoryList)
                    adapter.setTheFragment(this)
                    binding.recordsOverViewList.adapter = adapter
                    val spanCount =
                        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
                            resources.configuration.screenWidthDp >= 600
                        ) {
                            2
                        } else {
                            1
                        }
                    val layoutManager = GridLayoutManager(this.context, spanCount)
                    binding.recordsOverViewList.layoutManager = layoutManager
                } else {
                    binding.recordsOverviewNoRecords.visibility = View.GONE
                    binding.incomeAmount.visibility = View.VISIBLE
                    binding.expenseAmount.visibility = View.VISIBLE
                    recordViewModel.getIncomeOfTheMonth()
                    recordViewModel.getExpenseOfTheMonth()
                    recordViewModel.getTotalBalanceOfTheMonth()
                    binding.recordsOverViewList.visibility = View.VISIBLE
                    adapter = RecordRecyclerViewAdapter(
                        it.takeLast(4).reversed(),
                        Categories.categoryList
                    )
                    adapter.setTheFragment(this)
                    val spanCount =
                        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
                            resources.configuration.screenWidthDp >= 600
                        ) {
                            2
                        } else {
                            1
                        }
                    binding.recordsOverViewList.adapter = adapter
                    val layoutManager = GridLayoutManager(this.context, spanCount)
                    binding.recordsOverViewList.layoutManager = layoutManager
                    adapter.onItemClick = { record ->
                        navigationListener.onActionReceived(ViewRecordFragment(), record = record)
                    }
                    recordViewModel.getDataTransformed(it, RecordType.INCOME)
                    recordViewModel.getDataTransformed(it, RecordType.EXPENSE)
                }
            }

        }

        recordViewModel.isIncomeDataUpdated.observe(viewLifecycleOwner) { flag ->
            if (flag != null) {
                if (flag) {
                    recordViewModel.dataForIncomePieChart.observe(viewLifecycleOwner) {
                        if (it != null && it.isNotEmpty()) {
                            binding.noIncomeTv.visibility = View.GONE
                            binding.incomeChart.visibility = View.VISIBLE
                            drawPieChart(RecordType.INCOME, it, binding.incomeChart)
                        } else {
                            binding.noIncomeTv.visibility = View.VISIBLE
                            binding.incomeChart.visibility = View.GONE
                            drawPieChart(RecordType.INCOME, emptyList(), binding.incomeChart)
                        }
                    }

                }
                recordViewModel.isIncomeDataUpdated.value = null
            }
        }

        recordViewModel.isExpenseDataUpdated.observe(viewLifecycleOwner) { flag ->
            if (flag != null) {
                if (flag) {
                    recordViewModel.dataForExpensePieChart.observe(viewLifecycleOwner) {
                        if (it != null && it.isNotEmpty()) {
                            binding.noExpenseTv.visibility = View.GONE
                            binding.expenseChart.visibility = View.VISIBLE
                            drawPieChart(RecordType.EXPENSE, it, binding.expenseChart)
                        } else {
                            binding.noExpenseTv.visibility = View.VISIBLE
                            binding.expenseChart.visibility = View.GONE
                            drawPieChart(RecordType.EXPENSE, emptyList(), binding.expenseChart)
                        }
                    }

                }
                recordViewModel.isExpenseDataUpdated.value = null
            }
        }

        recordViewModel.incomeOfTheMonth.observe(viewLifecycleOwner) {
            if (it != null) {
                val amt = Helper.formatNumberToIndianStyle(it)
                if (amt == resources.getString(R.string.zero)) {
                    binding.incomeAmount.visibility = View.GONE
                    binding.currentMonthIncomeText.text = resources.getString(R.string.no_data_value)
                } else {
                    binding.incomeAmount.visibility = View.VISIBLE
                    binding.incomeAmount.text = resources.getString(R.string.amount_format, amt) //"${resources.getString(R.string.rupee_symbol)} $amt"
                    binding.currentMonthIncomeText.text = amt
                }

            }
        }
        recordViewModel.expenseOfTheMonth.observe(viewLifecycleOwner) {
            if (it != null) {
                val amt = Helper.formatNumberToIndianStyle(it)
                if (amt == resources.getString(R.string.zero)) {
                    binding.expenseAmount.visibility = View.GONE
                    binding.currentMonthExpenseText.text = resources.getString(R.string.no_data_value)
                } else {
                    binding.expenseAmount.visibility = View.VISIBLE
                    binding.expenseAmount.text = resources.getString(R.string.amount_format, amt)
                       // "${resources.getString(R.string.rupee_symbol)} $amt"
                    binding.currentMonthExpenseText.text = amt
                }
            }
        }
        recordViewModel.totalBalanceOfTheMonth.observe(viewLifecycleOwner) {
            if (it != null) {
                val amt = Helper.formatNumberToIndianStyle(it)
                binding.currentMonthTotalText.text = amt
            }
        }

        binding.currentMonthTotalText.setOnClickListener {
            if(it != null && binding.currentMonthTotalText.text.toString().isNotEmpty()) {
                if(binding.currentMonthTotalText.text.toString() == resources.getString(R.string.no_data_value)) {
                    showDialog(resources.getString(R.string.netBalance), binding.currentMonthTotalText.text.toString())
                } else {
                    showDialog(resources.getString(R.string.netBalance), resources.getString(R.string.amount_format, binding.currentMonthTotalText.text))
                }
            }
        }

        binding.currentMonthExpenseText.setOnClickListener {
            if(it != null && binding.currentMonthExpenseText.text.toString().isNotEmpty()) {
                if(binding.currentMonthExpenseText.text.toString() == resources.getString(R.string.no_data_value)) {
                    showDialog(resources.getString(R.string.expense_label), binding.currentMonthExpenseText.text.toString())
                } else {
                    showDialog(resources.getString(R.string.expense_label), resources.getString(R.string.amount_format, binding.currentMonthExpenseText.text))
                }
            }
        }

        binding.currentMonthIncomeText.setOnClickListener {
            if(it != null && binding.currentMonthIncomeText.text.toString().isNotEmpty()) {
                if(binding.currentMonthIncomeText.text.toString() == resources.getString(R.string.no_data_value)) {
                    showDialog(resources.getString(R.string.income_label), binding.currentMonthIncomeText.text.toString())
                } else {
                    showDialog(resources.getString(R.string.income_label), resources.getString(R.string.amount_format, binding.currentMonthIncomeText.text))
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
            val pieDataSet = PieDataSet(data, resources.getString(R.string.list))
            pieDataSet.colors = sliceColors
            pieDataSet.valueTextSize = 1f
            pieDataSet.valueTextColor = Color.BLACK
            pieDataSet.setDrawValues(true)
            val pieData = PieData(pieDataSet)
            pieChart.setDrawEntryLabels(false)
            pieChart.isHighlightPerTapEnabled = false
            pieChart.centerText = recordType.value.trimMargin()
            val centerOfChartColor = view?.let { MaterialColors.getColor(it, com.google.android.material.R.attr.colorTertiaryContainer) }
            if (centerOfChartColor != null) {
                pieChart.setHoleColor(centerOfChartColor)
            }
            pieChart.setCenterTextColor(resources.getColor(R.color.textColor))
            pieChart.data = pieData
            pieChart.description.text = ""
            pieChart.holeRadius = 60f
            pieChart.transparentCircleRadius = 0f
            pieChart.legend.isEnabled = false
            pieChart.invalidate()
        } catch (exception: Exception) {
            Log.e("Error", "nep: ${exception.localizedMessage}")
        }

    }

    override fun onPause() {
        super.onPause()
        restoreScrollPositionViewModel.updateDashboardScrollPosition(binding.rootScrollView.scrollY)
    }

    @SuppressLint("MissingInflatedId")
    private fun showDialog(title: String, amount: String) {
        val dialog = AlertDialog.Builder(context).setTitle(title).setMessage(amount)
            .setPositiveButton(resources.getString(R.string.close_button), null)
            .setNegativeButton("", null)
            .create()
        dialog.show()
    }

}