package com.example.spendwise.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
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
import com.google.android.material.color.MaterialColors

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

/*    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.alter_record_menu, menu)
    }*/

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
            .setPositiveButton("Delete") { _, _ ->
                deleteBudget()
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
        binding = FragmentViewBudgetBinding.inflate(inflater, container, false)
        binding.toolbarViewBudget.apply {
            setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }
            setOnMenuItemClickListener {
                onOptionsItemSelected(it)
            }
        }

        /*(activity as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back_arrow)
        }*/
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        budgetViewModel.budget.observe(viewLifecycleOwner, Observer {
            Log.e("OverSpent", "budget obs $it")
            Log.e("ViewBudget", "budget obs in viewbudget")
            if (it != null) {
                if(budgetViewModel.isBudgetUpdated) {
                    Log.e("OverSpent", "isBudgetUpdat true $it")
                    recordViewModel.filteredRecords.observe(
                        viewLifecycleOwner,
                        Observer { listOfRecords ->
                            if (listOfRecords != null) {
                                Log.e("ViewBudget", "list rec not null")
                                if (listOfRecords.isEmpty()) {
                                    Log.e("ViewBudget", "list rec empty")
                                    budgetViewModel.updateBudgetItemRecordsAmount(0f)
                                } else {
                                    Log.e("ViewBudget", "list rec not empty")
                                    var total = 0f
                                    listOfRecords.forEach { record ->
                                        if (binding.budgetCategoryView.text.toString() == record.category) {
                                            total += record.amount
                                        }
                                    }
                                    Log.e(
                                        "ViewBudget",
                                        "before update budgetitem in add budget frag"
                                    )
                                    budgetViewModel.updateBudgetItemRecordsAmount(total)
                                }
                            } else {
                                budgetViewModel.updateBudgetItemRecordsAmount(0f)
                            }
                        })
                    budgetViewModel.budgetItem.observe(viewLifecycleOwner, Observer { item ->
                        Log.e("OverSpent", "budgetitem obs $item")
                        Log.e("ViewBudget", "${item} - budgetitem obs in viewbudget")
                        if(item != null) {
                            Log.e("ViewBudget", "${item.toString()} - updated budget")
                            Log.e("ViewBudget", it.toString() + " view loaded " + it.budgetName.toString())
                            binding.budgetNameView.text = item.first.budgetName
                            binding.budgetAmountView.text = "₹ ${Helper.formatNumberToIndianStyle(item.first.maxAmount)}"
                            binding.budgetPeriodView.text = item.first.period
                            binding.budgetCategoryView.text = item.first.category
                            binding.viewBudgetNameText.text = item.first.category
                            binding.viewBudgetAmountText.text = Helper.formatNumberToIndianStyle(item.first.maxAmount)//.toString()
                            val spentAmt = item.second
                            Log.e("ViewBudget", "${item.toString()} - updated budget item")
                            val percentage = (((item.second)/item.first.maxAmount) * 100).toInt()
                            binding.viewBudgetProgressBarRecycler.progress = percentage.also { percent ->
                                Log.e("Progress", percent.toString())
                            }
                            if(percentage in 70..99) {
                                binding.viewBudgetProgressBarRecycler.apply {
                                    setIndicatorColor(resources.getColor(R.color.progressIndicatorDanger))
                                    trackColor = resources.getColor(R.color.progressBarNearingToBudget)
                                }
                            } else if (percentage < 70) {
                                binding.viewBudgetProgressBarRecycler.apply {
                                    setIndicatorColor(resources.getColor(R.color.progressIndicatorLeisure))
                                    trackColor = resources.getColor(R.color.progressBarLeisure)
                                }
//            holder.progressBar.setIndicatorColor(res.getColor(R.color.colorPrimary))
                            } else if(percentage >= 100) {
                                binding.viewBudgetProgressBarRecycler.apply {
                                    setIndicatorColor(resources.getColor(R.color.progressIndicatorExceeded))
                                }
//            holder.progressBar.setIndicatorColor(res.getColor(R.color.health_color))
                            }

                            if(percentage >= 100) {
                                Log.e("OverSpent", "overspent colors $percentage")
                                binding.viewBudgetPercentTextView.text = "${Helper.formatPercentage(percentage)} %"
                                binding.viewBudgetPercentTextView.setTextColor(resources.getColor(R.color.white))
                                binding.viewRemainingAmountText.setTextColor(resources.getColor(R.color.progressIndicatorExceeded))
                                binding.viewRemainingAmountText.text = Helper.formatNumberToIndianStyle(spentAmt.minus(it.maxAmount))
                                binding.viewRemainsText.setTextColor(resources.getColor(R.color.progressIndicatorExceeded))
                                binding.viewRemainsText.text = "Overspent"
                                binding.remainingCurrencySymbol.setTextColor(resources.getColor(R.color.progressIndicatorExceeded))
                            } else {
                                Log.e("OverSpent", "remains colors $percentage")
                                binding.viewBudgetPercentTextView.text = "$percentage % "
                                binding.viewBudgetPercentTextView.setTextColor(resources.getColor(R.color.black))
//                    binding.viewRemainingAmountText.setTextColor(resources.getColor(R.color.black))
                                binding.viewRemainingAmountText.text = Helper.formatNumberToIndianStyle(it.maxAmount.minus(spentAmt))
//                    binding.viewRemainsText.setTextColor(resources.getColor(MaterialColors.getColor(view, com.google.android.material.R.attr.colorOnTertiaryContainer)))
                                binding.viewRemainsText.text = "Remains"
//                    binding.remainingCurrencySymbol.setTextColor(resources.getColor(R.color.black))
                            }
                            binding.viewSpentAmountText.text = Helper.formatNumberToIndianStyle(spentAmt)
                        }

                    })
                    budgetViewModel.isBudgetUpdated = false
                } else {
                    budgetViewModel.budgetItem.observe(viewLifecycleOwner, Observer { item ->
                        Log.e("ViewBudget", "${item} - budgetitem obs in viewbudget")
                        if(item != null) {
                            Log.e("ViewBudget", "${item.toString()} - updated budget")
                            Log.e("ViewBudget", it.toString() + " view loaded " + it.budgetName.toString())
                            binding.budgetNameView.text = item.first.budgetName
                            binding.budgetAmountView.text = "₹ ${Helper.formatNumberToIndianStyle(item.first.maxAmount)}"
                            binding.budgetPeriodView.text = item.first.period
                            binding.budgetCategoryView.text = item.first.category
                            binding.viewBudgetNameText.text = item.first.category
                            binding.viewBudgetAmountText.text = Helper.formatNumberToIndianStyle(item.first.maxAmount)//.toString()

                            val spentAmt = item.second
                            Log.e("ViewBudget", "${item.toString()} - updated budget item")
                            val percentage = (((item.second)/item.first.maxAmount) * 100).toInt()
                            binding.viewBudgetProgressBarRecycler.progress = percentage.also { percent ->
                                Log.e("Progress", percent.toString())
                            }
                            if(percentage in 70..99) {
                                binding.viewBudgetProgressBarRecycler.apply {
                                    setIndicatorColor(resources.getColor(R.color.progressIndicatorDanger))
                                    trackColor = resources.getColor(R.color.progressBarNearingToBudget)
                                }
                            } else if (percentage < 70) {
                                binding.viewBudgetProgressBarRecycler.apply {
                                    setIndicatorColor(resources.getColor(R.color.progressIndicatorLeisure))
                                    trackColor = resources.getColor(R.color.progressBarLeisure)
                                }
//            holder.progressBar.setIndicatorColor(res.getColor(R.color.colorPrimary))
                            } else if(percentage >= 100) {
                                binding.viewBudgetProgressBarRecycler.apply {
                                    setIndicatorColor(resources.getColor(R.color.progressIndicatorExceeded))
                                }
//            holder.progressBar.setIndicatorColor(res.getColor(R.color.health_color))
                            }

                            if(percentage >= 100) {
                                Log.e("OverSpent", "overspent colors $percentage")
                                binding.viewBudgetPercentTextView.text = "${Helper.formatPercentage(percentage)} %"
                                binding.viewBudgetPercentTextView.setTextColor(resources.getColor(R.color.white))
                                binding.viewRemainingAmountText.setTextColor(resources.getColor(R.color.progressIndicatorExceeded))
                                binding.viewRemainingAmountText.text = Helper.formatNumberToIndianStyle(spentAmt.minus(it.maxAmount))
                                binding.viewRemainsText.setTextColor(resources.getColor(R.color.progressIndicatorExceeded))
                                binding.viewRemainsText.text = "Overspent"
                                binding.remainingCurrencySymbol.setTextColor(resources.getColor(R.color.progressIndicatorExceeded))
                            } else {
                                Log.e("OverSpent", "remains colors $percentage")
                                binding.viewBudgetPercentTextView.text = "$percentage % "
                                binding.viewBudgetPercentTextView.setTextColor(resources.getColor(R.color.black))
//                    binding.viewRemainingAmountText.setTextColor(resources.getColor(R.color.black))
                                binding.viewRemainingAmountText.text = Helper.formatNumberToIndianStyle(it.maxAmount.minus(spentAmt))
                                binding.viewRemainsText.setTextColor(resources.getColor(R.color.textColor))
                                binding.viewRemainingAmountText.setTextColor(resources.getColor(R.color.textColor))
                                binding.remainingCurrencySymbol.setTextColor(resources.getColor(R.color.textColor))
//                    binding.viewRemainsText.setTextColor(resources.getColor(MaterialColors.getColor(view, com.google.android.material.R.attr.colorOnTertiaryContainer)))
                                binding.viewRemainsText.text = "Remains"
//                    binding.remainingCurrencySymbol.setTextColor(resources.getColor(R.color.black))
                            }
                            binding.viewSpentAmountText.text = Helper.formatNumberToIndianStyle(spentAmt)
                        }

                    })
                }

            }
        })
                /*Log.e("ViewBudget", "${it.toString()} - updated budget")
                Log.e("Budget", it.toString() + " view loaded" + it.budgetName.toString())
                binding.budgetNameView.text = it.budgetName
                binding.budgetAmountView.text = "₹ ${Helper.formatNumberToIndianStyle(it.maxAmount)}"
                binding.budgetPeriodView.text = it.period
                binding.budgetCategoryView.text = it.category
                binding.viewBudgetNameText.text = it.category
                binding.viewBudgetAmountText.text = Helper.formatNumberToIndianStyle(it.maxAmount)//.toString()
                var spentAmt = 0f
                val percentage = budgetViewModel.budgetItem.value?.let { item ->
                    Log.e("ViewBudget", "${item.toString()} - updated budget item")
                    spentAmt = item.second
                    (((item.second)/item.first.maxAmount) * 100).toInt()
                } ?: 0
                binding.viewBudgetProgressBarRecycler.progress = percentage.also { percent ->
                    Log.e("Progress", percent.toString())
                }
                if(percentage in 70..99) {
                    binding.viewBudgetProgressBarRecycler.apply {
                        setIndicatorColor(resources.getColor(R.color.progressIndicatorDanger))
                        trackColor = resources.getColor(R.color.progressBarNearingToBudget)
                    }
                } else if (percentage < 70) {
                    binding.viewBudgetProgressBarRecycler.apply {
                        setIndicatorColor(resources.getColor(R.color.progressIndicatorLeisure))
                        trackColor = resources.getColor(R.color.progressBarLeisure)
                    }
//            holder.progressBar.setIndicatorColor(res.getColor(R.color.colorPrimary))
                } else if(percentage >= 100) {
                    binding.viewBudgetProgressBarRecycler.apply {
                        setIndicatorColor(resources.getColor(R.color.progressIndicatorExceeded))
                    }
//            holder.progressBar.setIndicatorColor(res.getColor(R.color.health_color))
                }

                if(percentage >= 100) {
                    binding.viewBudgetPercentTextView.text = "${Helper.formatPercentage(percentage)} %"
                    binding.viewBudgetPercentTextView.setTextColor(resources.getColor(R.color.white))
                    binding.viewRemainingAmountText.setTextColor(resources.getColor(R.color.progressIndicatorExceeded))
                    binding.viewRemainingAmountText.text = Helper.formatNumberToIndianStyle(spentAmt.minus(it.maxAmount))
                    binding.viewRemainsText.setTextColor(resources.getColor(R.color.progressIndicatorExceeded))
                    binding.viewRemainsText.text = "Overspent"
                    binding.remainingCurrencySymbol.setTextColor(resources.getColor(R.color.progressIndicatorExceeded))
                } else {
                    binding.viewBudgetPercentTextView.text = "$percentage % "
                    binding.viewBudgetPercentTextView.setTextColor(resources.getColor(R.color.black))
//                    binding.viewRemainingAmountText.setTextColor(resources.getColor(R.color.black))
                    binding.viewRemainingAmountText.text = Helper.formatNumberToIndianStyle(it.maxAmount.minus(spentAmt))
//                    binding.viewRemainsText.setTextColor(resources.getColor(MaterialColors.getColor(view, com.google.android.material.R.attr.colorOnTertiaryContainer)))
                    binding.viewRemainsText.text = "Remains"
//                    binding.remainingCurrencySymbol.setTextColor(resources.getColor(R.color.black))
                }
                binding.viewSpentAmountText.text = Helper.formatNumberToIndianStyle(spentAmt)
            }*/

    }

    private fun deleteBudget() {
        val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
        budgetViewModel.deleteBudget(userId)
        findNavController().popBackStack()
    }

    private fun editBudget() {
        val action = ViewBudgetFragmentDirections.actionViewBudgetFragmentToAddBudgetFragment(isEditBudget = true)
        findNavController().navigate(action)
    }

}