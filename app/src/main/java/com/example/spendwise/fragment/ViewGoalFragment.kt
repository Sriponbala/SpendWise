package com.example.spendwise.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.spendwise.Helper
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.databinding.FragmentViewGoalBinding
import com.example.spendwise.enums.GoalStatus
import com.example.spendwise.viewmodel.GoalViewModel
import com.example.spendwise.viewmodelfactory.GoalViewModelFactory
import com.google.android.material.textfield.TextInputEditText

class ViewGoalFragment : Fragment() {

    private lateinit var binding: FragmentViewGoalBinding
    private lateinit var goalViewModel: GoalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = GoalViewModelFactory(requireActivity().application)
        goalViewModel = ViewModelProvider(requireActivity(), factory)[GoalViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (activity as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back_arrow)
        }
        binding = FragmentViewGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun deleteBudget() {
        val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
        goalViewModel.deleteGoal(userId)
        findNavController().popBackStack()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.alter_record_menu, menu)
        goalViewModel.menu = menu
        goalViewModel.goal.value?.let {
            if(it.goalStatus == GoalStatus.ACTIVE.value) {
                val editItem = menu.findItem(R.id.edit)
                editItem.isVisible = true
            } else if(it.goalStatus == GoalStatus.COMPLETED.value) {
                val editItem = menu.findItem(R.id.edit)
                editItem.isVisible = false
            }
        }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        goalViewModel.goal.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                Log.e("Goal", it.toString() + " view loaded" + it.goalName.toString())
                binding.imageViewGoal.apply {
                    setImageResource(it.goalIcon)
                    backgroundTintList = ColorStateList.valueOf(resources.getColor(it.goalColor))
                    setColorFilter(resources.getColor(R.color.white))
                }
                binding.titleTextViewGoal.text = it.goalName
                binding.subTextViewDate.text = if(it.desiredDate == "") "No Target Date" else "Target Date ${it.desiredDate}"
                val percent = ((it.savedAmount)/(it.targetAmount) * 100).toInt()
                binding.percentageCircularProgress.text = "$percent %"
                binding.circularProgressIndicator.progress = percent
                binding.targetAmtTileT.setText("₹ ${Helper.formatNumberToIndianStyle(it.targetAmount)}")
                binding.savedAmtTileT.setText("₹ ${Helper.formatNumberToIndianStyle(it.savedAmount)}")
                if(it.goalStatus == GoalStatus.ACTIVE.value) {
                    binding.addSavedAmtButton.visibility = View.VISIBLE
                    binding.setGoalAsReachedBtn.visibility = View.VISIBLE
                } else if(it.goalStatus == GoalStatus.COMPLETED.value) {
                    binding.addSavedAmtButton.visibility = View.GONE
                    binding.setGoalAsReachedBtn.visibility = View.GONE
                }
            }
        })

        binding.addSavedAmtButton.setOnClickListener {
            showAddSavedAmountDialog()
        }

        binding.setGoalAsReachedBtn.setOnClickListener {
            goalViewModel.updateGoalStatus(GoalStatus.COMPLETED.value)
            binding.addSavedAmtButton.visibility = View.GONE
            binding.setGoalAsReachedBtn.visibility = View.GONE
//            binding.percentageCircularProgress.text = "100 %"
//            binding.circularProgressIndicator.progress = 100
            /*goalViewModel.goal.value?.savedAmount?.let {amt ->
                binding.savedAmtTileT.setText("₹ ${Helper.formatNumberToIndianStyle(amt)}")
            }*/
            val editItem = goalViewModel.menu?.findItem(R.id.edit)
            editItem?.isVisible = false
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showAddSavedAmountDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.add_saved_amount_dialog, null)
        val addSavedAmtEt = dialogView.findViewById<TextInputEditText>(R.id.savedAmtTileTInput)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton("INSERT") { _, _ ->
                goalViewModel.goal.value?.let {
                    if(!addSavedAmtEt.text.isNullOrEmpty()) {
                        val amt = addSavedAmtEt.text.toString().toFloat()
                        val savedAmt = it.savedAmount + amt
                        goalViewModel.updateGoal(userId = it.userId, goalName = it.goalName, targetAmount = it.targetAmount, savedAmount = savedAmt, goalColor = it.goalColor, goalIcon = it.goalIcon, desiredDate = it.desiredDate)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun editBudget() {
        val action = ViewGoalFragmentDirections.actionViewGoalFragmentToAddGoalFragment(isEditGoal = true)
        findNavController().navigate(action)
    }

}