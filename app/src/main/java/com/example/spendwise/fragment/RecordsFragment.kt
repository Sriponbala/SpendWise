package com.example.spendwise.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spendwise.Categories
import com.example.spendwise.Helper
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.adapter.RecordRecyclerViewAdapter
import com.example.spendwise.databinding.FragmentRecordsBinding
import com.example.spendwise.enums.Month
import com.example.spendwise.enums.RecordType
import com.example.spendwise.interfaces.FilterViewDelegate
import com.example.spendwise.viewmodel.RecordViewModel
import com.example.spendwise.viewmodel.RestoreScrollPositionViewModel
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory
import com.example.spendwise.viewmodelfactory.RestoreScrollPositionViewModelFactory
import java.util.*


class RecordsFragment : Fragment(), FilterViewDelegate { //, AdapterView.OnItemSelectedListener {

    private lateinit var binding: FragmentRecordsBinding
    private lateinit var recordViewModel: RecordViewModel
    private lateinit var adapter: RecordRecyclerViewAdapter
    private lateinit var restoreScrollPositionViewModel: RestoreScrollPositionViewModel
    private var filterView: FilterView? = null  // FilterView(this, requireActivity().application)
    private lateinit var args: RecordsFragmentArgs

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*val userViewModelFactory = UserViewModelFactory((activity as MainActivity).application)
        userViewModel = ViewModelProvider(requireActivity(), userViewModelFactory)[UserViewModel::class.java]
        Log.e("UserViewModel Records", userViewModel.toString())

        Log.e("User Record", userViewModel.user.value?.userId.toString())*/
        val recordViewModelFactory = RecordViewModelFactory(requireActivity().application)
        recordViewModel = ViewModelProvider(requireActivity(), recordViewModelFactory)[RecordViewModel::class.java].also {
        }
        val restoreScrollPositionFactory = RestoreScrollPositionViewModelFactory(requireActivity().application)
        restoreScrollPositionViewModel = ViewModelProvider(this, restoreScrollPositionFactory)[RestoreScrollPositionViewModel::class.java]
        /*userViewModel.user.value?.userId?.let {
            Log.e("UserId Record", it.toString())
            recordViewModel.userId = it
        }*/
        args = RecordsFragmentArgs.fromBundle(requireArguments())
        if(savedInstanceState == null) {
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
        /*(activity as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back_arrow)
        }*/
        binding = FragmentRecordsBinding.inflate(inflater, container, false)
        args.selectedCategory?.let {
            binding.toolbarRecords.apply {
                title = "${it.title} Records"
            }
        } ?: binding.toolbarRecords.apply { title = "Records" }

        binding.toolbarRecords.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.toolbarRecords.setOnMenuItemClickListener {
            false
        }

        if(recordViewModel.queryText.isNotEmpty()) {
            binding.recordsSearchView.isIconified = false
//            val titleTextView: TextView = binding.toolbarRecords.findViewById(com.google.android.material.R.id.action_bar_title)
//            titleTextView.visibility = View.GONE
            binding.recordsSearchView.onActionViewExpanded()
            binding.recordsSearchView.setQuery(recordViewModel.queryText, false)
            binding.recordsSearchView.isFocusable = true
            recordViewModel.searchedList.value = recordViewModel.filteredRecords.value?.filter {
                it.category.lowercase().contains(recordViewModel.queryText)||it.note.lowercase().contains(recordViewModel.queryText)
            }
//            filterCategories(categoryViewModel.queryText)
        }

        binding.recordsSearchView.setOnCloseListener {
            recordViewModel.queryText = ""
            binding.recordsSearchView.clearFocus()
            if(binding.recordsSearchView.query.toString() == "" || binding.recordsSearchView.query == null) {
                binding.recordsSearchView.onActionViewCollapsed()
            }
            recordViewModel.searchedList.value = recordViewModel.filteredRecords.value.also{
            }
            true
        }

        /*binding.recordsSearchView.setOnClickListener {
            binding.recordsSearchView.isFocusable = true
        }

        binding.recordsSearchView.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                Log.e("Coroutine", "hasFocus $hasFocus")

            } else {
                binding.recordsSearchView.setOnQueryTextListener(null)
            }
        }*/
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

        filterView = FilterView(recordViewModel, binding.filterLayoutRecordsFragment, this)
        if(args.hideAmountView) binding.recordsConstraint.visibility = View.GONE else binding.recordsConstraint.visibility = View.VISIBLE
        if(args.hideFilterView) binding.filterLayoutRecordsFragment.filterLayoutLinear.visibility = View.GONE else
            binding.filterLayoutRecordsFragment.filterLayoutLinear.visibility = View.VISIBLE
        return binding.root
    }

    private fun filterRecords(newText: String?) {
        if(newText?.isNotEmpty() == true){
            recordViewModel.searchedList.value = recordViewModel.filteredRecords.value?.filter {
                it.category.lowercase().contains(newText.lowercase()) || it.note.lowercase().contains(newText.lowercase())
            }.also {
            }
            recordViewModel.queryText = newText
        }else{
            if (newText != null) {
                recordViewModel.queryText = newText
            }
//            recordViewModel.filteredCategoryList.value = Categories.categoryList.filter { it.recordType.value == recordType }
            recordViewModel.searchedList.value = recordViewModel.filteredRecords.value.also {
            } //Categories.categoryList.filter { it.recordType.value == recordType }

        }

        // Notify adapter with filtered list
        /*categoryViewModel.searchedList.value?.let {
            Log.e("Category", "above adapter $it")
            adapter.filterList(it)
        }*/
//        adapter.filterList(categoryViewModel.searchedList.value)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recordsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    restoreScrollPositionViewModel.scrollPositionRecords = (binding.recordsRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                }
            }
        })

        val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
        /*Log.e("userId records", userId.toString())
        Log.e("Record", "args.card outside if ${args.hideAmountView}")
        Log.e("Record", "args.filter outside if ${args.hideFilterView}")
        Log.e("Record", "args.category outside if ${args.selectedCategory}")
        Log.e("Record", "args.desc outside if ${args.hideDescriptionText}")*/

//        recordViewModel.fetchAllRecords(userId)
        /*recordViewModel.allRecords.observe(viewLifecycleOwner, Observer {
            Log.e("Record", "allRecords")
            recordViewModel.fetchRecords()
            args.selectedCategory?.let {
                Log.e("Record", "arg cat in let ${args.selectedCategory}")
                recordViewModel.fetchRecordsOfTheCategory(it)
            }
        })*/

        if(savedInstanceState == null) {
//            Log.e("Records search", "saved instance bundle not null")
            //recordViewModel.fetchAllRecords(userId)
            recordViewModel.allRecords.observe(viewLifecycleOwner, Observer {
//                Log.e("Record", "allRecords")
//                Log.e("Coroutine", "allRecords rec frag savedinstancestate == null")
                recordViewModel.fetchRecords()
                args.selectedCategory?.let {
//                    Log.e("Record", "arg cat in let ${args.selectedCategory}")
                    recordViewModel.fetchRecordsOfTheCategory(it)
                }
            })
            recordViewModel.filteredRecords.observe(viewLifecycleOwner, Observer {
                if(it != null) {
                    recordViewModel.searchedList.value = it
                }
            })
        } //else Log.e("Search", "saved instance bundle null")

        if(!(args.hideDescriptionText) && args.selectedCategory != null) {
//            Log.e("Record", "arg cat ${args.selectedCategory}")
//            recordViewModel.fetchAllRecords(userId)
            /*recordViewModel.allRecords.observe(viewLifecycleOwner, Observer {
                Log.e("Record", "allRecords")
                recordViewModel.fetchRecords()
                args.selectedCategory?.let {
                    Log.e("Record", "arg cat in let ${args.selectedCategory}")
                    recordViewModel.fetchRecordsOfTheCategory(it)
                }
            })*/
            recordViewModel.recordsOfTheCategory.observe(viewLifecycleOwner, Observer {
                if(it != null) {
                    recordViewModel.updateFilteredRecords(it)
//                    Log.e("Record", "observe record category")
                }
            })


        } else {
//            recordViewModel.fetchAllRecords(userId)
            /*val month = Calendar.getInstance().get(Calendar.MONTH)+1
            val year = Calendar.getInstance().get(Calendar.YEAR)
            recordViewModel.month.value = month
            recordViewModel.year.value = year
            recordViewModel.recordType.value = RecordType.ALL.value*/

           // val monthName = Month.values()[month-1].value
           // binding.filterLayoutRecordsFragment.monthAndYearTv.text = "$monthName $year".also(::println)

            filterView?.setMonthYearValue()
            binding.filterLayoutRecordsFragment.monthAndYearTv.setOnClickListener {
                filterView?.showMonthYearPicker(requireContext())
            }
            filterView?.createAndSetAdapter(requireContext(), com.example.spendwise.R.array.recordTypes)
            recordViewModel.allRecords.observe(viewLifecycleOwner, Observer {
//                Log.e("Record", "allRecords")
//                Log.e("Coroutine", "allRecords rec frag")
                recordViewModel.fetchRecords()
            })

            recordViewModel.month.observe(viewLifecycleOwner, Observer {
//                Log.e("Coroutine", "allRecords rec frag month")
//                Log.e("Record", "month")
                recordViewModel.fetchRecords()
                /*binding.recordsSearchView.setOnFocusChangeListener { v, hasFocus ->
                    if(hasFocus) {
                        recordViewModel.filteredRecords.observe(viewLifecycleOwner, Observer {
                            if(it != null) {
                                filterRecords(recordViewModel.newQueryText) // here is the problem
                            }
                        })
                    }
                }*/
                recordViewModel.filteredRecords.observe(viewLifecycleOwner, Observer {
                    if(it != null) {
                        filterRecords(recordViewModel.newQueryText) // here is the problem
                    }
                })


            })

            recordViewModel.year.observe(viewLifecycleOwner, Observer {
//                Log.e("Record", "year")
//                Log.e("Coroutine", "allRecords rec frag year")
                recordViewModel.fetchRecords()
/*
                binding.recordsSearchView.setOnFocusChangeListener { v, hasFocus ->
                    if(hasFocus) {
                        recordViewModel.filteredRecords.observe(viewLifecycleOwner, Observer {
                            if(it != null) {
                                filterRecords(recordViewModel.newQueryText) // here is the problem
                            }
                        })
                    }
                }
*/
                recordViewModel.filteredRecords.observe(viewLifecycleOwner, Observer {
                    if(it != null) {
                        filterRecords(recordViewModel.newQueryText) // here is the problem
                    }
                })
            })

            recordViewModel.recordType.observe(viewLifecycleOwner, Observer {
//                Log.e("Record", "recordType")
//                Log.e("Coroutine", "allRecords rec frag record type")
                recordViewModel.fetchRecords()
                /*binding.recordsSearchView.setOnFocusChangeListener { v, hasFocus ->
                    if(hasFocus) {
                        recordViewModel.filteredRecords.observe(viewLifecycleOwner, Observer {
                            if(it != null) {
                                filterRecords(recordViewModel.newQueryText) // here is the problem
                            }
                        })
                    }
                }*/
                recordViewModel.filteredRecords.observe(viewLifecycleOwner, Observer {
                    if(it != null) {
                        filterRecords(recordViewModel.newQueryText) // here is the problem
                    }
                })
                /*recordViewModel.filteredRecords.observe(viewLifecycleOwner, Observer {
                    if(it != null) {
                        filterRecords(recordViewModel.newQueryText) // here is the problem
                    }
                })*/
            })
        }

        /*val arrayAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.recordTypes, android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.filterLayoutRecordsFragment.spinner.adapter = adapter
        }
        binding.filterLayoutRecordsFragment.spinner.onItemSelectedListener = filterView//this*/
        /*userViewModel.user.value?.userId?.let {
            recordViewModel.fetchAllRecords(it)
        }*/

       /* recordViewModel.records.observe(viewLifecycleOwner, Observer {
            recordViewModel.fetchRecords(month, year)
        })*/
//        recordViewModel.allRecords.observe(viewLifecycleOwner, Observer {
//            Log.e("Record", "allRecords")
//            recordViewModel.fetchRecords()
//        })
//
//        recordViewModel.month.observe(viewLifecycleOwner, Observer {
//            Log.e("Record", "month")
//            recordViewModel.fetchRecords()
//        })
//
//        recordViewModel.year.observe(viewLifecycleOwner, Observer {
//            Log.e("Record", "year")
//            recordViewModel.fetchRecords()
//        })
//
//        recordViewModel.recordType.observe(viewLifecycleOwner, Observer {
//            Log.e("Record", "recordType")
//            recordViewModel.fetchRecords()
//        })

        if(args.hideAmountView) {
            binding.recordsConstraint.visibility = View.GONE
        } else {
            binding.recordsConstraint.visibility = View.VISIBLE
        }

        recordViewModel.searchedList.observe(viewLifecycleOwner, Observer {
//            Log.e("Coroutine", "searchlist obs $it")
//            Log.e("Record", "filtered Records")
            if(it != null) {
                /*for(i in it) {
                    Log.e("Filtered Record", i.toString())
                }*/
                if(it.isEmpty()) {
//                    Log.e("Record", "empty")
                    binding.emptyRecordsList.visibility = View.VISIBLE
//                    binding.cardView.visibility = View.GONE
                    binding.recordsRecyclerView.visibility = View.GONE
                    recordViewModel.getIncomeOfTheMonth()
                    recordViewModel.getExpenseOfTheMonth()
                    recordViewModel.getTotalBalanceOfTheMonth()
                } else {
//                    Log.e("Record", "not empty")
                    binding.emptyRecordsList.visibility = View.GONE
//                    binding.cardView.visibility = View.VISIBLE
                    binding.recordsRecyclerView.visibility = View.VISIBLE
                    recordViewModel.getIncomeOfTheMonth()
                    recordViewModel.getExpenseOfTheMonth()
                    recordViewModel.getTotalBalanceOfTheMonth()
                    adapter = RecordRecyclerViewAdapter(it, Categories.categoryList)
                    adapter.setTheFragment(this)
                    binding.recordsRecyclerView.adapter = adapter
                    val layoutManager = LinearLayoutManager(this.context)
                    binding.recordsRecyclerView.layoutManager = layoutManager
                    (binding.recordsRecyclerView.layoutManager as LinearLayoutManager).scrollToPosition(restoreScrollPositionViewModel.scrollPositionRecords)
                    adapter.onItemClick = { record ->
                        recordViewModel.setSelectedRecord(record)
                        moveToNextFragment()
                    }
                }
            } //else Log.e("Error", "Null")

        })

        recordViewModel.incomeOfTheMonth.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                binding.incomeAmountTV.text = "${Helper.formatNumberToIndianStyle(it)}"
            }
        })
        recordViewModel.expenseOfTheMonth.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                binding.expenseAmountTV.text = "${Helper.formatNumberToIndianStyle(it)}"
//                binding.expenseAmountTV.text = "${resources.getString(R.string.rupee_symbol)} ${Helper.formatNumberToIndianStyle(it)}"
            }
        })
        recordViewModel.totalBalanceOfTheMonth.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                binding.totalAmountTV.text = "${Helper.formatNumberToIndianStyle(it)}"
            }
        })

        binding.fabRecordsPage.setOnClickListener {
            val action = RecordsFragmentDirections.actionRecordsFragmentToAddRecordFragment(isEditRecord = false)
            findNavController().navigate(action)
        }
    }

    fun showMonthYearPicker() {
        val dialogView = LayoutInflater.from(context).inflate(com.example.spendwise.R.layout.month_and_year_picker, null)
        val monthPicker = dialogView.findViewById<NumberPicker>(com.example.spendwise.R.id.month_picker)
        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.value = Calendar.getInstance().get(Calendar.MONTH) + 1

        val yearPicker = dialogView.findViewById<NumberPicker>(com.example.spendwise.R.id.year_picker)
        yearPicker.minValue = 1900
        yearPicker.maxValue = 2100
        yearPicker.value = Calendar.getInstance().get(Calendar.YEAR)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val month = monthPicker.value
                val year = yearPicker.value
                val monthName = Month.values()[month-1].value
                binding.filterLayoutRecordsFragment.monthAndYearTv.text = "$monthName $year"
                recordViewModel.month.value = month
                recordViewModel.year.value = year
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    override fun intimateSelectedDate(month: Int, year: Int) {
        recordViewModel.month.value = month
        recordViewModel.year.value = year
    }

    override fun intimateSelectedRecordType(recordType: String) {
        when(recordType) {
            RecordType.INCOME.value -> {
               /* binding.linearIncome.visibility = View.VISIBLE
                binding.linearExpense.visibility = View.GONE
                binding.linearTotal.visibility = View.GONE*/
            }
            RecordType.EXPENSE.value -> {
                /*binding.linearIncome.visibility = View.GONE
                binding.linearExpense.visibility = View.VISIBLE
                binding.linearTotal.visibility = View.GONE*/
            }
            RecordType.ALL.value -> {
              /*  binding.linearIncome.visibility = View.VISIBLE
                binding.linearExpense.visibility = View.VISIBLE
                binding.linearTotal.visibility = View.VISIBLE*/
            }
        }
        recordViewModel.recordType.value = recordType
    }

    override fun onDestroy() {
        super.onDestroy()
//        Log.e("Landscape", "recordsfrag onDestroy")
        filterView?.clear()
        filterView = null
    }

    /*    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val recordType = parent?.getItemAtPosition(position) as String

        when(recordType) {
            RecordType.INCOME.value -> {
                binding.linearIncome.visibility = View.VISIBLE
                binding.linearExpense.visibility = View.GONE
                binding.linearTotal.visibility = View.GONE
            }
            RecordType.EXPENSE.value -> {
                binding.linearIncome.visibility = View.GONE
                binding.linearExpense.visibility = View.VISIBLE
                binding.linearTotal.visibility = View.GONE
            }
            RecordType.ALL.value -> {
                binding.linearIncome.visibility = View.VISIBLE
                binding.linearExpense.visibility = View.VISIBLE
                binding.linearTotal.visibility = View.VISIBLE
            }
        }
        recordViewModel.recordType.value = recordType
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }*/

    private fun moveToNextFragment() {
        findNavController().navigate(com.example.spendwise.R.id.action_recordsFragment_to_viewRecordFragment)
    }

    override fun onStart() {
        super.onStart()
        Log.e("Animation", "on start records fragment")
    }

    override fun onResume() {
        super.onResume()
//        Log.e("Animation", "on resume records fragment")
    }

    override fun onPause() {
        super.onPause()
//        Log.e("Animation", "on pause records fragment")
    }

    override fun onStop() {
        super.onStop()
//        Log.e("Animation", "on stop records fragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        Log.e("Animation", "on destroy view records fragment")
    }


    /*override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig != null) {
            super.onConfigurationChanged(newConfig)
        }
        mEditTextData = mEditText.getText().tostring() //mEditTextData is a String
        //member variable
        setContentView(com.example.spendwise.R.layout.myLayout)
        initializeViews()
    }

    private fun initializeViews() {
        mEditText = findViewById(com.example.spendwise.R.id.edittext1) as EditText
        mEdiText.setText(mEditTextData)
    }*/

}