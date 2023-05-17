package com.example.spendwise.fragment

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.spendwise.R
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.spendwise.Helper
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.adapter.StatisticsAdapter
import com.example.spendwise.databinding.FragmentStatisticsBinding
import com.example.spendwise.domain.Category
import com.example.spendwise.enums.Period
import com.example.spendwise.interfaces.FilterViewDelegate
import com.example.spendwise.viewmodel.RecordViewModel
import com.example.spendwise.viewmodel.RestoreScrollPositionViewModel
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory
import com.example.spendwise.viewmodelfactory.RestoreScrollPositionViewModelFactory
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.math.BigDecimal
import kotlin.collections.ArrayList

class StatisticsFragment : Fragment(), FilterViewDelegate, OnChartValueSelectedListener {

    private lateinit var binding: FragmentStatisticsBinding
    private lateinit var recordType: String
    private lateinit var recordViewModel: RecordViewModel
    private lateinit var restoreScrollPositionViewModel: RestoreScrollPositionViewModel
    private var filterView: FilterView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recordViewModelFactory = RecordViewModelFactory(requireActivity().application)
        recordViewModel = ViewModelProvider(requireActivity(), recordViewModelFactory)[RecordViewModel::class.java]
        val factory = RestoreScrollPositionViewModelFactory(requireActivity().application)
        restoreScrollPositionViewModel = ViewModelProvider(this, factory)[RestoreScrollPositionViewModel::class.java]
        val args = StatisticsFragmentArgs.fromBundle(requireArguments())
        recordType = args.recordType
        if(savedInstanceState == null) {
            val month = android.icu.util.Calendar.getInstance().get(android.icu.util.Calendar.MONTH) + 1
            val year = android.icu.util.Calendar.getInstance().get(android.icu.util.Calendar.YEAR)
            recordViewModel.month.value = month
            recordViewModel.year.value = year
            recordViewModel.recordType.value = recordType
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        binding.toolbarStatistics.apply {
            title = resources.getString(R.string.two_strings_concate, recordType, resources.getString(R.string.stats_label))
            setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }
        }

        filterView = FilterView(recordViewModel, binding.filterLayoutRecordsFragment, this, resources)
        filterView?.setMonthYearValue()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.filterLayoutRecordsFragment.monthAndYearTv.setOnClickListener {
            filterView?.showMonthYearPicker(requireContext())
        }
        restoreScrollPositionViewModel.statsScrollPosition.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.scrollViewStats.scrollTo(0, it)
            }
        }

        val userId = (activity as MainActivity).getSharedPreferences(resources.getString(R.string.loginStatus), Context.MODE_PRIVATE).getInt(resources.getString(R.string.userId), 0)

        binding.filterLayoutRecordsFragment.apply {
            spinner.visibility = View.GONE
            recordTypeTv.apply {
                text = recordType
                visibility = View.VISIBLE
            }
        }

        recordViewModel.fetchAllRecords(userId)
        recordViewModel.allRecords.observe(viewLifecycleOwner) {
            recordViewModel.fetchRecords()
        }

        recordViewModel.month.observe(viewLifecycleOwner) {
            recordViewModel.fetchRecords()
        }

        recordViewModel.year.observe(viewLifecycleOwner) {
            recordViewModel.fetchRecords()
        }

        recordViewModel.recordType.observe(viewLifecycleOwner) {
            recordViewModel.fetchRecords()
        }

        recordViewModel.filteredRecords.observe(viewLifecycleOwner) {
            if (it != null) {
                recordViewModel.transformedDataForPieChart(it)
                recordViewModel.isDataForPieChartUpdated.observe(
                    viewLifecycleOwner
                ) { flag ->
                    if (flag != null) {
                        if (flag) {
                            recordViewModel.dataForPieChart.value?.let { data ->
                                if (data.isNotEmpty()) {
                                    binding.emptyRecordsList.emptyDataLinearRoot.visibility =
                                        View.GONE
                                    binding.emptyScrollView.visibility = View.GONE
                                    binding.pieChart.visibility = View.VISIBLE
                                    binding.statsRecyclerView.visibility = View.VISIBLE
                                    drawPieChart(data)
                                    setStatisticsRecyclerView(data)
                                } else {
                                    binding.emptyRecordsList.emptyDataLinearRoot.visibility =
                                        View.VISIBLE
                                    binding.emptyRecordsList.emptyDataImage.setImageResource(R.drawable.stats)
                                    binding.emptyRecordsList.emptyDataText.text =
                                        resources.getString(R.string.no_stats_info)
                                    binding.emptyScrollView.visibility = View.VISIBLE
                                    binding.pieChart.visibility = View.GONE
                                    binding.statsRecyclerView.visibility = View.GONE
                                }
                            }

                        }
                        recordViewModel.isDataForPieChartUpdated.value = null
                    }
                }
            }
        }
        binding.pieChart.setOnChartValueSelectedListener(this)
    }

    private fun moveToNextFragment() {
        val action = StatisticsFragmentDirections.actionStatisticsFragmentToRecordsFragment(hideFilterView = true,
        hideAmountView = true, hideDescriptionText = false, selectedCategory = recordViewModel.category.value)
        findNavController().navigate(action)
    }

    private fun setStatisticsRecyclerView(list: List<Pair<Category, BigDecimal>>) {
        val adapter = StatisticsAdapter(list)
        binding.statsRecyclerView.adapter = adapter
        val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
            resources.configuration.screenWidthDp >= 600) {
                        binding.statsRecyclerView.setBackgroundColor(resources.getColor(R.color.behindScreen))
            2
        } else {
                        binding.statsRecyclerView.setBackgroundColor(resources.getColor(R.color.recordPage))
            1
        }
        binding.statsRecyclerView.layoutManager = GridLayoutManager(this.context, spanCount)
        adapter.onItemClick = {
            recordViewModel.period = Period.MONTH
            recordViewModel.category.value = it.first
            moveToNextFragment()
        }
    }

    private fun drawPieChart(list: List<Pair<Category, BigDecimal>>) {

        var total = BigDecimal(0)
        val data = ArrayList<PieEntry>()
        val sliceColors = mutableListOf<Int>()
        list.forEach { value ->
            data.add(PieEntry(value.second.toFloat(), value.first.title))
            sliceColors.add(resources.getColor(value.first.bgColor))
            total += value.second
        }

        val pieDataSet = PieDataSet(data, resources.getString(R.string.two_strings_concate, recordType, resources.getString(R.string.stats_label)))

        pieDataSet.colors = sliceColors
        pieDataSet.valueTextSize = 1f
        pieDataSet.valueTextColor = Color.BLACK
        pieDataSet.setDrawValues(true)
        pieDataSet.valueFormatter = PercentFormatter(binding.pieChart)

        val pieData = PieData(pieDataSet)


        binding.pieChart.setDrawEntryLabels(false)
        binding.pieChart.setUsePercentValues(true)
        binding.pieChart.data = pieData
        binding.pieChart.description.text = ""
        binding.pieChart.centerText = """$recordType
            |${resources.getString(R.string.amount_format, Helper.formatNumberToIndianStyle(total))} 
        """.trimMargin()
        //₹ ${Helper.formatNumberToIndianStyle(total)}
        binding.pieChart.setCenterTextColor(resources.getColor(R.color.black))
        binding.pieChart.holeRadius = 60f
        binding.pieChart.transparentCircleRadius = 0f
        binding.pieChart.animateY(1000)
        binding.pieChart.legend.isEnabled = false
        binding.pieChart.invalidate()
    }

    override fun intimateSelectedDate(month: Int, year: Int) {
        recordViewModel.month.value = month
        recordViewModel.year.value = year
    }

    override fun intimateSelectedRecordType(recordType: String) {
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        binding.pieChart.centerText = """${(e as  PieEntry).label}
            |${resources.getString(R.string.amount_format, Helper.formatNumberToIndianStyle(e.value))} 
        """.trimMargin()
        //${Helper.formatNumberToIndianStyle(e.value)}
    }

    override fun onNothingSelected() {
        binding.pieChart.centerText = recordType
    }

    override fun onPause() {
        super.onPause()
        restoreScrollPositionViewModel.updateDashboardScrollPosition(binding.scrollViewStats.scrollY)
    }

}