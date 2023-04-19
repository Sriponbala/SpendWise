package com.example.spendwise.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendwise.Categories
import com.example.spendwise.Categories.categoryList
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.adapter.CategoryRecyclerViewAdapter
import com.example.spendwise.databinding.FragmentCategoryBinding
import com.example.spendwise.domain.Category
import com.example.spendwise.viewmodel.CategoryViewModel
import java.util.*
import java.util.Collections.addAll

class CategoryFragment : Fragment() {

    private lateinit var recordType: String
    private var prevFragment: Int = 0
    private lateinit var binding: FragmentCategoryBinding
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private lateinit var adapter: CategoryRecyclerViewAdapter
    private lateinit var searchView: SearchView
//    private val recordView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState == null) {
           // categoryViewModel = CategoryViewModel()
            categoryViewModel.also{
                Log.e("Search", it.toString())
            }
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back_arrow)
        }
        binding = FragmentCategoryBinding.inflate(inflater, container, false)
        val args = CategoryFragmentArgs.fromBundle(requireArguments())
        recordType = args.recordType
        prevFragment = args.previousFragment
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
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
    private fun filterCategories(newText: String?) {
        if(newText?.isNotEmpty() == true){
            categoryViewModel.searchedList.value = categoryViewModel.filteredCategoryList.value?.filter {
                it.title.lowercase().contains(newText.lowercase())
            }
            categoryViewModel.queryText = newText
        }else{
            if (newText != null) {
                categoryViewModel.queryText = newText
            }
            categoryViewModel.filteredCategoryList.value = categoryList.filter { it.recordType.value == recordType }
            categoryViewModel.searchedList.value = categoryList.filter { it.recordType.value == recordType }

        }

        // Notify adapter with filtered list
        categoryViewModel.searchedList.value?.let {
            adapter.filterList(it)
        }
//        adapter.filterList(categoryViewModel.searchedList.value)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if(savedInstanceState == null) {
            Log.e("Search", "saved instance bundle not null")
            categoryViewModel.filteredCategoryList.value = categoryList.filter{category ->
                category.recordType.value == recordType }
        } else Log.e("Search", "saved instance bundle null")

        categoryViewModel.filteredCategoryList.observe(viewLifecycleOwner, Observer{
            Log.e("Search", "filteredcatlist 1")
            if(it != null) {
                Log.e("Search", "filteredcatlist 2")
                /*categoryViewModel.filteredCategoryList.value = categoryList.filter{category ->
                    category.recordType.value == recordType }*/
                categoryViewModel.searchedList.value = it
            }
            Log.e("Search", "filteredcatlist 3")
        })

     //   val categories = categoryList.filter { it.recordType.value == recordType }

        categoryViewModel.searchedList.observe(viewLifecycleOwner, Observer {
            Log.e("Search", "filteredcatlist 4")
            if(it != null) {
                Log.e("Search", "filteredcatlist 5")
                adapter = CategoryRecyclerViewAdapter(it)
                binding.categoryRecyclerView.adapter = adapter
                binding.categoryRecyclerView.layoutManager = LinearLayoutManager(this.context)
                adapter.onItemClick = {
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