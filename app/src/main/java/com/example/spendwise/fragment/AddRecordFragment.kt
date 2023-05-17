package com.example.spendwise.fragment

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.databinding.FragmentAddRecordBinding
import com.example.spendwise.Helper
import com.example.spendwise.enums.RecordType
import com.example.spendwise.viewmodel.CategoryViewModel
import com.example.spendwise.viewmodel.RecordViewModel
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory
import java.util.*

class AddRecordFragment : Fragment() {

    private lateinit var binding: FragmentAddRecordBinding
    private lateinit var recordViewModel: RecordViewModel
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private lateinit var type: RecordType
    private lateinit var args: AddRecordFragmentArgs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recordViewModelFactory = RecordViewModelFactory((activity as MainActivity).application)
        recordViewModel = ViewModelProvider(requireActivity(), recordViewModelFactory)[RecordViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddRecordBinding.inflate(inflater, container, false)
        args = AddRecordFragmentArgs.fromBundle(requireArguments())
        binding.toolbarAddRecord.apply {
            title = if(args.isEditRecord) {
                resources.getString(R.string.two_strings_concate, resources.getString(R.string.edit), args.recordType)
            } else {
                resources.getString(R.string.two_strings_concate, resources.getString(R.string.add), args.recordType)
            }
        }
        binding.toolbarAddRecord.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(args.isEditRecord && savedInstanceState == null) {
            if(recordViewModel.isTempDataSet) {
                recordViewModel.tempData.observe(viewLifecycleOwner) {
                    if (it != null) {
                        binding.amountEditText.setText(it[resources.getString(R.string.amount_label)])
                        binding.dateEditText.setText(it[resources.getString(R.string.date_label)])
                        binding.noteEditText.setText(it[resources.getString(R.string.title_label)])
                        binding.descriptionTextField.setText(it[resources.getString(R.string.description_label)])
                        recordViewModel.tempData.value = null
                    }
                }
            } else {
                recordViewModel.record.observe(viewLifecycleOwner) {
                    if (it != null) {
                        if (it.type == RecordType.INCOME.value) {
                            binding.toolbarAddRecord.title = resources.getString(R.string.two_strings_concate, resources.getString(R.string.edit), resources.getString(R.string.income_label))
                        } else if (it.type == RecordType.EXPENSE.value) {
                            binding.toolbarAddRecord.title = resources.getString(R.string.two_strings_concate, resources.getString(R.string.edit), resources.getString(R.string.expense_label))
                        }
                        binding.amountEditText.setText(Helper.formatDecimal(it.amount.toBigDecimal()))
                        binding.dateEditText.setText(Helper.formatDate(it.date))
                        binding.categoryEditText.setText(it.category)
                        binding.noteEditText.setText(it.note)
                        binding.descriptionTextField.setText(it.description)
                    }
                }
            }

        }
        type = if(args.recordType == RecordType.INCOME.value) RecordType.INCOME else RecordType.EXPENSE

        binding.dateEditText.setOnClickListener {
            hideInputMethod(it as EditText)
            getInputDate()
        }

        binding.saveButton.setOnClickListener {
            if(args.isEditRecord) {
                recordViewModel.isTempDataSet = false
                updateRecord()
            } else addRecord()
        }
        categoryViewModel.category.observe(viewLifecycleOwner) { category ->
            if (category != null) {
                categoryViewModel.queryText = ""
                binding.categoryEditText.setText(category.title)
                if (args.isEditRecord) {
                    binding.categoryEditText.setText(category.title)
                }
                categoryViewModel.category.value = null
            }
        }

        binding.categoryEditText.setOnClickListener {
            if(args.isEditRecord) {
                recordViewModel.isTempDataSet = true
                recordViewModel.tempData.value = mapOf(resources.getString(R.string.amount_label) to binding.amountEditText.text.toString(), resources.getString(R.string.date_label) to binding.dateEditText.text.toString(), resources.getString(R.string.title_label) to binding.noteEditText.text.toString(), resources.getString(R.string.description_label) to binding.descriptionTextField.text.toString())
            }
            val action = AddRecordFragmentDirections.actionAddRecordFragmentToCategoryFragment(type.value, R.id.addRecordFragment)
            findNavController().navigate(action)
        }

        val category = AddRecordFragmentArgs.fromBundle(requireArguments()).category
        binding.categoryEditText.setText(category)

        binding.amountEditText.addTextChangedListener {
            if(it != null && it.toString().isNotEmpty()) {
                if(recordViewModel.amountError.isNotEmpty()) {
                    if(binding.amountEditText.text?.isEmpty() == true) {
                        binding.amountTextInputLayoutRecord.error = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.amount_label))
                        recordViewModel.amountError = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.amount_label))
                    } else if(Helper.checkAmountIsZeroOrNot(binding.amountEditText.text.toString())) {
                        binding.amountTextInputLayoutRecord.error = resources.getString(R.string.amount_can_not_be_zero)
                        recordViewModel.amountError = resources.getString(R.string.amount_can_not_be_zero)
                    } else if (!Helper.validateAmount(binding.amountEditText.text.toString())) {
                        binding.amountTextInputLayoutRecord.error = resources.getString(R.string.amount_format_message)
                        recordViewModel.amountError = resources.getString(R.string.amount_format_message)
                    } else {
                        binding.amountTextInputLayoutRecord.error = null
                        recordViewModel.amountError = ""
                    }
                } else {
                    binding.amountTextInputLayoutRecord.error = null
                }
            }
        }
        binding.categoryEditText.addTextChangedListener {
            if(it != null && it.toString().isNotEmpty()) {
                binding.categoryTextInputLayoutRecord.error = null
            }
        }
        binding.dateEditText.addTextChangedListener {
            if(it != null && it.toString().isNotEmpty()) {
                binding.dateTextInputLayoutRecord.error = null
            }
        }
        binding.noteEditText.addTextChangedListener {
            if(it != null && it.toString().isNotEmpty()) {
                if(recordViewModel.titleError.isNotEmpty()) {
                    if(Helper.validateName(binding.noteEditText.text.toString())){
                        binding.noteTextInputLayoutRecord.error = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.title_label))
                        recordViewModel.titleError = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.title_label))
                    } else {
                        binding.noteTextInputLayoutRecord.error = null
                        recordViewModel.titleError = ""
                    }
                } else {
                    binding.noteTextInputLayoutRecord.error = null
                }
            }
        }
    }

    private fun getInputDate() {

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this.requireContext(), {
                _, selectedYear, monthOfYear, dayOfMonth ->
            val date = Helper.formatDate(Helper.getDate("$dayOfMonth-${monthOfYear+1}-$selectedYear"))
            binding.dateEditText.setText(date)
        }, year, month, day)
        datePickerDialog.show()
    }

    private fun addRecord() {
        if(validateAllFields()) {
            val userId = (activity as MainActivity).getSharedPreferences(resources.getString(R.string.loginStatus), Context.MODE_PRIVATE).getInt(resources.getString(R.string.userId), 0)
            recordViewModel.insertRecord(userId,
                binding.categoryEditText.text.toString(),
            binding.amountEditText.text.toString(),
            type.value,
            binding.dateEditText.text.toString(),
            binding.noteEditText.text.toString(),
            binding.descriptionTextField.text.toString())
            moveToPreviousPage()
        }
    }

    private fun updateRecord() {
        if(validateAllFields()) {
            val userId = (activity as MainActivity).getSharedPreferences(resources.getString(R.string.loginStatus), Context.MODE_PRIVATE).getInt(resources.getString(R.string.userId), 0)
            recordViewModel.updateRecord(userId,
                binding.categoryEditText.text.toString(),
                binding.amountEditText.text.toString(),
                type.value,
                binding.dateEditText.text.toString(),
                binding.noteEditText.text.toString(),
                binding.descriptionTextField.text.toString())
            moveToPreviousPage()
        }
    }

    private fun validateAllFields(): Boolean {
        if(binding.noteEditText.text?.isEmpty() == true) {
            binding.noteTextInputLayoutRecord.error = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.title_label))
        } else if(Helper.validateName(binding.noteEditText.text.toString())){
            binding.noteTextInputLayoutRecord.error = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.title_label))
            recordViewModel.titleError = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.title_label))
        } else {
            binding.noteTextInputLayoutRecord.error = null
        }
        if(binding.amountEditText.text?.isEmpty() == true) {
            binding.amountTextInputLayoutRecord.error = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.amount_label))
            recordViewModel.amountError = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.amount_label))
        } else if(Helper.checkAmountIsZeroOrNot(binding.amountEditText.text.toString())) {
            binding.amountTextInputLayoutRecord.error = resources.getString(R.string.amount_can_not_be_zero)
            recordViewModel.amountError = resources.getString(R.string.amount_can_not_be_zero)
        } else if (!Helper.validateAmount(binding.amountEditText.text.toString())) {
            binding.amountTextInputLayoutRecord.error = resources.getString(R.string.amount_format_message)
            recordViewModel.amountError = resources.getString(R.string.amount_format_message)
        } else {
            binding.amountTextInputLayoutRecord.error = null
            recordViewModel.amountError = ""
        }
        if(binding.categoryEditText.text?.isEmpty() == true) {
            binding.categoryTextInputLayoutRecord.error = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.category))
            binding.categoryTextInputLayoutRecord.helperText = null
        } else {
            binding.categoryTextInputLayoutRecord.error = null
        }
        if(binding.dateEditText.text?.isEmpty() == true) {
            binding.dateTextInputLayoutRecord.error = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.date_label))
            binding.dateTextInputLayoutRecord.helperText = null
        } else {
            binding.dateTextInputLayoutRecord.error = null
        }

        return if(binding.noteEditText.text?.isEmpty() == true || Helper.validateName(binding.noteEditText.text.toString())) {
            false
        } else if(binding.amountEditText.text?.isEmpty() == true || Helper.checkAmountIsZeroOrNot(binding.amountEditText.text.toString())) {
            false
        } else if(!Helper.validateAmount(binding.amountEditText.text.toString())) {
            false
        } else if(binding.categoryEditText.text?.isEmpty() == true) {
            false
        } else binding.dateEditText.text?.isEmpty() != true
    }

    private fun hideInputMethod(view: EditText) {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view.windowToken,0)
    }

    private fun moveToPreviousPage() {
        this.findNavController().popBackStack()
    }

}