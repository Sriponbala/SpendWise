package com.example.spendwise.fragment

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.databinding.FragmentAddRecordBinding
import com.example.spendwise.viewmodel.RecordViewModel
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory
import java.util.*

class AddRecordFragment : Fragment() {

    private lateinit var binding: FragmentAddRecordBinding
    private lateinit var recordViewModel: RecordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recordViewModelFactory = RecordViewModelFactory((activity as MainActivity).application)
        recordViewModel = ViewModelProvider(this, recordViewModelFactory)[RecordViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (activity as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        binding = FragmentAddRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dateEditText.setOnClickListener {
            getInputDate()
        }
        binding.saveButton.setOnClickListener {
            addRecord()
        }
    }

    private fun getInputDate() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this.requireContext(), {
                _, selectedYear, monthOfYear, dayOfMonth ->
            val date = "$dayOfMonth/$monthOfYear/$selectedYear"
            binding.dateEditText.setText(date)
        }, year, month, day)
        datePickerDialog.show()
    }

    private fun addRecord() {
        if(validateAllFields()) {
            val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
           //userid: Int, category: String, amount: String, type: String, date: String, _note: String, desc: String
            //,
            recordViewModel.insertRecord(userId,
                binding.categoryEditText.text.toString(),
            binding.amountEditText.text.toString(),
            "income",
            binding.dateEditText.text.toString(),
            binding.noteEditText.text.toString(),
            binding.descriptionTextField.text.toString())
            moveToPreviousPage()
        }
    }

    private fun validateAllFields(): Boolean {
        return if(binding.amountEditText.text.isEmpty()) {
            Toast.makeText(this.requireContext(), "Amount should not be empty", Toast.LENGTH_SHORT).show()
            false
        } else if(binding.categoryEditText.text.isEmpty()) {
            Toast.makeText(this.requireContext(), "Category should not be empty", Toast.LENGTH_SHORT).show()
            false
        } else if(binding.dateEditText.text.isEmpty()) {
            Toast.makeText(this.requireContext(), "Date should not be empty", Toast.LENGTH_SHORT).show()
            false
        } else true
    }

    private fun moveToPreviousPage() {
        this.findNavController().navigate(R.id.action_addRecordFragment_to_homePageFragment)
    }

}