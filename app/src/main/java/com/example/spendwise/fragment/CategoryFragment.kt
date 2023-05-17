package com.example.spendwise.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spendwise.Categories.categoryList
import com.example.spendwise.R
import com.example.spendwise.adapter.CategoryRecyclerViewAdapter
import com.example.spendwise.databinding.FragmentCategoryBinding
import com.example.spendwise.enums.RecordType
import com.example.spendwise.viewmodel.CategoryViewModel
import com.example.spendwise.viewmodel.RestoreScrollPositionViewModel
import com.example.spendwise.viewmodelfactory.RestoreScrollPositionViewModelFactory

class CategoryFragment : Fragment() {

    private lateinit var recordType: String
    private var prevFragment: Int = 0
    private lateinit var binding: FragmentCategoryBinding
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private lateinit var restoreScrollPositionViewModel: RestoreScrollPositionViewModel
    private lateinit var adapter: CategoryRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val restoreScrollPositionFactory = RestoreScrollPositionViewModelFactory(requireActivity().application)
        restoreScrollPositionViewModel = ViewModelProvider(this, restoreScrollPositionFactory)[RestoreScrollPositionViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoryBinding.inflate(inflater, container, false)
        val args = CategoryFragmentArgs.fromBundle(requireArguments())

        binding.toolbarCategory.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.toolbarCategory.apply {
            title = when(args.recordType) {
                RecordType.INCOME.value -> {
                    resources.getString(R.string.two_strings_concate, args.recordType, resources.getString(R.string.categories_label))
                }
                RecordType.EXPENSE.value -> {
                    resources.getString(R.string.two_strings_concate, args.recordType, resources.getString(R.string.categories_label))
                }
                else -> {
                    resources.getString(R.string.select_a_category)
                }
            }
        }

        recordType = args.recordType
        prevFragment = args.previousFragment
        return binding.root
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
            categoryViewModel.filteredCategoryList.value = categoryList.filter{category ->
                category.recordType.value == recordType }
        }

        categoryViewModel.filteredCategoryList.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.isNotEmpty()) {
                    binding.categoryRecyclerView.visibility = View.VISIBLE
                    binding.emptyCategoriesTextView.visibility = View.GONE
                    adapter = CategoryRecyclerViewAdapter(it)
                    binding.categoryRecyclerView.adapter = adapter
                    binding.categoryRecyclerView.layoutManager = LinearLayoutManager(this.context)
                    (binding.categoryRecyclerView.layoutManager as LinearLayoutManager).scrollToPosition(
                        restoreScrollPositionViewModel.scrollPositionCategory
                    )
                    adapter.onItemClick = { category ->
                        categoryViewModel.category.value = category
                        when (prevFragment) {
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
        }

    }

    private fun moveToPreviousFragment() {
        findNavController().popBackStack()
    }

    private fun moveToNextFragment() {
        val action = CategoryFragmentDirections.actionCategoryFragmentToRecordsFragment(hideDescriptionText = false, hideAmountView = true, hideFilterView = true, selectedCategory = categoryViewModel.category.value)
        findNavController().navigate(action)
    }

}