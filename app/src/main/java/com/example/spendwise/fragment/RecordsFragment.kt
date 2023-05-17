package com.example.spendwise.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spendwise.Categories
import com.example.spendwise.Helper
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.adapter.RecordRecyclerViewAdapter
import com.example.spendwise.databinding.FragmentRecordsBinding
import com.example.spendwise.enums.RecordType
import com.example.spendwise.interfaces.FilterViewDelegate
import com.example.spendwise.viewmodel.RecordViewModel
import com.example.spendwise.viewmodel.RestoreScrollPositionViewModel
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory
import com.example.spendwise.viewmodelfactory.RestoreScrollPositionViewModelFactory
import java.util.*


class RecordsFragment : Fragment(), FilterViewDelegate {

    private lateinit var binding: FragmentRecordsBinding
    private lateinit var recordViewModel: RecordViewModel
    private lateinit var adapter: RecordRecyclerViewAdapter
    private lateinit var restoreScrollPositionViewModel: RestoreScrollPositionViewModel
    private var filterView: FilterView? = null
    private lateinit var args: RecordsFragmentArgs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recordViewModelFactory = RecordViewModelFactory(requireActivity().application)
        recordViewModel = ViewModelProvider(requireActivity(), recordViewModelFactory)[RecordViewModel::class.java]
        val restoreScrollPositionFactory = RestoreScrollPositionViewModelFactory(requireActivity().application)
        restoreScrollPositionViewModel = ViewModelProvider(this, restoreScrollPositionFactory)[RestoreScrollPositionViewModel::class.java]
        args = RecordsFragmentArgs.fromBundle(requireArguments())

        if(savedInstanceState == null) {
            val userId = (activity as MainActivity).getSharedPreferences(resources.getString(R.string.loginStatus), Context.MODE_PRIVATE).getInt(resources.getString(R.string.userId), 0)
            recordViewModel.fetchAllRecords(userId)
            if((args.hideDescriptionText) && args.selectedCategory == null) {
                val month = Calendar.getInstance().get(Calendar.MONTH)+1
                val year = Calendar.getInstance().get(Calendar.YEAR)
                recordViewModel.month.value = month
                recordViewModel.year.value = year
                recordViewModel.recordType.value = RecordType.ALL.value
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecordsBinding.inflate(inflater, container, false)

        args.selectedCategory?.let {
            binding.toolbarTitle.apply {
                text = resources.getString(R.string.two_strings_concate, it.title, resources.getString(R.string.records))
            }
        } ?: binding.toolbarTitle.apply { text = resources.getString(R.string.records) }
        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.searchIcon.setOnClickListener {
            binding.searchIcon.visibility = View.GONE
            binding.toolbarTitle.visibility = View.GONE
            binding.back.visibility = View.GONE
            binding.searchViewBackBtn.visibility = View.VISIBLE
            binding.recordsSearchView.visibility = View.VISIBLE
            binding.recordsSearchView.onActionViewExpanded()
            binding.recordsSearchView.isFocusable = true
        }

        if(recordViewModel.queryText.isNotEmpty()) {
            binding.searchIcon.visibility = View.GONE
            binding.toolbarTitle.visibility = View.GONE
            binding.back.visibility = View.GONE
            binding.searchViewBackBtn.visibility = View.VISIBLE
            binding.recordsSearchView.visibility = View.VISIBLE
            binding.recordsSearchView.isIconified = false
            binding.recordsSearchView.onActionViewExpanded()
            binding.recordsSearchView.setQuery(recordViewModel.queryText, false)
            binding.recordsSearchView.isFocusable = true
            recordViewModel.searchedList.value = recordViewModel.filteredRecords.value?.filter {
                it.category.lowercase().contains(recordViewModel.queryText)||it.note.lowercase().contains(recordViewModel.queryText)
            }
        }

        val closeButton = binding.recordsSearchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        closeButton.setOnClickListener {
            if(binding.recordsSearchView.query.toString() == "" || binding.recordsSearchView.query == null) {
                binding.recordsSearchView.onActionViewCollapsed()
                binding.recordsSearchView.isIconified = true
                binding.recordsSearchView.clearFocus()
                binding.searchIcon.visibility = View.VISIBLE
                binding.toolbarTitle.visibility = View.VISIBLE
                binding.back.visibility = View.VISIBLE
                binding.searchViewBackBtn.visibility = View.GONE
                binding.recordsSearchView.visibility = View.GONE
            }
            recordViewModel.queryText = ""
            binding.recordsSearchView.setQuery("", false)
            binding.recordsSearchView.isFocusable = true
            binding.recordsSearchView.isFocused
            recordViewModel.searchedList.value = recordViewModel.filteredRecords.value
        }

        binding.searchViewBackBtn.setOnClickListener {
            binding.recordsSearchView.onActionViewCollapsed()
            recordViewModel.queryText = ""
            binding.recordsSearchView.isIconified = true
            binding.recordsSearchView.clearFocus()
            binding.searchIcon.visibility = View.VISIBLE
            binding.toolbarTitle.visibility = View.VISIBLE
            binding.back.visibility = View.VISIBLE
            binding.searchViewBackBtn.visibility = View.GONE
            binding.recordsSearchView.visibility = View.GONE
        }

        binding.recordsSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                recordViewModel.newQueryText = newText ?: ""
                filterRecords(newText)
                return false
            }
        })

        filterView = FilterView(recordViewModel, binding.filterLayoutRecordsFragment, this, resources)
        if(args.hideAmountView) binding.recordsConstraint.visibility = View.GONE else binding.recordsConstraint.visibility = View.VISIBLE
        if(args.hideFilterView) binding.filterLayoutRecordsFragment.filterLayoutLinear.visibility = View.GONE else
            binding.filterLayoutRecordsFragment.filterLayoutLinear.visibility = View.VISIBLE
        return binding.root
    }

    private fun filterRecords(newText: String?) {
        if(newText?.isNotEmpty() == true){
            recordViewModel.searchedList.value = recordViewModel.filteredRecords.value?.filter {
                it.category.lowercase().contains(newText.lowercase()) || it.note.lowercase().contains(newText.lowercase())
            }
            recordViewModel.queryText = newText
        }else{
            if (newText != null) {
                recordViewModel.queryText = newText
            } else recordViewModel.queryText = ""
            recordViewModel.searchedList.value = recordViewModel.filteredRecords.value

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recordsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    restoreScrollPositionViewModel.scrollPositionRecords = (binding.recordsRecyclerView.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
                }
            }
        })

        if(savedInstanceState == null) {
            recordViewModel.allRecords.observe(viewLifecycleOwner) {
                recordViewModel.fetchRecords()
                args.selectedCategory?.let { category ->
                    recordViewModel.fetchRecordsOfTheCategory(category)
                }
            }
            recordViewModel.filteredRecords.observe(viewLifecycleOwner) {
                if (it != null) {
                    recordViewModel.searchedList.value = it
                }
            }
        }

        if(!(args.hideDescriptionText) && args.selectedCategory != null) {
            recordViewModel.recordsOfTheCategory.observe(viewLifecycleOwner) {
                if (it != null) {
                    recordViewModel.updateFilteredRecords(it)
                }
            }


        } else {

            filterView?.setMonthYearValue()
            binding.filterLayoutRecordsFragment.monthAndYearTv.setOnClickListener {
                filterView?.showMonthYearPicker(requireContext())
            }
            filterView?.createAndSetAdapter(requireContext(), R.array.recordTypes)
            recordViewModel.allRecords.observe(viewLifecycleOwner) {
                recordViewModel.fetchRecords()
            }

            recordViewModel.month.observe(viewLifecycleOwner) {
                recordViewModel.fetchRecords()
                recordViewModel.filteredRecords.observe(viewLifecycleOwner) {
                    if (it != null) {
                        filterRecords(recordViewModel.newQueryText) // here is the problem
                    }
                }


            }

            recordViewModel.year.observe(viewLifecycleOwner) {
                recordViewModel.fetchRecords()
                recordViewModel.filteredRecords.observe(viewLifecycleOwner) {
                    if (it != null) {
                        filterRecords(recordViewModel.newQueryText) // here is the problem
                    }
                }
            }

            recordViewModel.recordType.observe(viewLifecycleOwner) {
                if (recordViewModel.recordType.value == RecordType.ALL.value) {
                    binding.recordsConstraint.visibility = View.VISIBLE
                    binding.recordsLinearIncomeExpense.visibility = View.GONE
                } else {
                    binding.recordsConstraint.visibility = View.GONE
                    binding.recordsLinearIncomeExpense.visibility = View.VISIBLE
                }
                recordViewModel.fetchRecords()
                recordViewModel.filteredRecords.observe(viewLifecycleOwner) {
                    if (it != null) {
                        filterRecords(recordViewModel.newQueryText) // here is the problem
                    }
                }
            }
        }

        if(args.hideAmountView) {
            binding.recordsConstraint.visibility = View.GONE
        } else {
            binding.recordsConstraint.visibility = View.VISIBLE
        }

        recordViewModel.searchedList.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.isEmpty()) {
                    binding.scrollViewEmptyDataRecords.visibility = View.VISIBLE
                    binding.emptyRecordsList.emptyDataImage.setImageResource(R.drawable.records)
                    binding.emptyRecordsList.emptyDataText.text = resources.getString(R.string.no_records_found)
                    binding.recordsRecyclerView.visibility = View.GONE
                    recordViewModel.getIncomeOfTheMonth()
                    recordViewModel.getExpenseOfTheMonth()
                    recordViewModel.getTotalBalanceOfTheMonth()
                } else {
                    binding.scrollViewEmptyDataRecords.visibility = View.GONE
                    binding.recordsRecyclerView.visibility = View.VISIBLE
                    recordViewModel.getIncomeOfTheMonth()
                    recordViewModel.getExpenseOfTheMonth()
                    recordViewModel.getTotalBalanceOfTheMonth()
                    adapter = RecordRecyclerViewAdapter(it, Categories.categoryList)
                    adapter.setTheFragment(this)
                    binding.recordsRecyclerView.adapter = adapter
                    val spanCount =
                        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
                            resources.configuration.screenWidthDp >= 600
                        ) {
                            2
                        } else {
                            1
                        }
                    val layoutManager = GridLayoutManager(
                        this.context,
                        spanCount
                    )
                    binding.recordsRecyclerView.layoutManager = layoutManager
                    (binding.recordsRecyclerView.layoutManager as GridLayoutManager).scrollToPosition(
                        restoreScrollPositionViewModel.scrollPositionRecords
                    )
                    adapter.onItemClick = { record ->
                        recordViewModel.setSelectedRecord(record)
                        moveToNextFragment()
                    }
                }
            }

        }

        recordViewModel.incomeOfTheMonth.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.incomeAmountTV.text = Helper.formatNumberToIndianStyle(it)
                if (recordViewModel.recordType.value == RecordType.INCOME.value) {
                    binding.currentMonthIncomeExpenseTvLinear.apply {
                        text = resources.getString(R.string.income_label)
                    }
                    binding.incomeExpenseAmountTVLinear.text =
                        Helper.formatNumberToIndianStyle(it)
                    binding.incomeExpenseAmountTVLinear.setTextColor(resources.getColor(R.color.incomeColour))
                }
            }
        }
        recordViewModel.expenseOfTheMonth.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.expenseAmountTV.text = Helper.formatNumberToIndianStyle(it)
                if (recordViewModel.recordType.value == RecordType.EXPENSE.value) {
                    binding.currentMonthIncomeExpenseTvLinear.apply {
                        text = resources.getString(R.string.expense_label)
                    }
                    binding.incomeExpenseAmountTVLinear.text =
                        Helper.formatNumberToIndianStyle(it)
                    binding.incomeExpenseAmountTVLinear.setTextColor(resources.getColor(R.color.expenseColour))
                }
            }
        }
        recordViewModel.totalBalanceOfTheMonth.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.totalAmountTV.text = Helper.formatNumberToIndianStyle(it)
            }
        }

        binding.fabRecordsPage.setOnClickListener {
            if(recordViewModel.recordType.value == RecordType.ALL.value) {
                if(recordViewModel.isAllFabVisible) {
                    binding.backScreenHome.visibility = View.GONE
                    binding.addIncomeFabHome.visibility = View.GONE
                    binding.addExpenseFabHome.visibility = View.GONE
                    recordViewModel.isAllFabVisible = false
                    recordViewModel.isFabRotated = false
                    binding.fabRecordsPage.animate().rotationBy(-45F)
                } else {
                    binding.backScreenHome.visibility = View.VISIBLE
                    binding.addIncomeFabHome.visibility = View.VISIBLE
                    binding.addExpenseFabHome.visibility = View.VISIBLE
                    recordViewModel.isAllFabVisible = true
                    recordViewModel.isFabRotated = true
                    binding.fabRecordsPage.animate().rotationBy(45F)
                }
            } else if(recordViewModel.recordType.value == RecordType.INCOME.value) {
                val action = RecordsFragmentDirections.actionRecordsFragmentToAddRecordFragment(isEditRecord = false, recordType = RecordType.INCOME.value)
                findNavController().navigate(action)
            } else if(recordViewModel.recordType.value == RecordType.EXPENSE.value) {
                val action = RecordsFragmentDirections.actionRecordsFragmentToAddRecordFragment(isEditRecord = false, recordType = RecordType.EXPENSE.value)
                findNavController().navigate(action)
            }
        }

        binding.totalAmountTV.setOnClickListener {
            if(it != null && binding.totalAmountTV.text.toString().isNotEmpty()) {
                showDialog(resources.getString(R.string.netBalance), resources.getString(R.string.amount_format, binding.totalAmountTV.text)) // "₹ ${binding.totalAmountTV.text}"
            }
        }

        binding.expenseAmountTV.setOnClickListener {
            if(it != null && binding.expenseAmountTV.text.toString().isNotEmpty()) {
                showDialog(resources.getString(R.string.expense_label), resources.getString(R.string.amount_format,binding.expenseAmountTV.text))
            }
        }

        binding.incomeAmountTV.setOnClickListener {
            if(it != null && binding.incomeAmountTV.text.toString().isNotEmpty()) {
                showDialog(resources.getString(R.string.income_label), resources.getString(R.string.amount_format, binding.incomeAmountTV.text))
            }
        }

        binding.addIncomeFabHome.setOnClickListener {
            recordViewModel.isAllFabVisible = false
            recordViewModel.isFabRotated = true
            val action = RecordsFragmentDirections.actionRecordsFragmentToAddRecordFragment(isEditRecord = false, recordType = RecordType.INCOME.value)
            findNavController().navigate(action)
        }
        binding.addExpenseFabHome.setOnClickListener {
            recordViewModel.isAllFabVisible = false
            recordViewModel.isFabRotated = true
            val action = RecordsFragmentDirections.actionRecordsFragmentToAddRecordFragment(isEditRecord = false, recordType = RecordType.EXPENSE.value)
            findNavController().navigate(action)
        }


        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true){
                override fun handleOnBackPressed() {
                    if(recordViewModel.isAllFabVisible) {
                        binding.backScreenHome.visibility = View.GONE
                        binding.addIncomeFabHome.visibility = View.GONE
                        binding.addExpenseFabHome.visibility = View.GONE
                        recordViewModel.isAllFabVisible = false
                        recordViewModel.isFabRotated = false
                        binding.fabRecordsPage.animate().rotationBy(-45F)
                    } else {
                        recordViewModel.queryText = ""
                        recordViewModel.newQueryText = ""
                        findNavController().popBackStack()
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun intimateSelectedDate(month: Int, year: Int) {
        recordViewModel.month.value = month
        recordViewModel.year.value = year
    }

    override fun intimateSelectedRecordType(recordType: String) {
        recordViewModel.recordType.value = recordType
    }

    override fun onDestroy() {
        super.onDestroy()
        filterView?.clear()
        filterView = null

    }

    private fun moveToNextFragment() {
        findNavController().navigate(R.id.action_recordsFragment_to_viewRecordFragment)
    }

    override fun onResume() {
        super.onResume()

        if(recordViewModel.isAllFabVisible) {
            binding.backScreenHome.visibility = View.VISIBLE
            binding.addIncomeFabHome.visibility = View.VISIBLE
            binding.addExpenseFabHome.visibility = View.VISIBLE
            recordViewModel.isAllFabVisible = true
            if(recordViewModel.isFabRotated) {
                Log.e("App", "isAllFab true")
                binding.fabRecordsPage.animate().rotationBy(45F)
            }
        } else {
            binding.backScreenHome.visibility = View.GONE
            binding.addIncomeFabHome.visibility = View.GONE
            binding.addExpenseFabHome.visibility = View.GONE
            recordViewModel.isAllFabVisible = false
            if(recordViewModel.isFabRotated) {
                Log.e("App", "isAllFab false")
               // binding.fabRecordsPage.animate().rotationBy(-45F)
            }
        }

        if(binding.recordsSearchView.hasFocus()) {
            binding.searchIcon.visibility = View.GONE
            binding.toolbarTitle.visibility = View.GONE
            binding.back.visibility = View.GONE
            binding.searchViewBackBtn.visibility = View.VISIBLE
            binding.recordsSearchView.visibility = View.VISIBLE
            binding.recordsSearchView.isIconified = false
            binding.recordsSearchView.onActionViewExpanded()
            binding.recordsSearchView.setQuery(recordViewModel.queryText, false)
            binding.recordsSearchView.isFocusable = true
            recordViewModel.searchedList.value = recordViewModel.filteredRecords.value?.filter {
                it.category.lowercase().contains(recordViewModel.queryText, ignoreCase = true)||it.note.lowercase().contains(recordViewModel.queryText, ignoreCase = true)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        recordViewModel.isSearchViewFocused = binding.recordsSearchView.hasFocus()
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