package com.example.spendwise.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.databinding.FragmentViewRecordBinding
import com.example.spendwise.enums.RecordType
import com.example.spendwise.viewmodel.RecordViewModel
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory

class ViewRecordFragment : Fragment() {

    private lateinit var binding: FragmentViewRecordBinding
    private lateinit var recordViewModel: RecordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = RecordViewModelFactory(requireActivity().application)
        recordViewModel = ViewModelProvider(requireActivity(), factory)[RecordViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.alter_record_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.edit -> {
                editRecord()
            }
            R.id.delete -> {
                deleteRecord()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentViewRecordBinding.inflate(inflater, container, false)

        (activity as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        recordViewModel.record.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                Log.e("Record", it.toString() + " view loaded" + it.recordId.toString())
                binding.viewAmountEditText.setText(it.amount.toString())
                binding.viewCategoryEditText.setText(it.category)
                binding.viewDateEditText.setText(it.date)
                binding.viewNoteEditText.setText(it.note)
                binding.descriptionTextField.setText(it.description)
                if(it.type == RecordType.INCOME.value) {
                    binding.toggleGroupView.check(R.id.incomeButtonView)
                } else if (it.type == RecordType.EXPENSE.value) {
                    binding.toggleGroupView.check(R.id.expenseButtonView)
                }
            }
        })
//        deleteRecord()
//        editRecord()
    }

    private fun deleteRecord() {
        val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
        recordViewModel.deleteRecord(userId)
        findNavController().popBackStack()
    }

    private fun editRecord() {
        val action = ViewRecordFragmentDirections.actionViewRecordFragmentToAddRecordFragment(isEditRecord = true)
        findNavController().navigate(action)
    }

}