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
import androidx.core.widget.addTextChangedListener
import androidx.databinding.adapters.TextViewBindingAdapter.setText
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.spendwise.Helper
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.databinding.FragmentAddBudgetBinding
import com.example.spendwise.domain.Budget
import com.example.spendwise.enums.Period
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
        binding = FragmentAddBudgetBinding.inflate(inflater, container, false)
        args = AddBudgetFragmentArgs.fromBundle(requireArguments())
        /*setHasOptionsMenu(true)
        binding.toolbarAddBudgets.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }*/
        /*(activity as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_close_24)
            title = if(args.isEditBudget) {
                "Edit Budget"
            } else "Add Budget"
        }*/
        binding.toolbarAddBudget.apply {
            title = if(args.isEditBudget) {
                "Edit Budget"
            } else {
                "Add Budget"
            }
            setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(args.isEditBudget && savedInstanceState == null) {
            if(recordViewModel.isTempDataSet) {
                recordViewModel.tempData.observe(viewLifecycleOwner, Observer {
                    Log.e("Edit", "isTempData true, inside temp data observe")
                    if(it != null) {
                        Log.e("Edit", "temp data not null ${it.toString()}")
                        binding.budgetNameEditText.setText(it["BudgetName"])
                        binding.budgetAmountEditText.setText(it["Amount"])
                        recordViewModel.tempData.value = null
                    }
                })
            } else {
                budgetViewModel.budget.observe(viewLifecycleOwner, Observer {
                    if(it != null) {
                        binding.budgetNameEditText.setText(it.budgetName)
                        binding.budgetAmountEditText.setText(Helper.formatDecimal(it.maxAmount.toBigDecimal()))
//                        binding.budgetPeriodEditText.setText(it.period)
                        binding.budgetCategoryEditText.setText(it.category)
                    }
                })
            }
        }

        binding.saveButton.setOnClickListener {
            if(args.isEditBudget) {
                recordViewModel.isTempDataSet = false
                updateBudget()
            } else addBudget()
        }

        categoryViewModel.category.observe(viewLifecycleOwner, Observer { category ->
            if(category != null) {
                categoryViewModel.queryText = ""
                binding.budgetCategoryEditText.setText(category.title)
                categoryViewModel.category.value = null
            }
        })

        binding.budgetCategoryEditText.setOnClickListener {
            if(args.isEditBudget) {
                recordViewModel.isTempDataSet = true
                recordViewModel.tempData.value = mapOf("BudgetName" to binding.budgetNameEditText.text.toString(), "Amount" to binding.budgetAmountEditText.text.toString())
            }
            val action = AddBudgetFragmentDirections.actionAddBudgetFragmentToCategoryFragment(RecordType.EXPENSE.value, R.id.addBudgetFragment)
            findNavController().navigate(action)
        }

        /*val category = AddRecordFragmentArgs.fromBundle(requireArguments()).category
        binding.categoryEditText.setText(category)*/

        binding.budgetNameEditText.addTextChangedListener {
            if(it != null && it.toString().isNotEmpty()) {
                binding.budgetNameTextInputLayout.error = null
            }
        }
        binding.budgetCategoryEditText.addTextChangedListener {
            if(it != null && it.toString().isNotEmpty()) {
                binding.budgetCategoryTextInputLayout.error = null
            }
        }
        binding.budgetAmountEditText.addTextChangedListener {
            if(it != null && it.toString().isNotEmpty()) {
//                binding.budgetAmountTextInputLayout.error = null
                if(budgetViewModel.amountError.isNotEmpty()) {
                    if(binding.budgetAmountEditText.text?.isEmpty() == true) {
                        binding.budgetAmountTextInputLayout.error = "Amount should not be empty"
                        budgetViewModel.amountError = "Amount should not be empty"
                    } else if(Helper.checkAmountIsZeroOrNot(binding.budgetAmountEditText.text.toString())) {
                        binding.budgetAmountTextInputLayout.error = "Amount can not be zero"
                        budgetViewModel.amountError = "Amount can not be zero"
                    } else if(!Helper.validateAmount(binding.budgetAmountEditText.text.toString())) {
                        binding.budgetAmountTextInputLayout.error = "Should have 1 to 5 digits before decimal, 0 to 2 digits after decimal & decimal not mandatory"//"Amount should have min 1 digit before decimal, can have max 5 digits before decimal and max 2 digits after decimal"
                        budgetViewModel.amountError = "Should have 1 to 5 digits before decimal, 0 to 2 digits after decimal & decimal not mandatory"//"Amount should have min 1 digit before decimal, can have max 5 digits before decimal and max 2 digits after decimal"
                    } else {
                        binding.budgetAmountTextInputLayout.error = null
                        budgetViewModel.amountError = ""
                    }
                } else {
                    binding.budgetAmountTextInputLayout.error = null
                }
            }
        }
    }

    private fun addBudget() {
        if(validateAllFields()) {
            val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
            Log.e("userId add record", userId.toString())
            budgetViewModel.checkIfCategoryAlreadyExists(userId, binding.budgetCategoryEditText.text.toString(), Period.MONTH.value)//binding.budgetPeriodEditText.text.toString())
            budgetViewModel.budgetCategoryAlreadyExists.observe(viewLifecycleOwner, Observer {
                if(it != null) {
                    if(it) {
                        Toast.makeText(this.context, "Category ${binding.budgetCategoryEditText.text.toString().toLowerCase()} already exists", Toast.LENGTH_LONG).show()
                    } else {
                        budgetViewModel.insertBudget(
                            userId = userId,
                            budgetName = binding.budgetNameEditText.text.toString(),
                            maxAmount = binding.budgetAmountEditText.text.toString(),
//                            period = binding.budgetPeriodEditText.text.toString(),
                            period = Period.MONTH.value,
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
            if(binding.budgetCategoryEditText.text.toString() == budgetViewModel.budget.value?.category) {
                budgetViewModel.updateBudget(
                    userId = userId,
                    budgetName = binding.budgetNameEditText.text.toString(),
                    budgetAmount = binding.budgetAmountEditText.text.toString(),
                    period = Period.MONTH.value,//binding.budgetPeriodEditText.text.toString(),
                    category = binding.budgetCategoryEditText.text.toString())
                /* userId,
                 binding.budgetNameEditText.text.toString(),
                 binding.budgetAmountEditText.text.toString().toFloat(),
                 binding.budgetPeriodEditText.text.toString(),
                 binding.budgetCategoryEditText.text.toString())*/
                moveToPreviousPage()
            } else {
                budgetViewModel.checkIfCategoryAlreadyExists(userId, binding.budgetCategoryEditText.text.toString(), Period.MONTH.value)//binding.budgetPeriodEditText.text.toString())
                budgetViewModel.budgetCategoryAlreadyExists.observe(viewLifecycleOwner, Observer {
                    if(it != null) {
                        if(it) {
                            Toast.makeText(this.context, "Category ${binding.budgetCategoryEditText.text.toString().toLowerCase()} already exists", Toast.LENGTH_LONG).show()
                        } else {
                            budgetViewModel.updateBudget(
                                userId = userId,
                                budgetName = binding.budgetNameEditText.text.toString(),
                                budgetAmount = binding.budgetAmountEditText.text.toString(),
                                period = Period.MONTH.value, //binding.budgetPeriodEditText.text.toString(),
                                category = binding.budgetCategoryEditText.text.toString())
                            /* userId,
                             binding.budgetNameEditText.text.toString(),
                             binding.budgetAmountEditText.text.toString().toFloat(),
                             binding.budgetPeriodEditText.text.toString(),
                             binding.budgetCategoryEditText.text.toString())*/
                            moveToPreviousPage()
                        }
                        budgetViewModel.budgetCategoryAlreadyExists.value = null
                    }
                })
            }
            /*budgetViewModel.updateBudget(
                userId = userId,
                budgetName = binding.budgetNameEditText.text.toString(),
                budgetAmount = binding.budgetAmountEditText.text.toString().toFloat(),
                period = binding.budgetPeriodEditText.text.toString(),
                category = binding.budgetCategoryEditText.text.toString())
               *//* userId,
                binding.budgetNameEditText.text.toString(),
                binding.budgetAmountEditText.text.toString().toFloat(),
                binding.budgetPeriodEditText.text.toString(),
                binding.budgetCategoryEditText.text.toString())*//*
            moveToPreviousPage()*/
        }
    }

    private fun validateAllFields(): Boolean {
        if(binding.budgetNameEditText.text?.isEmpty() == true) {
            binding.budgetNameTextInputLayout.error = "Budget name should not be empty"
        } else {
            binding.budgetNameTextInputLayout.error = null
        }
        if(binding.budgetAmountEditText.text?.isEmpty() == true) {
            binding.budgetAmountTextInputLayout.error = "Amount should not be empty"
            budgetViewModel.amountError = "Amount should not be empty"
        } else if(Helper.checkAmountIsZeroOrNot(binding.budgetAmountEditText.text.toString())) {
//            binding.amountEditText.error = "Amount can not be zero"
            binding.budgetAmountTextInputLayout.error = "Amount can not be zero"
            budgetViewModel.amountError = "Amount can not be zero"
        } else if(!Helper.validateAmount(binding.budgetAmountEditText.text.toString())) {
            binding.budgetAmountTextInputLayout.error = "Should have 1 to 5 digits before decimal, 0 to 2 digits after decimal & decimal not mandatory"//"Amount should have min 1 digit before decimal, can have max 5 digits before decimal and max 2 digits after decimal"
            budgetViewModel.amountError = "Should have 1 to 5 digits before decimal, 0 to 2 digits after decimal & decimal not mandatory"//"Amount should have min 1 digit before decimal, can have max 5 digits before decimal and max 2 digits after decimal"
        } else {
            binding.budgetAmountTextInputLayout.error = null
            budgetViewModel.amountError = ""
        }
        if(binding.budgetCategoryEditText.text?.isEmpty() == true) {
            binding.budgetCategoryTextInputLayout.error = "Category should not be empty"
        } else {
            binding.budgetCategoryTextInputLayout.error = null
        }

        return if(binding.budgetNameEditText.text?.isEmpty() == true || Helper.checkAmountIsZeroOrNot(binding.budgetAmountEditText.text.toString())) {
//            binding.budgetNameEditText.error = "Budget name should not be empty"
//            Toast.makeText(this.requireContext(), "Budget name should not be empty", Toast.LENGTH_SHORT).show()
            false
        } else if(binding.budgetAmountEditText.text?.isEmpty() == true) {
//            binding.budgetAmountEditText.error = "Amount should not be empty"
//            Toast.makeText(this.requireContext(), "Amount should not be empty", Toast.LENGTH_SHORT).show()
            false
        } else if(!Helper.validateAmount(binding.budgetAmountEditText.text.toString())) {
//            binding.budgetAmountEditText.error = "Amount should have min 1 digit before decimal, can have max 5 digits before decimal and max 2 digits after decimal"
//            Toast.makeText(this.requireContext(), "Amount should have min 1 digit before decimal, can have max 5 digits before decimal and max 2 digits after decimal", Toast.LENGTH_SHORT).show()
            false
        } else if(binding.budgetCategoryEditText.text?.isEmpty() == true) {
//            binding.budgetCategoryEditText.error = "Category should not be empty"
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