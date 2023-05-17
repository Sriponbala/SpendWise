package com.example.spendwise.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.spendwise.Helper
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.databinding.FragmentAddBudgetBinding
import com.example.spendwise.enums.Period
import com.example.spendwise.enums.RecordType
import com.example.spendwise.viewmodel.BudgetViewModel
import com.example.spendwise.viewmodel.CategoryViewModel
import com.example.spendwise.viewmodel.RecordViewModel
import com.example.spendwise.viewmodelfactory.BudgetViewModelFactory
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory


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
        binding.toolbarAddBudget.apply {
            title = if(args.isEditBudget) {
                resources.getString(R.string.two_strings_concate, resources.getString(R.string.edit), resources.getString(R.string.budget_label))
            } else {
                resources.getString(R.string.two_strings_concate, resources.getString(R.string.add), resources.getString(R.string.budget_label))
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
                recordViewModel.tempData.observe(viewLifecycleOwner) {
                    if (it != null) {
                        binding.budgetNameEditText.setText(it[resources.getString(R.string.budget_name_label)])
                        binding.budgetAmountEditText.setText(it[resources.getString(R.string.amount_label)])
                        recordViewModel.tempData.value = null
                    }
                }
            } else {
                budgetViewModel.budget.observe(viewLifecycleOwner) {
                    if (it != null) {
                        binding.budgetNameEditText.setText(it.budgetName)
                        binding.budgetAmountEditText.setText(Helper.formatDecimal(it.maxAmount.toBigDecimal()))
                        binding.budgetCategoryEditText.setText(it.category)
                    }
                }
            }
        }

        binding.saveButton.setOnClickListener {
            if(args.isEditBudget) {
                recordViewModel.isTempDataSet = false
                updateBudget()
            } else addBudget()
        }

        categoryViewModel.category.observe(viewLifecycleOwner) { category ->
            if (category != null) {
                categoryViewModel.queryText = ""
                binding.budgetCategoryEditText.setText(category.title)
                categoryViewModel.category.value = null
            }
        }

        binding.budgetCategoryEditText.setOnClickListener {
            if(args.isEditBudget) {
                recordViewModel.isTempDataSet = true
                recordViewModel.tempData.value = mapOf(resources.getString(R.string.budget_name_label) to binding.budgetNameEditText.text.toString(), resources.getString(R.string.amount_label) to binding.budgetAmountEditText.text.toString())
            }
            val action = AddBudgetFragmentDirections.actionAddBudgetFragmentToCategoryFragment(RecordType.EXPENSE.value, R.id.addBudgetFragment)
            findNavController().navigate(action)
        }

        binding.budgetNameEditText.addTextChangedListener {
            if(it != null && it.toString().isNotEmpty()) {
                if(budgetViewModel.budgetNameError.isNotEmpty()) {
                    if(Helper.validateName(binding.budgetNameEditText.text.toString())){
                        binding.budgetNameTextInputLayout.error = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.budget_name))
                        budgetViewModel.budgetNameError = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.budget_name))
                    } else {
                        binding.budgetCategoryTextInputLayout.error = null
                        budgetViewModel.budgetNameError = ""
                    }
                } else {
                    binding.budgetNameTextInputLayout.error = null
                }
//                binding.budgetNameTextInputLayout.error = null
            }
        }
        binding.budgetCategoryEditText.addTextChangedListener {
            if(it != null && it.toString().isNotEmpty()) {
                binding.budgetCategoryTextInputLayout.error = null
            }
        }
        binding.budgetAmountEditText.addTextChangedListener {
            if(it != null && it.toString().isNotEmpty()) {
                if(budgetViewModel.amountError.isNotEmpty()) {
                    if(binding.budgetAmountEditText.text?.isEmpty() == true) {
                        binding.budgetAmountTextInputLayout.error = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.amount_label))
                        budgetViewModel.amountError = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.amount_label))
                    } else if(Helper.checkAmountIsZeroOrNot(binding.budgetAmountEditText.text.toString())) {
                        binding.budgetAmountTextInputLayout.error = resources.getString(R.string.amount_can_not_be_zero)
                        budgetViewModel.amountError = resources.getString(R.string.amount_can_not_be_zero)
                    } else if(!Helper.validateAmount(binding.budgetAmountEditText.text.toString())) {
                        binding.budgetAmountTextInputLayout.error = resources.getString(R.string.amount_format_message)
                        budgetViewModel.amountError = resources.getString(R.string.amount_format_message)
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
            val userId = (activity as MainActivity).getSharedPreferences(resources.getString(R.string.loginStatus), Context.MODE_PRIVATE).getInt("userId", 0)
            budgetViewModel.checkIfCategoryAlreadyExists(userId, binding.budgetCategoryEditText.text.toString(), Period.MONTH.value)
            budgetViewModel.budgetCategoryAlreadyExists.observe(viewLifecycleOwner) {
                if (it != null) {
                    if (it) {
                        Toast.makeText(
                            this.context,
                            resources.getString(R.string.three_strings_concate, resources.getString(R.string.category), binding.budgetCategoryEditText.text.toString().lowercase(), resources.getString(R.string.already_exists)),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        budgetViewModel.insertBudget(
                            userId = userId,
                            budgetName = binding.budgetNameEditText.text.toString(),
                            maxAmount = binding.budgetAmountEditText.text.toString(),
                            period = Period.MONTH.value,
                            category = binding.budgetCategoryEditText.text.toString()
                        )
                        moveToPreviousPage()
                    }
                    budgetViewModel.budgetCategoryAlreadyExists.value = null
                }
            }
        }
    }

    private fun updateBudget() {
        if(validateAllFields()) {
            val userId = (activity as MainActivity).getSharedPreferences(resources.getString(R.string.loginStatus), Context.MODE_PRIVATE).getInt("userId", 0)
            if(binding.budgetCategoryEditText.text.toString() == budgetViewModel.budget.value?.category) {
                budgetViewModel.updateBudget(
                    userId = userId,
                    budgetName = binding.budgetNameEditText.text.toString(),
                    budgetAmount = binding.budgetAmountEditText.text.toString(),
                    period = Period.MONTH.value,
                    category = binding.budgetCategoryEditText.text.toString())
                moveToPreviousPage()
            } else {
                budgetViewModel.checkIfCategoryAlreadyExists(userId, binding.budgetCategoryEditText.text.toString(), Period.MONTH.value)
                budgetViewModel.budgetCategoryAlreadyExists.observe(viewLifecycleOwner) {
                    if (it != null) {
                        if (it) {
                            Toast.makeText(
                                this.context,
                                resources.getString(R.string.three_strings_concate, resources.getString(R.string.category), binding.budgetCategoryEditText.text.toString().lowercase(), resources.getString(R.string.already_exists)),
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            budgetViewModel.updateBudget(
                                userId = userId,
                                budgetName = binding.budgetNameEditText.text.toString(),
                                budgetAmount = binding.budgetAmountEditText.text.toString(),
                                period = Period.MONTH.value,
                                category = binding.budgetCategoryEditText.text.toString()
                            )
                            moveToPreviousPage()
                        }
                        budgetViewModel.budgetCategoryAlreadyExists.value = null
                    }
                }
            }
        }
    }

    private fun validateAllFields(): Boolean {
        if(binding.budgetNameEditText.text?.isEmpty() == true) {
            binding.budgetNameTextInputLayout.error = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.budget_name))
        }  else if(Helper.validateName(binding.budgetNameEditText.text.toString())){
            binding.budgetNameTextInputLayout.error = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.budget_name))
            budgetViewModel.budgetNameError = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.budget_name))
        } else {
            binding.budgetNameTextInputLayout.error = null
        }
        if(binding.budgetAmountEditText.text?.isEmpty() == true) {
            binding.budgetAmountTextInputLayout.error = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.amount_label))
            budgetViewModel.amountError = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.amount_label))
        } else if(Helper.checkAmountIsZeroOrNot(binding.budgetAmountEditText.text.toString())) {
            binding.budgetAmountTextInputLayout.error = resources.getString(R.string.amount_can_not_be_zero)
            budgetViewModel.amountError = resources.getString(R.string.amount_can_not_be_zero)
        } else if(!Helper.validateAmount(binding.budgetAmountEditText.text.toString())) {
            binding.budgetAmountTextInputLayout.error = resources.getString(R.string.amount_format_message)
            budgetViewModel.amountError = resources.getString(R.string.amount_format_message)
        } else {
            binding.budgetAmountTextInputLayout.error = null
            budgetViewModel.amountError = ""
        }
        if(binding.budgetCategoryEditText.text?.isEmpty() == true) {
            binding.budgetCategoryTextInputLayout.error = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.category))
        } else {
            binding.budgetCategoryTextInputLayout.error = null
        }

        return if(binding.budgetNameEditText.text?.isEmpty() == true || Helper.validateName(binding.budgetNameEditText.text.toString()) || Helper.checkAmountIsZeroOrNot(binding.budgetAmountEditText.text.toString())) {
            false
        } else if(binding.budgetAmountEditText.text?.isEmpty() == true) {
            false
        } else if(!Helper.validateAmount(binding.budgetAmountEditText.text.toString())) {
            false
        } else if(binding.budgetCategoryEditText.text?.isEmpty() == true) {
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