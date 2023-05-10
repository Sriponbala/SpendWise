package com.example.spendwise.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.method.Touch.scrollTo
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.spendwise.Categories
import com.example.spendwise.Helper
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.databinding.FragmentViewRecordBinding
import com.example.spendwise.enums.RecordType
import com.example.spendwise.viewmodel.CategoryViewModel
import com.example.spendwise.viewmodel.RecordViewModel
import com.example.spendwise.viewmodel.RestoreScrollPositionViewModel
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory
import com.example.spendwise.viewmodelfactory.RestoreScrollPositionViewModelFactory

class ViewRecordFragment : Fragment() {

    private lateinit var binding: FragmentViewRecordBinding
    private lateinit var recordViewModel: RecordViewModel
    private val categoryViewModel : CategoryViewModel by activityViewModels()
    private lateinit var restoreScrollPositionViewModel: RestoreScrollPositionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = RecordViewModelFactory(requireActivity().application)
        recordViewModel = ViewModelProvider(requireActivity(), factory)[RecordViewModel::class.java]
        val restoreFactory = RestoreScrollPositionViewModelFactory(requireActivity().application)
        restoreScrollPositionViewModel = ViewModelProvider(this, restoreFactory)[RestoreScrollPositionViewModel::class.java]
        setHasOptionsMenu(true)
    }

   /* override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.alter_record_menu, menu)
    }*/

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.edit -> {
                editRecord()
            }
            R.id.delete -> {
                val alertMessage = resources.getString(R.string.deleteRecordAlert)
                showAlertDialog(alertMessage)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingInflatedId")
    private fun showAlertDialog(alertMessage: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.alert_text_view, null)
        val alertTextView = dialogView.findViewById<TextView>(R.id.alertTextView)
        alertTextView.text = alertMessage
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton("Delete") { _, _ ->
                deleteRecord()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentViewRecordBinding.inflate(inflater, container, false)
        binding.toolbarViewRecord.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.toolbarViewRecord.setOnMenuItemClickListener {
            onOptionsItemSelected(it)
        }
/*
        (activity as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back_arrow)
        }
*/
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        restoreScrollPositionViewModel.viewRecordScrollPosition.observe(viewLifecycleOwner, Observer {
            Log.e("Scroll", it.toString() + "observe")
            if (it != null) {
                binding.viewRecordRootScroll.scrollTo(0, it)
            }
        })

        Log.e("Coroutine", "record view record - ${recordViewModel.record.value}")
        recordViewModel.record.observe(viewLifecycleOwner, Observer {
            Log.e("Coroutine", "record view record - $it")
            if(it != null) {
                Log.e("Coroutine", "record not null view record - $it")
                Log.e("Test", "inside record observe in add Rec: rec - $it")
                categoryViewModel.category.value = Categories.categoryList.find {category ->
                    it.category == category.title
                }
                Log.e("Record", it.toString() + " view loaded" + it.recordId.toString())
                binding.viewAmountEditText.amountTvInComponent.text = Helper.formatNumberToIndianStyle(it.amount.toBigDecimal())//it.amount.toString()
                binding.viewCategoryEditText.text = it.category
                binding.viewDateEditText.text = Helper.formatDate(it.date)
                binding.viewNoteEditText.text = it.note.ifEmpty { "-" }
                if(it.description.isEmpty()) {
                    binding.viewEmptyDescription.visibility = View.VISIBLE
//                    binding.viewRecordDescriptionLabel.visibility = View.GONE
//                    binding.descriptionScrollView.visibility = View.GONE
                    binding.descriptionTextField.visibility = View.GONE
                } else {
                    binding.viewEmptyDescription.visibility = View.GONE
//                    binding.viewRecordDescriptionLabel.visibility = View.VISIBLE
//                    binding.descriptionScrollView.visibility = View.VISIBLE
                    binding.descriptionTextField.visibility = View.VISIBLE
                    binding.descriptionTextField.text = it.description
                }
                binding.viewRecordTypeEditText.text = it.type

                /*if(it.type == RecordType.INCOME.value) {
                    binding.incomeButtonView.setTextColor(resources.getColor(R.color.white))
                    binding.expenseButtonView.setTextColor(resources.getColor(R.color.gray))
                    binding.incomeButtonView.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                    binding.expenseButtonView.setBackgroundColor(resources.getColor(R.color.white))
                    binding.toggleGroupView.check(R.id.incomeButtonView)
                } else if (it.type == RecordType.EXPENSE.value) {
                    binding.incomeButtonView.setTextColor(resources.getColor(R.color.gray))
                    binding.expenseButtonView.setTextColor(resources.getColor(R.color.white))
                    binding.incomeButtonView.setBackgroundColor(resources.getColor(R.color.white))
                    binding.expenseButtonView.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                    binding.toggleGroupView.check(R.id.expenseButtonView)
                }*/
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

    override fun onStop() {
        super.onStop()
        categoryViewModel.category.value = null
    }

    override fun onPause() {

        super.onPause()
        Log.e("Scroll", "onPause")
        restoreScrollPositionViewModel.updateDashboardScrollPosition(binding.viewRecordRootScroll.scrollY)
    }

}