package com.example.spendwise.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.spendwise.Categories
import com.example.spendwise.Helper
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.databinding.FragmentViewRecordBinding
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
            .setPositiveButton(resources.getString(R.string.delete)) { _, _ ->
                deleteRecord()
            }
            .setNegativeButton(resources.getString(R.string.cancel_button), null)
            .create()

        dialog.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewRecordBinding.inflate(inflater, container, false)
        binding.toolbarViewRecord.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.toolbarViewRecord.setOnMenuItemClickListener {
            onOptionsItemSelected(it)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        restoreScrollPositionViewModel.viewRecordScrollPosition.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.viewRecordRootScroll.scrollTo(0, it)
            }
        }

        recordViewModel.record.observe(viewLifecycleOwner) {
            if (it != null) {
                categoryViewModel.category.value = Categories.categoryList.find { category ->
                    it.category == category.title
                }
                binding.viewAmountEditText.amountTvInComponent.text =
                    Helper.formatNumberToIndianStyle(it.amount.toBigDecimal())
                binding.viewCategoryEditText.text = it.category
                binding.viewDateEditText.text = Helper.formatDate(it.date)
                binding.viewNoteEditText.text = it.note
                if (it.description.isEmpty()) {
                    binding.viewEmptyDescription.visibility = View.VISIBLE
                    binding.descriptionTextField.visibility = View.GONE
                } else {
                    binding.viewEmptyDescription.visibility = View.GONE
                    binding.descriptionTextField.visibility = View.VISIBLE
                    binding.descriptionTextField.text = it.description
                }
                binding.viewRecordTypeEditText.text = it.type
            }
        }
    }

    private fun deleteRecord() {
        val userId = (activity as MainActivity).getSharedPreferences(resources.getString(R.string.loginStatus), Context.MODE_PRIVATE).getInt(resources.getString(R.string.userId), 0)
        recordViewModel.deleteRecord(userId)
        findNavController().popBackStack()
    }

    private fun editRecord() {
        recordViewModel.record.value?.let {
            val action = ViewRecordFragmentDirections.actionViewRecordFragmentToAddRecordFragment(isEditRecord = true, recordType = it.type)
            findNavController().navigate(action)
        }

    }

    override fun onStop() {
        super.onStop()
        categoryViewModel.category.value = null
    }

    override fun onPause() {
        super.onPause()
        restoreScrollPositionViewModel.updateDashboardScrollPosition(binding.viewRecordRootScroll.scrollY)
    }

}