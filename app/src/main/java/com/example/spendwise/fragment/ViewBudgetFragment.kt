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
import com.example.spendwise.databinding.FragmentViewBudgetBinding
import com.example.spendwise.viewmodel.BudgetViewModel
import com.example.spendwise.viewmodel.RecordViewModel
import com.example.spendwise.viewmodelfactory.BudgetViewModelFactory
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory

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
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        budgetViewModel.budget.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                Log.e("Budget", it.toString() + " view loaded" + it.budgetName.toString())
                binding.budgetNameView.setText(it.budgetName)
                binding.budgetAmountView.setText(it.maxAmount.toString())
                binding.budgetPeriodView.setText(it.period)
                binding.budgetCategoryView.setText(it.category)
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