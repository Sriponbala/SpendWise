package com.example.spendwise.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendwise.Categories
import com.example.spendwise.R
import com.example.spendwise.adapter.CategoryRecyclerViewAdapter
import com.example.spendwise.databinding.FragmentCategoryBinding
import com.example.spendwise.domain.Category
import com.example.spendwise.viewmodel.CategoryViewModel

class CategoryFragment : Fragment() {

    private lateinit var recordType: String
    private var prevFragment: Int = 0
    private lateinit var binding: FragmentCategoryBinding
    private val categoryViewModel: CategoryViewModel by activityViewModels()
//    private val recordView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoryBinding.inflate(inflater, container, false)
        val args = CategoryFragmentArgs.fromBundle(requireArguments())
        recordType = args.recordType
        prevFragment = args.previousFragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val categories = Categories.categoryList.filter { it.recordType.value == recordType }
        val adapter = CategoryRecyclerViewAdapter(categories)
        binding.categoryRecyclerView.adapter = adapter
        binding.categoryRecyclerView.layoutManager = LinearLayoutManager(this.context)

        adapter.onItemClick = {
            categoryViewModel.category.value = it
            when(prevFragment) {
                R.id.homePageFragment -> {
                    moveToNextFragment()
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