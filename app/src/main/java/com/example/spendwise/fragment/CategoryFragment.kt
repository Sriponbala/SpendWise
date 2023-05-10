package com.example.spendwise.fragment

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.appcompat.widget.SearchView
import android.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spendwise.Categories
import com.example.spendwise.Categories.categoryList
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.adapter.CategoryRecyclerViewAdapter
import com.example.spendwise.databinding.FragmentCategoryBinding
import com.example.spendwise.domain.Category
import com.example.spendwise.enums.RecordType
import com.example.spendwise.viewmodel.CategoryViewModel
import com.example.spendwise.viewmodel.RestoreScrollPositionViewModel
import com.example.spendwise.viewmodelfactory.RestoreScrollPositionViewModelFactory
import java.util.*
import java.util.Collections.addAll

class CategoryFragment : Fragment() {

    private lateinit var recordType: String
    private var prevFragment: Int = 0
    private lateinit var binding: FragmentCategoryBinding
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private lateinit var restoreScrollPositionViewModel: RestoreScrollPositionViewModel
    private lateinit var adapter: CategoryRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState == null) {
            categoryViewModel.also{
                Log.e("Search", it.toString())
                Log.e("Category", it.toString())
            }
        }
        val restoreScrollPositionFactory = RestoreScrollPositionViewModelFactory(requireActivity().application)
        restoreScrollPositionViewModel = ViewModelProvider(this, restoreScrollPositionFactory)[RestoreScrollPositionViewModel::class.java]
        setHasOptionsMenu(true)
        Log.e("Category", "below setHasOptionsMenu")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoryBinding.inflate(inflater, container, false)
        val args = CategoryFragmentArgs.fromBundle(requireArguments())

        if(savedInstanceState == null) {
            /*categoryViewModel.queryText = ""
            searchView.setQuery("", false)
            searchView.onActionViewCollapsed()*/
            Log.e("Category", "savedinstancebundle == null")
        } else {
            Log.e("Category", "savedinstancebundle != null")
        }

        Log.e("Category", "Below search view")

       // searchView.queryHint = "Search Categories"
        /*Log.e("Category", "query text : ${categoryViewModel.queryText}")
        if(categoryViewModel.queryText.isNotEmpty()) {
            binding.categorySearchView.isIconified = false
            binding.categorySearchView.onActionViewExpanded()
            binding.categorySearchView.setQuery(categoryViewModel.queryText, false)
            binding.categorySearchView.isFocusable = true
            *//*Log.e("Category", "query text not empty : ${categoryViewModel.queryText}")
            searchView.isIconified = false
            searchItem.expandActionView()
            searchView.setQuery(categoryViewModel.queryText.also {
                Log.e("Category", "inside query text in searchview.setQuery: $it")
            }, false)
            searchView.isFocusable = true*//*
            categoryViewModel.searchedList.value = categoryViewModel.filteredCategoryList.value?.filter {
                Log.e("Category", "inside filtered category list above lowercase")
                it.title.lowercase().contains(categoryViewModel.queryText)//.lowercase()) //.lowercase add pananum
            }
//            filterCategories(categoryViewModel.queryText)
            Log.e("Category", categoryViewModel.searchedList.value.toString())
        }*/ /*else {
            Log.e("Category", "query text empty : ${categoryViewModel.queryText}")
            searchView.isIconified = true
            searchItem.collapseActionView()
            searchView.isFocusable = false
            searchView.setQuery(categoryViewModel.queryText, true)
        }*/
//        searchView.setIconifiedByDefault(true)

        /*binding.categorySearchView.setOnCloseListener {
            Log.e("Records search", "inside setOn close listener")
            categoryViewModel.queryText = ""
            binding.categorySearchView.clearFocus()
            if(binding.categorySearchView.query.toString() == "" || binding.categorySearchView.query == null) {
                binding.categorySearchView.onActionViewCollapsed()
            }
            categoryViewModel.searchedList.value = categoryViewModel.filteredCategoryList.value.also{
                Log.e("Records search", "filtered rec list - $it")
            }
            true
        }*/

        /*searchView.setOnCloseListener {
            Log.e("Category", "inside setOn close listener")
            categoryViewModel.queryText = ""
            searchView.clearFocus()
            categoryViewModel.searchedList.value = categoryViewModel.filteredCategoryList.value.also{
                Log.e("Category", "filteredcatlist - $it")
            }
            true
        }*/

        /*binding.categorySearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.e("Category", "onQueryTextListener")
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.e("Category", "onQueryTextChangeListener : $newText")
                filterCategories(newText)
                return false
            }
        })*/
        binding.toolbarCategory.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        /*binding.toolbarCategory.setOnMenuItemClickListener {
            Log.e("Search", "action search start, ${it.itemId} ${R.id.action_search}")
            onOptionsItemSelected(it)
        }*/

        binding.toolbarCategory.apply {
            title = when(args.recordType) {
                RecordType.INCOME.value -> {
                    "${args.recordType} Categories"
                }
                RecordType.EXPENSE.value -> {
                    "${args.recordType} Categories"
                }
                else -> {
                    "Select A Category"
                }
            }
        }

        recordType = args.recordType
        prevFragment = args.previousFragment
        return binding.root
    }

/*    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.e("Search", "action search start, ${item.itemId} ${R.id.action_search}")
        when(item.itemId) {
            R.id.action_search -> {
                Log.e("Search", "action search start")
                item.apply {
                    icon = resources.getDrawable(R.drawable.new_search_icon)
//                    iconTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
                }
                searchView = item.actionView as SearchView

                if(categoryViewModel.queryText.isNotEmpty()) {
                    searchView.isIconified = false
                    item.expandActionView()
                    searchView.setQuery(categoryViewModel.queryText, false)
                    searchView.isFocusable = true
                    categoryViewModel.searchedList.value = categoryViewModel.filteredCategoryList.value?.filter {
                        it.title.lowercase().contains(categoryViewModel.queryText)
                    }
                }
                searchView.setIconifiedByDefault(true)

                searchView.setOnCloseListener {
                    categoryViewModel.queryText = ""
                    searchView.clearFocus()
                    categoryViewModel.searchedList.value = categoryViewModel.filteredCategoryList.value
                    true
                }

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        filterCategories(newText)
                        return false
                    }
                })
            }
        }
        return super.onOptionsItemSelected(item)
    }*/

/*
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.e("Search", "onCreateOptionsMenu")
        inflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        searchItem.apply {
            iconTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
        }
        searchView = searchItem.actionView as SearchView

        if(categoryViewModel.queryText.isNotEmpty()) {
            searchView.isIconified = false
            searchItem.expandActionView()
            searchView.setQuery(categoryViewModel.queryText, false)
            searchView.isFocusable = true
            categoryViewModel.searchedList.value = categoryViewModel.filteredCategoryList.value?.filter {
                it.title.lowercase().contains(categoryViewModel.queryText)
            }
        }
        searchView.setIconifiedByDefault(true)

        searchView.setOnCloseListener {
            categoryViewModel.queryText = ""
            searchView.clearFocus()
            categoryViewModel.searchedList.value = categoryViewModel.filteredCategoryList.value
            true
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterCategories(newText)
                return false
            }
        })
    }
*/
    private fun filterCategories(newText: String?) {
        if(newText?.isNotEmpty() == true){
            Log.e("Category", "New text not empty - $newText")
            categoryViewModel.searchedList.value = categoryViewModel.filteredCategoryList.value?.filter {
                it.title.lowercase().contains(newText.lowercase())
            }
            categoryViewModel.queryText = newText.also {
                Log.e("Category", "inside new text (if): $it")
            }
        }else{
            if (newText != null) {
                Log.e("Category", "new text != null - $newText")
                categoryViewModel.queryText = newText.also {
                    Log.e("Category", "inside new text (else): $it")
                }
            }
            categoryViewModel.filteredCategoryList.value = categoryList.filter { it.recordType.value == recordType }
            categoryViewModel.searchedList.value = categoryList.filter { it.recordType.value == recordType }

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

        binding.categoryRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    restoreScrollPositionViewModel.scrollPositionCategory = (binding.categoryRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                }
            }
        })


        if(savedInstanceState == null) {
            Log.e("Search", "saved instance bundle not null")
            categoryViewModel.filteredCategoryList.value = categoryList.filter{category ->
                category.recordType.value == recordType }
            /*categoryViewModel.filteredCategoryList.observe(viewLifecycleOwner, Observer{
                Log.e("Search", "filteredcatlist 1 $it")
                if(it != null) {
                    Log.e("Search", "filteredcatlist 2 $it")
                    *//*categoryViewModel.filteredCategoryList.value = categoryList.filter{category ->
                        category.recordType.value == recordType }*//*
                    categoryViewModel.searchedList.value = it
                }
                Log.e("Search", "filteredcatlist 3")
            })*/
        } else Log.e("Search", "saved instance bundle null")

//        categoryViewModel.filteredCategoryList.observe(viewLifecycleOwner, Observer{
//            Log.e("Search", "filteredcatlist 1 $it")
//            if(it != null) {
//                Log.e("Search", "filteredcatlist 2 $it")
//                /*categoryViewModel.filteredCategoryList.value = categoryList.filter{category ->
//                    category.recordType.value == recordType }*/
//                categoryViewModel.searchedList.value = it
//            }
//            Log.e("Search", "filteredcatlist 3")
//        })

     //   val categories = categoryList.filter { it.recordType.value == recordType }

        categoryViewModel.filteredCategoryList.observe(viewLifecycleOwner, Observer {
            Log.e("Search", "filteredcatlist 4 $it")
            if(it != null) {
                if(it.isNotEmpty()) {
                    Log.e("Search", "filteredcatlist 5 $it")
                    binding.categoryRecyclerView.visibility = View.VISIBLE
                    binding.emptyCategoriesTextView.visibility = View.GONE
                    adapter = CategoryRecyclerViewAdapter(it)
                    binding.categoryRecyclerView.adapter = adapter
                    binding.categoryRecyclerView.layoutManager = LinearLayoutManager(this.context)
                    (binding.categoryRecyclerView.layoutManager as LinearLayoutManager).scrollToPosition(restoreScrollPositionViewModel.scrollPositionCategory)
                    adapter.onItemClick = { category ->
                        categoryViewModel.category.value = category
                        /*binding.categorySearchView.isIconified = true
                        binding.categorySearchView.isFocusable = false
                        categoryViewModel.queryText = ""
                        binding.categorySearchView.setQuery("", false)
                        binding.categorySearchView.clearFocus()
                        binding.categorySearchView.onActionViewCollapsed()*/

                        when(prevFragment) {
                            R.id.homePageFragment -> {
                                moveToNextFragment()
                                categoryViewModel.category.value = null
                            }
                            R.id.addRecordFragment -> {
                                moveToPreviousFragment()
                            }
                            R.id.addBudgetFragment -> {
                                moveToPreviousFragment()
                            }
                        }
                    }
                } else {
                    adapter = CategoryRecyclerViewAdapter(it)
                    binding.categoryRecyclerView.adapter = adapter
                    binding.categoryRecyclerView.layoutManager = LinearLayoutManager(this.context)
                    binding.categoryRecyclerView.visibility = View.GONE
                    binding.emptyCategoriesTextView.visibility = View.VISIBLE
                }

            }
        })
       /* categoryViewModel.searchedList.value?.let {
            Log.e("Search", "filteredcatlist 4")
            adapter = CategoryRecyclerViewAdapter(it)
        }*/

        /*binding.categoryRecyclerView.adapter = adapter
        binding.categoryRecyclerView.layoutManager = LinearLayoutManager(this.context)*/

        /*adapter.onItemClick = {
            categoryViewModel.category.value = it
            when(prevFragment) {
                R.id.homePageFragment -> {
                    moveToNextFragment()
                    categoryViewModel.category.value = null
                }
                R.id.addRecordFragment -> {
                    moveToPreviousFragment()
                }
                R.id.addBudgetFragment -> {
                    moveToPreviousFragment()
                }
            }
        }*/
    }

    override fun onStop() {
        super.onStop()

    }

    private fun moveToPreviousFragment() {
        findNavController().popBackStack()
    }

    private fun moveToNextFragment() {
        Log.e("Next" , "hi")
        Log.e("Record", "Cat - ${categoryViewModel.category.value}")
        val action = CategoryFragmentDirections.actionCategoryFragmentToRecordsFragment(hideDescriptionText = false, hideAmountView = true, hideFilterView = true, selectedCategory = categoryViewModel.category.value)
        findNavController().navigate(action)
    }

}