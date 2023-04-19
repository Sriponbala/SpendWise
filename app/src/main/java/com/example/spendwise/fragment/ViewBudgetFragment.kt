package com.example.spendwise.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.alter_record_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.edit -> {
                editBudget()
            }
            R.id.delete -> {
                deleteBudget()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentViewBudgetBinding.inflate(inflater, container, false)
        (activity as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back_arrow)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        budgetViewModel.budget.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                Log.e("Budget", it.toString() + " view loaded" + it.budgetName.toString())
                binding.budgetNameView.setText(it.budgetName)
                binding.budgetAmountView.setText("₹ ${Helper.formatNumberToIndianStyle(it.maxAmount)}")
                binding.budgetPeriodView.setText(it.period)
                binding.budgetCategoryView.setText(it.category)
                binding.viewBudgetNameText.text = it.category
                binding.viewBudgetAmountText.text = Helper.formatNumberToIndianStyle(it.maxAmount)//.toString()
                var spentAmt = 0f
                val percentage = budgetViewModel.budgetItem.value?.let { item ->
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
                    binding.viewBudgetPercentTextView.text = "$percentage %"
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
            }
        })
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