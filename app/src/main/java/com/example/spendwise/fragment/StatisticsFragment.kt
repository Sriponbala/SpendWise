package com.example.spendwise.fragment

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.method.Touch.scrollTo
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.spendwise.R
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendwise.Helper
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.adapter.StatisticsAdapter
import com.example.spendwise.databinding.FragmentStatisticsBinding
import com.example.spendwise.domain.Category
import com.example.spendwise.enums.Month
import com.example.spendwise.enums.Period
import com.example.spendwise.enums.RecordType
import com.example.spendwise.interfaces.FilterViewDelegate
import com.example.spendwise.viewmodel.CategoryViewModel
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
import java.util.*
import kotlin.collections.ArrayList

class StatisticsFragment : Fragment(), FilterViewDelegate, OnChartValueSelectedListener {

    private lateinit var binding: FragmentStatisticsBinding
    private lateinit var recordType: String
    private lateinit var recordViewModel: RecordViewModel
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private lateinit var restoreScrollPositionViewModel: RestoreScrollPositionViewModel
    private var filterView: FilterView? = null  // FilterView(this, requireActivity().application)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*val userViewModelFactory = UserViewModelFactory((activity as MainActivity).application)
        userViewModel = ViewModelProvider(requireActivity(), userViewModelFactory)[UserViewModel::class.java]
        Log.e("UserViewModel Records", userViewModel.toString())

        Log.e("User Record", userViewModel.user.value?.userId.toString())*/
        val recordViewModelFactory = RecordViewModelFactory(requireActivity().application)
        recordViewModel = ViewModelProvider(requireActivity(), recordViewModelFactory)[RecordViewModel::class.java]
        /*userViewModel.user.value?.userId?.let {
            Log.e("UserId Record", it.toString())
            recordViewModel.userId = it
        }*/
        Log.e("Landscape", "stats onCreate - ${recordViewModel.month.value} - ${recordViewModel.month.value}")
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
        /*(activity as MainActivity).supportActionBar?.apply {
            title = "$recordType Stats"
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back_arrow)
        }*/
        binding.toolbarStatistics.apply {
            title = "$recordType Stats"
            setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }
        }

        filterView = FilterView(recordViewModel, binding.filterLayoutRecordsFragment, this)
        Log.e("Landscape", "stats onCreateView - ${recordViewModel.month.value} - ${recordViewModel.month.value}")
        filterView?.setMonthYearValue()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.filterLayoutRecordsFragment.monthAndYearTv.setOnClickListener {
            filterView?.showMonthYearPicker(requireContext())
        }
//        filterView?.createAndSetAdapter(requireContext(), R.array.recordTypes)

        restoreScrollPositionViewModel.statsScrollPosition.observe(viewLifecycleOwner, Observer {
            Log.e("Scroll", it.toString() + "observe")
            if (it != null) {
                binding.scrollViewStats.scrollTo(0, it)
            }
        })

        val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
        Log.e("userId records", userId.toString())

        /*val month = Calendar.getInstance().get(Calendar.MONTH)+1
        val year = Calendar.getInstance().get(Calendar.YEAR)
        recordViewModel.month.value = month
        recordViewModel.year.value = year
        recordViewModel.recordType.value = recordType*/

//        val monthName = Month.values()[month-1].value
        binding.filterLayoutRecordsFragment.apply {
//            monthAndYearTv.text = "$monthName $year"
            spinner.visibility = View.GONE
            recordTypeTv.apply {
                text = recordType
                visibility = View.VISIBLE
            }
        }

        recordViewModel.fetchAllRecords(userId)
        recordViewModel.allRecords.observe(viewLifecycleOwner, Observer {
            recordViewModel.fetchRecords()
        })

        recordViewModel.month.observe(viewLifecycleOwner, Observer {
            Log.e("month", it.toString())
            recordViewModel.fetchRecords()
        })

        recordViewModel.year.observe(viewLifecycleOwner, Observer {
            Log.e("year", it.toString())
            recordViewModel.fetchRecords()
        })

        recordViewModel.recordType.observe(viewLifecycleOwner, Observer {
            Log.e("recordType", it.toString())
            recordViewModel.fetchRecords()
        })

        recordViewModel.filteredRecords.observe(viewLifecycleOwner, Observer {
            Log.e("filtered 1", "")
            if(it != null) {
                Log.e("filtered 2", "")
                it.forEach { r ->
                    Log.e("PieChart", r.toString())
                }
                recordViewModel.transformedDataForPieChart(it)
                recordViewModel.isDataForPieChartUpdated.observe(viewLifecycleOwner, Observer{ flag ->
                    if(flag != null) {
                        if(flag) {
                            recordViewModel.dataForPieChart.value?.let { data ->
                                if(data.isNotEmpty()) {
                                    binding.emptyRecordsList.emptyDataLinearRoot.visibility = View.GONE
                                    binding.emptyScrollView.visibility = View.GONE
                                    binding.pieChart.visibility = View.VISIBLE
                                    binding.statsRecyclerView.visibility = View.VISIBLE
                                    drawPieChart(data)
                                    setStatisticsRecyclerView(data)
                                } else {
                                    binding.emptyRecordsList.emptyDataLinearRoot.visibility = View.VISIBLE
                                    binding.emptyRecordsList.emptyDataImage.setImageResource(R.drawable.stats)
                                    binding.emptyRecordsList.emptyDataText.text = "No Stats Found"
                                    binding.emptyScrollView.visibility = View.VISIBLE
                                    binding.pieChart.visibility = View.GONE
                                    binding.statsRecyclerView.visibility = View.GONE
                                }
                            }

                        }
                        recordViewModel.isDataForPieChartUpdated.value = null
                    }
                })
                /*if(it.isNotEmpty()) {
                    binding.pieChart.visibility = View.VISIBLE
                    binding.statsRecyclerView.visibility = View.VISIBLE
                    binding.emptyRecordsList.visibility = View.GONE

                } else {
                    binding.pieChart.visibility = View.GONE
                    binding.statsRecyclerView.visibility = View.GONE
                    binding.emptyRecordsList.visibility = View.VISIBLE
                }*/
            }
        })
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
        binding.statsRecyclerView.layoutManager = GridLayoutManager(this.context, spanCount)//LinearLayoutManager(requireContext())
        adapter.onItemClick = {
            recordViewModel.period = Period.MONTH
            recordViewModel.category.value = it.first
            //recordViewModel.fetchRecordsOfTheCategory(it.first)
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

        val pieDataSet = PieDataSet(data, "$recordType Stats")

        pieDataSet.colors = sliceColors
        pieDataSet.valueTextSize = 1f
        pieDataSet.valueTextColor = Color.BLACK
        pieDataSet.setDrawValues(true)
        pieDataSet.valueFormatter = PercentFormatter(binding.pieChart)

        val pieData = PieData(pieDataSet)


        binding.pieChart.setDrawEntryLabels(false)
        binding.pieChart.setUsePercentValues(true)
//        binding.pieChart.description.textAlign = Paint.Align.RIGHT
        binding.pieChart.data = pieData
        binding.pieChart.description.text = ""
//        binding.pieChart.description.textSize = 25f
        binding.pieChart.centerText = """$recordType
            |₹ ${Helper.formatNumberToIndianStyle(total)}
        """.trimMargin()
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

        //val formattedValue = dataSet.valueFormatter.getFormattedValue(entry.y, entry, i, pieChart.viewPortHandler)
        binding.pieChart.centerText = """${(e as  PieEntry).label}
            |₹ ${Helper.formatNumberToIndianStyle((e as PieEntry).value)}
        """.trimMargin()
    }

    override fun onNothingSelected() {
        binding.pieChart.centerText = recordType
    }

    override fun onPause() {

        super.onPause()
        Log.e("Animation", "on pause statistics fragment")
        Log.e("Scroll", "onPause")
        restoreScrollPositionViewModel.updateDashboardScrollPosition(binding.scrollViewStats.scrollY)
    }

    override fun onStop() {
        super.onStop()
        Log.e("Animation", "on stop statistics fragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e("Animation", "on destroy view statistics fragment")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("Animation", "on destroy statistics fragment")
        Log.e("Landscape", "statsfrag onDestroy")
        /*filterView?.clear()
        filterView = null*/
    }

}