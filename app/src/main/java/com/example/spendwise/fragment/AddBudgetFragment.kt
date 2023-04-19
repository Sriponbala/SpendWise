package com.example.spendwise.fragment

import androidx.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.adapters.TextViewBindingAdapter.setText
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.spendwise.Helper
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.databinding.FragmentAddBudgetBinding
import com.example.spendwise.enums.RecordType
import com.example.spendwise.viewmodel.BudgetViewModel
import com.example.spendwise.viewmodel.CategoryViewModel
import com.example.spendwise.viewmodel.RecordViewModel
import com.example.spendwise.viewmodelfactory.BudgetViewModelFactory
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory
import java.util.*


class AddBudgetFragment : Fragment() {

    private lateinit var binding: FragmentAddBudgetBinding
    private lateinit var budgetViewModel: BudgetViewModel
    private lateinit var recordViewModel: RecordViewModel
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private lateinit var args: AddBudgetFragmentArgs
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = BudgetViewModelFactory(requireActivity().application)
        budgetViewModel = ViewModelProvider(requireActivity(), factory)[BudgetViewModel::class.java]
        val recordViewModelFactory = RecordViewModelFactory(requireActivity().application)
        recordViewModel = ViewModelProvider(requireActivity(), recordViewModelFactory)[RecordViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        args = AddBudgetFragmentArgs.fromBundle(requireArguments())
        (activity as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_close_24)
            title = if(args.isEditBudget) {
                "Edit Budget"
            } else "Add Budget"
        }
        binding = FragmentAddBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(args.isEditBudget && savedInstanceState == null) {
            budgetViewModel.budget.observe(viewLifecycleOwner, Observer {
                if(it != null) {
                    binding.budgetNameEditText.setText(it.budgetName)
                    binding.budgetAmountEditText.setText(it.maxAmount.toString())
                    binding.budgetPeriodEditText.setText(it.period)
                    binding.budgetCategoryEditText.setText(it.category)
                }
            })
        }

        binding.saveButton.setOnClickListener {
            if(args.isEditBudget) {
                updateBudget()
            } else addBudget()
        }

        categoryViewModel.category.observe(viewLifecycleOwner, Observer { category ->
            if(category != null) {
                binding.budgetCategoryEditText.setText(category.title)
                categoryViewModel.category.value = null
            }
        })

        binding.budgetCategoryEditText.setOnClickListener {
            val action = AddBudgetFragmentDirections.actionAddBudgetFragmentToCategoryFragment(RecordType.EXPENSE.value, R.id.addBudgetFragment)
            findNavController().navigate(action)
        }

        /*val category = AddRecordFragmentArgs.fromBundle(requireArguments()).category
        binding.categoryEditText.setText(category)*/

    }

    private fun addBudget() {
        if(validateAllFields()) {
            val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
            Log.e("userId add record", userId.toString())
            budgetViewModel.checkIfCategoryAlreadyExists(userId, binding.budgetCategoryEditText.text.toString(), binding.budgetPeriodEditText.text.toString())
            budgetViewModel.budgetCategoryAlreadyExists.observe(viewLifecycleOwner, Observer {
                if(it != null) {
                    if(it) {
                        Toast.makeText(this.context, "Category ${binding.budgetCategoryEditText.text.toString().toLowerCase()} already exists", Toast.LENGTH_LONG).show()
                    } else {
                        budgetViewModel.insertBudget(
                            userId = userId,
                            budgetName = binding.budgetNameEditText.text.toString(),
                            maxAmount = binding.budgetAmountEditText.text.toString().toFloat(),
                            period = binding.budgetPeriodEditText.text.toString(),
                            category = binding.budgetCategoryEditText.text.toString()
                        )
                        moveToPreviousPage()
                    }
                    budgetViewModel.budgetCategoryAlreadyExists.value = null
                }
            })
        }
    }

    private fun updateBudget() {
        if(validateAllFields()) {
            val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
            Log.e("userId edit record", userId.toString())
            budgetViewModel.updateBudget(
                userId = userId,
                budgetName = binding.budgetNameEditText.text.toString(),
                budgetAmount = binding.budgetAmountEditText.text.toString().toFloat(),
                period = binding.budgetPeriodEditText.text.toString(),
                category = binding.budgetCategoryEditText.text.toString())
               /* userId,
                binding.budgetNameEditText.text.toString(),
                binding.budgetAmountEditText.text.toString().toFloat(),
                binding.budgetPeriodEditText.text.toString(),
                binding.budgetCategoryEditText.text.toString())*/
            moveToPreviousPage()
        }
    }

    private fun validateAllFields(): Boolean {
        return if(binding.budgetNameEditText.text?.isEmpty() == true) {
            binding.budgetNameEditText.error = "Budget name should not be empty"
//            Toast.makeText(this.requireContext(), "Budget name should not be empty", Toast.LENGTH_SHORT).show()
            false
        } else if(binding.budgetAmountEditText.text?.isEmpty() == true) {
            binding.budgetAmountEditText.error = "Amount should not be empty"
//            Toast.makeText(this.requireContext(), "Amount should not be empty", Toast.LENGTH_SHORT).show()
            false
        } else if(!Helper.validateAmount(binding.budgetAmountEditText.text.toString())) {
            binding.budgetAmountEditText.error = "Amount should have min 1 digit before decimal, can have max 5 digits before decimal and max 2 digits after decimal"
//            Toast.makeText(this.requireContext(), "Amount should have min 1 digit before decimal, can have max 5 digits before decimal and max 2 digits after decimal", Toast.LENGTH_SHORT).show()
            false
        } else if(binding.budgetCategoryEditText.text?.isEmpty() == true) {
            binding.budgetCategoryEditText.error = "Category should not be empty"
//            Toast.makeText(this.requireContext(), "Category should not be empty", Toast.LENGTH_SHORT).show()
            false
        } else {
            binding.budgetAmountEditText.error = null
            true
        }
    }

    private fun moveToPreviousPage() {
        this.findNavController().popBackStack()
    }

}