package com.example.spendwise.fragment

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.databinding.FragmentAddRecordBinding
import androidx.lifecycle.Observer
import com.example.spendwise.Categories
import com.example.spendwise.Helper
import com.example.spendwise.enums.RecordType
import com.example.spendwise.viewmodel.CategoryViewModel
import com.example.spendwise.viewmodel.RecordViewModel
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory
import java.util.*

class AddRecordFragment : Fragment() {

    private lateinit var binding: FragmentAddRecordBinding
//    private lateinit var userViewModel: UserViewModel
    private lateinit var recordViewModel: RecordViewModel
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private lateinit var type: RecordType
    private lateinit var args: AddRecordFragmentArgs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*val userViewModelFactory = UserViewModelFactory((activity as MainActivity).application)
        userViewModel = ViewModelProvider(requireActivity(), userViewModelFactory)[UserViewModel::class.java]
        Log.e("UserViewModel AddRecord", userViewModel.toString())

        Log.e("User AddRecord", userViewModel.user.value?.userId.toString())*/
        val recordViewModelFactory = RecordViewModelFactory((activity as MainActivity).application)
        recordViewModel = ViewModelProvider(requireActivity(), recordViewModelFactory)[RecordViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        args = AddRecordFragmentArgs.fromBundle(requireArguments())
        (activity as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_close_24)
            title = if(args.isEditRecord) {
                "Edit record"
            } else "Add record"
        }
        binding = FragmentAddRecordBinding.inflate(inflater, container, false)
//        Helper.validateAmountField(binding.amountEditText)
        Helper.setupEditTextValidation(binding.amountEditText)
        /*userViewModel.user.value?.userId?.let {
            Log.e("UserId AddRecord", it.toString())
            recordViewModel.userId = it
        }*/
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(args.isEditRecord) {
            recordViewModel.record.observe(viewLifecycleOwner, Observer {
                if(it != null) {
                    if(it.type == RecordType.INCOME.value) {
                        binding.toggleGroup.check(R.id.incomeButton)
                    } else if(it.type == RecordType.EXPENSE.value) {
                        binding.toggleGroup.check(R.id.expenseButton)
                    }
                    Log.e("Amount", Helper.retrieveValueFromScientificNotation(it.amount))
                    binding.amountEditText.setText(it.amount.toString())
                    binding.dateEditText.setText(it.date)
                    binding.categoryEditText.setText(it.category)
                    binding.noteEditText.setText(it.note)
                    binding.descriptionTextField.setText(it.description)
                }
            })
        }

        binding.toggleGroup.check(R.id.incomeButton)
        binding.incomeButton.setTextColor(resources.getColor(R.color.white))
        binding.expenseButton.setTextColor(resources.getColor(R.color.gray))
        binding.incomeButton.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        binding.expenseButton.setBackgroundColor(resources.getColor(R.color.white))
        type = RecordType.INCOME

        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if(checkedId == R.id.incomeButton && isChecked) {
                binding.incomeButton.setTextColor(resources.getColor(R.color.white))
                binding.expenseButton.setTextColor(resources.getColor(R.color.gray))
                binding.incomeButton.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                binding.expenseButton.setBackgroundColor(resources.getColor(R.color.white))
                type = RecordType.INCOME
            } else if(checkedId == R.id.expenseButton && isChecked) {
                binding.incomeButton.setTextColor(resources.getColor(R.color.gray))
                binding.expenseButton.setTextColor(resources.getColor(R.color.white))
                binding.incomeButton.setBackgroundColor(resources.getColor(R.color.white))
                binding.expenseButton.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                type = RecordType.EXPENSE
            }
            binding.categoryEditText.text.clear()
        }


        binding.dateEditText.setOnClickListener {
            getInputDate()
        }

        binding.saveButton.setOnClickListener {
            if(args.isEditRecord) {
                updateRecord()
            } else addRecord()
        }
        categoryViewModel.category.observe(viewLifecycleOwner, Observer { category ->
            if(category != null) {
                binding.categoryEditText.setText(category.title)
                if(args.isEditRecord) {
                    Log.e("Record", "${args.isEditRecord} - edit - $category")
                    if(category.recordType == RecordType.INCOME) {
                        binding.toggleGroup.check(R.id.incomeButton)
                        binding.categoryEditText.setText(category.title)
                    } else{
                        binding.toggleGroup.check(R.id.expenseButton)
                        binding.categoryEditText.setText(category.title)
                    }
                }
                categoryViewModel.category.value = null
            }
        })

        binding.categoryEditText.setOnClickListener {
            val action = AddRecordFragmentDirections.actionAddRecordFragmentToCategoryFragment(type.value, R.id.addRecordFragment)
            findNavController().navigate(action)
        }

        val category = AddRecordFragmentArgs.fromBundle(requireArguments()).category
        binding.categoryEditText.setText(category)

    }

    private fun getInputDate() {

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this.requireContext(), {
                _, selectedYear, monthOfYear, dayOfMonth ->
            val date = "$dayOfMonth/${monthOfYear+1}/$selectedYear"
            binding.dateEditText.setText(date)
        }, year, month, day)
        datePickerDialog.show()
    }

    private fun addRecord() {
        if(validateAllFields()) {
            val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
           Log.e("userId add record", userId.toString())
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
            val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
            Log.e("userId edit record", userId.toString())
            recordViewModel.updateRecord(userId,
                binding.categoryEditText.text.toString(),
                binding.amountEditText.text.toString().toFloat(),
                type.value,
                binding.dateEditText.text.toString(),
                binding.noteEditText.text.toString(),
                binding.descriptionTextField.text.toString())
            moveToPreviousPage()
        }
    }

    private fun validateAllFields(): Boolean {
        return if(binding.amountEditText.text.isEmpty()) {
            binding.amountEditText.error = "Amount should not be empty"
//            Toast.makeText(this.requireContext(), "Amount should not be empty", Toast.LENGTH_SHORT).show()
            false
        } else if(!Helper.validateAmount(binding.amountEditText.text.toString())) {
            binding.amountEditText.error = "Amount should have min 1 digit before decimal, can have max 5 digits before decimal and max 2 digits after decimal"
//            Toast.makeText(this.requireContext(), "Amount should have min 1 digit before decimal, can have max 5 digits before decimal and max 2 digits after decimal", Toast.LENGTH_SHORT).show()
            false
        } else if(binding.categoryEditText.text.isEmpty()) {
            binding.categoryEditText.error = "Category should not be empty"
//            Toast.makeText(this.requireContext(), "Category should not be empty", Toast.LENGTH_SHORT).show()
            false
        } else if(binding.dateEditText.text.isEmpty()) {
            binding.dateEditText.error = "Date should not be empty"
//            Toast.makeText(this.requireContext(), "Date should not be empty", Toast.LENGTH_SHORT).show()
            false
        } else true
    }

    private fun moveToPreviousPage() {
        this.findNavController().popBackStack()
    }

  /*  private fun setCheckedButtonColor(checkedButton: Button) {
        checkedButton.setTextColor(resources.getColor(R.color.white))
    }*/

}