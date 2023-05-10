package com.example.spendwise.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spendwise.Categories
import com.example.spendwise.Helper
import com.example.spendwise.R
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
            val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
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
        /*(activity as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back_arrow)
        }*/
        binding = FragmentRecordsBinding.inflate(inflater, container, false)

        Log.e("Coroutine", " search has focus oncreateview- " + binding.recordsSearchView.hasFocus())
        args.selectedCategory?.let {
            binding.toolbarTitle.apply {
                text = "${it.title} Records"
            }
        } ?: binding.toolbarTitle.apply { text = "Records" }
        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        /*args.selectedCategory?.let {
            binding.toolbarRecords.apply {
                title = "${it.title} Records"
            }
        } ?: binding.toolbarRecords.apply { title = "Records" }*/

        /*binding.toolbarRecords.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.toolbarRecords.setOnMenuItemClickListener {
            false
        }*/

        binding.searchIcon.setOnClickListener {
            binding.searchIcon.visibility = View.GONE
            binding.toolbarTitle.visibility = View.GONE
            binding.back.visibility = View.GONE
            binding.searchViewBackBtn.visibility = View.VISIBLE
            binding.recordsSearchView.visibility = View.VISIBLE
            binding.recordsSearchView.onActionViewExpanded()
            binding.recordsSearchView.isFocusable = true
        }

        Log.e("Coroutine", "Query text in createview ${recordViewModel.queryText} ${recordViewModel.queryText.isNotEmpty()}")
        if(recordViewModel.queryText.isNotEmpty()) {
          //  binding.recordsSearchView.isIconified = false
            Log.e("Coroutine", "is not empty - $recordViewModel.queryText.isNotEmpty(), is focused  - ${binding.recordsSearchView.hasFocus()} ")
            binding.searchIcon.visibility = View.GONE
            binding.toolbarTitle.visibility = View.GONE
            binding.back.visibility = View.GONE
            binding.searchViewBackBtn.visibility = View.VISIBLE
            binding.recordsSearchView.visibility = View.VISIBLE
//            val titleTextView: TextView = binding.toolbarRecords.findViewById(com.google.android.material.R.id.action_bar_title)
//            titleTextView.visibility = View.GONE
            binding.recordsSearchView.isIconified = false
            binding.recordsSearchView.onActionViewExpanded()
            Log.e("Coroutine", "Query text in if ${recordViewModel.queryText}")
            binding.recordsSearchView.setQuery(recordViewModel.queryText, false)
            binding.recordsSearchView.isFocusable = true
            recordViewModel.searchedList.value = recordViewModel.filteredRecords.value?.filter {
                it.category.lowercase().contains(recordViewModel.queryText)||it.note.lowercase().contains(recordViewModel.queryText)
            }
//            filterCategories(categoryViewModel.queryText)
        } else {
           /* binding.recordsSearchView.clearFocus()
            binding.searchIcon.visibility = View.VISIBLE
            binding.toolbarTitle.visibility = View.VISIBLE
            binding.back.visibility = View.VISIBLE
            binding.searchViewBackBtn.visibility = View.GONE
            binding.recordsSearchView.visibility = View.GONE
            binding.recordsSearchView.onActionViewCollapsed()
            binding.recordsSearchView.isIconified = true
            hideInputMethod(binding.recordsSearchView)
            Log.e("Coroutine", "Query text in else ${recordViewModel.queryText}")*/
        }

       /* binding.recordsSearchView.setOnQueryTextFocusChangeListener { view, hasFocus ->
            if (!hasFocus && TextUtils.isEmpty(binding.recordsSearchView.query)) {
                binding.recordsSearchView.clearFocus()
                binding.recordsSearchView.visibility = View.GONE
                binding.searchIcon.visibility = View.VISIBLE
                binding.toolbarTitle.visibility = View.VISIBLE
            }
        }*/

        val closeButton = binding.recordsSearchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        closeButton.setOnClickListener {
            Log.e("Coroutine", "close button")
            if(binding.recordsSearchView.query.toString() == "" || binding.recordsSearchView.query == null) {
                binding.recordsSearchView.onActionViewCollapsed()
                Log.e("Coroutine", "Query text in setoncloselistener ${recordViewModel.queryText}")
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
            Log.e("Coroutine", " setonclose listener isfocued "+binding.recordsSearchView.isFocused.toString())
//            binding.recordsSearchView.clearFocus()
            Log.e("Coroutine", " setonclose listener isfocued "+binding.recordsSearchView.isFocused.toString())
            binding.recordsSearchView.isFocused.also {
                Log.e("Coroutine", " setonclose listener isfocued "+it.toString())
            }
            recordViewModel.searchedList.value = recordViewModel.filteredRecords.value.also{
            }
        }

/*
        binding.recordsSearchView.setOnCloseListener {
            if(binding.recordsSearchView.query.toString() == "" || binding.recordsSearchView.query == null) {
                binding.recordsSearchView.onActionViewCollapsed()
                Log.e("Coroutine", "Query text in setoncloselistener ${recordViewModel.queryText}")
                binding.recordsSearchView.isIconified = true
                binding.recordsSearchView.clearFocus()
                binding.searchIcon.visibility = View.VISIBLE
                binding.toolbarTitle.visibility = View.VISIBLE
                binding.recordsSearchView.visibility = View.GONE
            }
            recordViewModel.queryText = ""
            Log.e("Coroutine", " setonclose listener isfocued "+binding.recordsSearchView.isFocused.toString())
            binding.recordsSearchView.clearFocus()
            Log.e("Coroutine", " setonclose listener isfocued "+binding.recordsSearchView.isFocused.toString())
            binding.recordsSearchView.isFocused.also {
                Log.e("Coroutine", " setonclose listener isfocued "+it.toString())
            }
            recordViewModel.searchedList.value = recordViewModel.filteredRecords.value.also{
            }
            true
        }
*/

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

        binding.searchViewBackBtn.setOnClickListener {
            binding.recordsSearchView.onActionViewCollapsed()
            Log.e("Coroutine", "Query text in setoncloselistener ${recordViewModel.queryText}")
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
                Log.e("Coroutine", "new query - ${recordViewModel.newQueryText}")
                Log.e("Coroutine", "Query text ${recordViewModel.queryText}")
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
            } else recordViewModel.queryText = ""
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

        binding.recordsSearchView.isFocused.also {
            Log.e("Coroutine", it.toString())
        }

        binding.recordsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                Log.e("Coroutine", "scroll pos saved records first")
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.e("Coroutine", "scroll pos saved records second")
                    restoreScrollPositionViewModel.scrollPositionRecords = (binding.recordsRecyclerView.layoutManager as GridLayoutManager).findFirstVisibleItemPosition().also {
                        Log.e("Coroutine", "scroll pos saved records third - ${it}")
                    }
                    Log.e("Coroutine", "scroll pos saved records fourth- ${restoreScrollPositionViewModel.scrollPositionRecords}")
                }
            }
        })
/*
        binding.searchIcon.setOnClickListener {
            findNavController().navigate(R.id.action_recordsFragment_to_searchFragment)
        }*/
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

            recordViewModel.allRecords.observe(viewLifecycleOwner, Observer {
//                Log.e("Record", "allRecords")
//                Log.e("Coroutine", "allRecords rec frag savedinstancestate == null")
                /*it?.forEach {
                    Log.e("Coroutine", "date value - ${it.date}")
                }*/
                recordViewModel.fetchRecords()
                args.selectedCategory?.let {category ->
//                    Log.e("Record", "arg cat in let ${args.selectedCategory}")
                    recordViewModel.fetchRecordsOfTheCategory(category)
                }
            })
            recordViewModel.filteredRecords.observe(viewLifecycleOwner, Observer {
                if(it != null) {
                    recordViewModel.searchedList.value = it.also{
                        Log.e("Coroutine", "searchlist updated on view created" + it.toString())
                    }
                }
            })
        } //else Log.e("Search", "saved instance bundle null")

        Log.e("Coroutine", "args?.selectedCat - ${args.selectedCategory}")
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
                if(recordViewModel.recordType.value == RecordType.ALL.value) {
                    binding.recordsConstraint.visibility = View.VISIBLE
                    binding.recordsLinearIncomeExpense.visibility = View.GONE
                } else {
                    binding.recordsConstraint.visibility = View.GONE
                    binding.recordsLinearIncomeExpense.visibility = View.VISIBLE
                }
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
            if(it != null) {
                if(it.isEmpty()) {
                    binding.scrollViewEmptyDataRecords.visibility = View.VISIBLE
                    binding.emptyRecordsList.emptyDataImage.setImageResource(R.drawable.records)
                    binding.emptyRecordsList.emptyDataText.text = "No Records Found"
//                    binding.emptyRecordsList.visibility = View.VISIBLE
//                    binding.cardView.visibility = View.GONE
                    binding.recordsRecyclerView.visibility = View.GONE
                    recordViewModel.getIncomeOfTheMonth()
                    recordViewModel.getExpenseOfTheMonth()
                    recordViewModel.getTotalBalanceOfTheMonth()
                } else {
//                    Log.e("Record", "not empty")
                    binding.scrollViewEmptyDataRecords.visibility = View.GONE
//                    binding.emptyRecordsList.visibility = View.GONE
//                    binding.cardView.visibility = View.VISIBLE
                    binding.recordsRecyclerView.visibility = View.VISIBLE
                    recordViewModel.getIncomeOfTheMonth()
                    recordViewModel.getExpenseOfTheMonth()
                    recordViewModel.getTotalBalanceOfTheMonth()
                    adapter = RecordRecyclerViewAdapter(it, Categories.categoryList)
                    adapter.setTheFragment(this)
                    binding.recordsRecyclerView.adapter = adapter
                    val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
                        resources.configuration.screenWidthDp >= 600) {
                        2
                    } else {
                        1
                    }
                    val layoutManager = GridLayoutManager(this.context, spanCount)//LinearLayoutManager(this.context)
                    binding.recordsRecyclerView.layoutManager = layoutManager
                    Log.e("Coroutine", "scroll position - ${restoreScrollPositionViewModel.scrollPositionRecords}")
//                    binding.recordsSearchView.scrollTo(0, restoreScrollPositionViewModel.scrollPositionRecords)
                    (binding.recordsRecyclerView.layoutManager as GridLayoutManager).scrollToPosition(restoreScrollPositionViewModel.scrollPositionRecords).also {
                        Log.e("Coroutine", "scroll position - ${restoreScrollPositionViewModel.scrollPositionRecords}")
                    }
                    adapter.onItemClick = { record ->
                        recordViewModel.setSelectedRecord(record)
                        moveToNextFragment()
                    }
                    /*adapter.onItemClickRecordsFragment = {
                        record, position ->
                        restoreScrollPositionViewModel.scrollPositionRecords = position
                        Log.e("Coroutine", "position - $position")
                    }*/
                }
            } //else Log.e("Error", "Null")

        })

        recordViewModel.incomeOfTheMonth.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                binding.incomeAmountTV.text = "${Helper.formatNumberToIndianStyle(it)}"
                if(recordViewModel.recordType.value == RecordType.INCOME.value) {
                    binding.currentMonthIncomeExpenseTvLinear.apply{
                        text = "Income"
                    }
                    binding.incomeExpenseAmountTVLinear.text = "${Helper.formatNumberToIndianStyle(it)}"
                    binding.incomeExpenseAmountTVLinear.setTextColor(resources.getColor(R.color.incomeColour))
                }
            }
        })
        recordViewModel.expenseOfTheMonth.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                binding.expenseAmountTV.text = "${Helper.formatNumberToIndianStyle(it)}"
                if(recordViewModel.recordType.value == RecordType.EXPENSE.value) {
                    binding.currentMonthIncomeExpenseTvLinear.apply{
                        text = "Expense"
                    }
                    binding.incomeExpenseAmountTVLinear.text = "${Helper.formatNumberToIndianStyle(it)}"
                    binding.incomeExpenseAmountTVLinear.setTextColor(resources.getColor(R.color.expenseColour))
                }
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

        binding.totalAmountTV.setOnClickListener {
            if(it != null && binding.totalAmountTV.text.toString().isNotEmpty()) {
                showDialog("Net Balance", "₹ ${binding.totalAmountTV.text.toString()}")
            }
        }

        binding.expenseAmountTV.setOnClickListener {
            if(it != null && binding.expenseAmountTV.text.toString().isNotEmpty()) {
                showDialog("Expense", "₹ ${binding.expenseAmountTV.text.toString()}")
            }
        }

        binding.incomeAmountTV.setOnClickListener {
            if(it != null && binding.incomeAmountTV.text.toString().isNotEmpty()) {
                showDialog("Income", "₹ ${binding.incomeAmountTV.text.toString()}")
            }
        }
    }

/*
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
*/

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

        Log.e("Coroutine", " search has focus on resume- " + binding.recordsSearchView.hasFocus())
        if(binding.recordsSearchView.hasFocus()) {
            Log.e("Coroutine", "is not empty - $recordViewModel.queryText.isNotEmpty(), is focused  - ${binding.recordsSearchView.hasFocus()} ")
            binding.searchIcon.visibility = View.GONE
            binding.toolbarTitle.visibility = View.GONE
            binding.back.visibility = View.GONE
            binding.searchViewBackBtn.visibility = View.VISIBLE
            binding.recordsSearchView.visibility = View.VISIBLE
            binding.recordsSearchView.isIconified = false
            binding.recordsSearchView.onActionViewExpanded()
            Log.e("Coroutine", "Query text in if ${recordViewModel.queryText}")
            binding.recordsSearchView.setQuery(recordViewModel.queryText, false)
            binding.recordsSearchView.isFocusable = true
            recordViewModel.searchedList.value = recordViewModel.filteredRecords.value?.filter {
                it.category.lowercase().contains(recordViewModel.queryText, ignoreCase = true)||it.note.lowercase().contains(recordViewModel.queryText, ignoreCase = true)
            }
//            filterCategories(categoryViewModel.queryText)
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        recordViewModel.isSearchViewFocused = binding.recordsSearchView.hasFocus()
        Log.e("Coroutine", " search has focus - " + binding.recordsSearchView.hasFocus())
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun hideInputMethod(view: SearchView) {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view.windowToken,0)
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