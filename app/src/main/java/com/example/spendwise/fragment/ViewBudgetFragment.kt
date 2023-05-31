package com.example.spendwise.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.spendwise.Helper
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.databinding.FragmentViewBudgetBinding
import com.example.spendwise.viewmodel.BudgetViewModel
import com.example.spendwise.viewmodel.RecordViewModel
import com.example.spendwise.viewmodelfactory.BudgetViewModelFactory
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory
import java.math.BigDecimal

class ViewBudgetFragment : Fragment() {

    private lateinit var binding: FragmentViewBudgetBinding
    private lateinit var budgetViewModel: BudgetViewModel
    private lateinit var recordViewModel: RecordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = BudgetViewModelFactory(requireActivity().application)
        budgetViewModel = ViewModelProvider(requireActivity(), factory)[BudgetViewModel::class.java]
        val recordViewModelFactory = RecordViewModelFactory(requireActivity().application)
        recordViewModel = ViewModelProvider(requireActivity(), recordViewModelFactory)[RecordViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.edit -> {
                editBudget()
            }
            R.id.delete -> {
                val alertMessage = resources.getString(R.string.deleteBudgetAlert)
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
                deleteBudget()
            }
            .setNegativeButton(resources.getString(R.string.cancel_button), null)
            .create()

        dialog.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewBudgetBinding.inflate(inflater, container, false)
        binding.toolbarViewBudget.apply {
            setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }
            setOnMenuItemClickListener {
                onOptionsItemSelected(it)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        budgetViewModel.budget.observe(viewLifecycleOwner) {
            if (it != null) {
                if (budgetViewModel.isBudgetUpdated) {
                    recordViewModel.filteredRecords.observe(
                        viewLifecycleOwner
                    ) { listOfRecords ->
                        if (listOfRecords != null) {
                            Log.e("List tag", listOfRecords.toString())
                            val categorizedList = listOfRecords.filter { r -> r.category == binding.includedViewBudgetComponent.viewBudgetCategoryText.text.toString() }
                            Log.e("List tag", categorizedList.toString())
                            if (listOfRecords.isEmpty()) {
                                budgetViewModel.updateBudgetItemRecordsAmount(BigDecimal(0))
                            } else {
                                var total = BigDecimal(0)
                                listOfRecords.forEach { record ->
                                    if (binding.includedViewBudgetComponent.viewBudgetCategoryText.text.toString() == record.category) {
                                        total += (record.amount).toBigDecimal()
                                    }
                                }
                                Log.e("Total", total.toString())
                                budgetViewModel.updateBudgetItemRecordsAmount(total)
                            }
                        } else {
                            budgetViewModel.updateBudgetItemRecordsAmount(BigDecimal(0))
                        }
                    }
                    budgetViewModel.budgetItem.observe(viewLifecycleOwner) { item ->
                        if (item != null) {
                            binding.includedViewBudgetComponent.viewBudgetNameText.text =
                                item.first.budgetName.ifEmpty { resources.getString(R.string.empty_data_fill_in_value) }
                            binding.includedViewBudgetComponent.viewBudgetAmountText.amountTvInComponent.text =
                                Helper.formatNumberToIndianStyle(item.first.maxAmount.toBigDecimal())
                            binding.includedViewBudgetComponent.viewBudgetCategoryText.text =
                                item.first.category
                            binding.includedViewBudgetComponent.viewBudgetAmountText.amountTvInComponent.text =
                                Helper.formatNumberToIndianStyle(item.first.maxAmount.toBigDecimal())
                            val spentAmt = item.second
                            val percentage =
                                (((item.second) / item.first.maxAmount.toBigDecimal()) * BigDecimal(
                                    100
                                )).toInt()
                            binding.includedViewBudgetComponent.viewBudgetProgressBarRecycler.progress =
                                percentage
                            if (percentage in 70..99) {
                                binding.includedViewBudgetComponent.viewBudgetProgressBarRecycler.apply {
                                    setIndicatorColor(resources.getColor(R.color.progressIndicatorDanger))
                                    trackColor =
                                        resources.getColor(R.color.progressBarNearingToBudget)
                                }
                                binding.includedViewBudgetComponent.budgetThresholdChip.visibility =
                                    View.VISIBLE
                                binding.includedViewBudgetComponent.budgetExceededChip.visibility =
                                    View.INVISIBLE
                                binding.includedViewBudgetComponent.budgetRemainingChip.visibility =
                                    View.INVISIBLE
                            } else if (percentage < 70) {
                                binding.includedViewBudgetComponent.viewBudgetProgressBarRecycler.apply {
                                    setIndicatorColor(resources.getColor(R.color.progressIndicatorLeisure))
                                    trackColor = resources.getColor(R.color.progressBarLeisure)
                                }
                                binding.includedViewBudgetComponent.budgetThresholdChip.visibility =
                                    View.INVISIBLE
                                binding.includedViewBudgetComponent.budgetExceededChip.visibility =
                                    View.INVISIBLE
                                binding.includedViewBudgetComponent.budgetRemainingChip.visibility =
                                    View.VISIBLE
                            } else if (percentage >= 100) {
                                binding.includedViewBudgetComponent.viewBudgetProgressBarRecycler.apply {
                                    setIndicatorColor(resources.getColor(R.color.progressIndicatorExceeded))
                                }
                                binding.includedViewBudgetComponent.budgetThresholdChip.visibility =
                                    View.INVISIBLE
                                binding.includedViewBudgetComponent.budgetExceededChip.visibility =
                                    View.VISIBLE
                                binding.includedViewBudgetComponent.budgetRemainingChip.visibility =
                                    View.INVISIBLE
                            }

                            if (percentage >= 100) {
                                binding.includedViewBudgetComponent.viewBudgetPercentTextView.text =
                                    resources.getString(R.string.percent_format, Helper.formatPercentage(percentage), resources.getString(R.string.percentage_symbol))
                                   // "${Helper.formatPercentage(percentage)}%"
                                binding.includedViewBudgetComponent.viewRemainingAmountText.amountTvInComponent.text =
                                    Helper.formatNumberToIndianStyle(spentAmt.minus(it.maxAmount.toBigDecimal()))
                                binding.includedViewBudgetComponent.viewRemainsText.text =
                                    resources.getString(R.string.overspent_label)
                                    //"Overspent"
                            } else {
                                binding.includedViewBudgetComponent.viewBudgetPercentTextView.text =
                                    resources.getString(R.string.percent_format, percentage, resources.getString(R.string.percentage_symbol))
                                    //"$percentage% "
                                binding.includedViewBudgetComponent.viewRemainingAmountText.amountTvInComponent.text =
                                    Helper.formatNumberToIndianStyle(
                                        it.maxAmount.toBigDecimal().minus(spentAmt)
                                    )
                                binding.includedViewBudgetComponent.viewRemainsText.text = resources.getString(R.string.remains_recycler_item) //"Remains"
                            }
                            binding.includedViewBudgetComponent.viewSpentAmountText.amountTvInComponent.text =
                                Helper.formatNumberToIndianStyle(spentAmt)
                        }

                    }
                    budgetViewModel.isBudgetUpdated = false
                } else {
                    budgetViewModel.budgetItem.observe(viewLifecycleOwner) { item ->
                        if (item != null) {
                            binding.includedViewBudgetComponent.viewBudgetNameText.text =
                                item.first.budgetName.ifEmpty { resources.getString(R.string.empty_data_fill_in_value) }
                            binding.includedViewBudgetComponent.viewBudgetAmountText.amountTvInComponent.text =
                                Helper.formatNumberToIndianStyle(item.first.maxAmount.toBigDecimal())
                            binding.includedViewBudgetComponent.viewBudgetCategoryText.text =
                                item.first.category
                            binding.includedViewBudgetComponent.viewBudgetAmountText.amountTvInComponent.text =
                                Helper.formatNumberToIndianStyle(item.first.maxAmount.toBigDecimal())

                            val spentAmt = item.second
                            val percentage =
                                (((item.second) / item.first.maxAmount.toBigDecimal()) * BigDecimal(
                                    100
                                )).toInt()
                            binding.includedViewBudgetComponent.viewBudgetProgressBarRecycler.progress =
                                percentage
                            if (percentage in 70..99) {
                                binding.includedViewBudgetComponent.viewBudgetProgressBarRecycler.apply {
                                    setIndicatorColor(resources.getColor(R.color.progressIndicatorDanger))
                                    trackColor =
                                        resources.getColor(R.color.progressBarNearingToBudget)
                                }
                                binding.includedViewBudgetComponent.budgetThresholdChip.visibility =
                                    View.VISIBLE
                                binding.includedViewBudgetComponent.budgetExceededChip.visibility =
                                    View.INVISIBLE
                                binding.includedViewBudgetComponent.budgetRemainingChip.visibility =
                                    View.INVISIBLE

                            } else if (percentage < 70) {
                                binding.includedViewBudgetComponent.viewBudgetProgressBarRecycler.apply {
                                    setIndicatorColor(resources.getColor(R.color.progressIndicatorLeisure))
                                    trackColor = resources.getColor(R.color.progressBarLeisure)
                                }
                                binding.includedViewBudgetComponent.budgetThresholdChip.visibility =
                                    View.INVISIBLE
                                binding.includedViewBudgetComponent.budgetExceededChip.visibility =
                                    View.INVISIBLE
                                binding.includedViewBudgetComponent.budgetRemainingChip.visibility =
                                    View.VISIBLE
                            } else if (percentage >= 100) {
                                binding.includedViewBudgetComponent.viewBudgetProgressBarRecycler.apply {
                                    setIndicatorColor(resources.getColor(R.color.progressIndicatorExceeded))
                                }
                                binding.includedViewBudgetComponent.budgetThresholdChip.visibility =
                                    View.INVISIBLE
                                binding.includedViewBudgetComponent.budgetExceededChip.visibility =
                                    View.VISIBLE
                                binding.includedViewBudgetComponent.budgetRemainingChip.visibility =
                                    View.INVISIBLE
                            }

                            if (percentage >= 100) {
                                binding.includedViewBudgetComponent.viewBudgetPercentTextView.text =
                                    resources.getString(R.string.percent_format, Helper.formatPercentage(percentage), resources.getString(R.string.percentage_symbol))
                                   // "${Helper.formatPercentage(percentage)}%"
                                binding.includedViewBudgetComponent.viewRemainingAmountText.amountTvInComponent.text =
                                    Helper.formatNumberToIndianStyle(spentAmt.minus(it.maxAmount.toBigDecimal()))
                                binding.includedViewBudgetComponent.viewRemainsText.text =
                                    resources.getString(R.string.overspent_label)
                            } else {
                                binding.includedViewBudgetComponent.viewBudgetPercentTextView.text =
                                    resources.getString(R.string.percent_format, percentage, resources.getString(R.string.percentage_symbol))
                                  //  "$percentage%"
                                binding.includedViewBudgetComponent.viewRemainingAmountText.amountTvInComponent.text =
                                    Helper.formatNumberToIndianStyle(
                                        it.maxAmount.toBigDecimal().minus(spentAmt)
                                    )
                                binding.includedViewBudgetComponent.viewRemainsText.text = resources.getString(R.string.remains_recycler_item)
                            }
                            binding.includedViewBudgetComponent.viewSpentAmountText.amountTvInComponent.text =
                                Helper.formatNumberToIndianStyle(spentAmt)
                        }

                    }
                }

            }
        }
    }

    private fun deleteBudget() {
        val userId = (activity as MainActivity).getSharedPreferences(resources.getString(R.string.loginStatus), Context.MODE_PRIVATE).getInt(resources.getString(R.string.userId), 0)
        budgetViewModel.deleteBudget(userId)
        findNavController().popBackStack()
    }

    private fun editBudget() {
        val action = ViewBudgetFragmentDirections.actionViewBudgetFragmentToAddBudgetFragment(isEditBudget = true)
        findNavController().navigate(action)
    }


}