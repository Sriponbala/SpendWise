package com.example.spendwise.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
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
import com.google.android.material.textfield.TextInputLayout
import java.math.BigDecimal

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
        /*(activity as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back_arrow)
        }*/
        binding = FragmentViewGoalBinding.inflate(inflater, container, false)
        binding.toolbarViewGoal.apply {
            setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }
            goalViewModel.menu = menu
            goalViewModel.goal.value?.let {
                if(it.goalStatus == GoalStatus.ACTIVE.value) {
                    val editItem = menu.findItem(R.id.edit)
                    editItem.isVisible = true
                } else if(it.goalStatus == GoalStatus.CLOSED.value) {
                    val editItem = menu.findItem(R.id.edit)
                    editItem.isVisible = false
                }
            }
            setOnMenuItemClickListener {
                onOptionsItemSelected(it)
            }
        }
        return binding.root
    }

    private fun deleteGoal() {
        val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
        goalViewModel.deleteGoal(userId)
        findNavController().popBackStack()
    }

    /*override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
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
    }*/

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.edit -> {
                editGoal()
            }
            R.id.delete -> {
                val alertMessage = resources.getString(R.string.deleteGoalAlert)
                showAlertDialog(alertMessage)
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
                binding.titleTextViewGoal.text = it.goalName.ifEmpty { "-" }
                binding.subTextViewDate.text = if(it.desiredDate == "") "No Target Date" else "Target Date ${it.desiredDate}"
                val percent = ((it.savedAmount.toBigDecimal())/(it.targetAmount.toBigDecimal()) * BigDecimal(100)).toInt()
                Log.e("Coroutine", """savedamt - ${it.savedAmount}
                    |targetamt - ${it.targetAmount}
                    |percent - $percent
                """.trimMargin())
                binding.percentageCircularProgress.text = "${Helper.formatPercentage(percent)}%"
                binding.circularProgressIndicator.progress = percent
                binding.goalStatusText.text = it.goalStatus
                binding.targetAmtTileT.text = "₹ ${Helper.formatNumberToIndianStyle(it.targetAmount.toBigDecimal())}"
                binding.savedAmtTileT.text = "₹ ${Helper.formatNumberToIndianStyle(it.savedAmount.toBigDecimal())}"
                if(it.goalStatus == GoalStatus.ACTIVE.value) {
                    val savedAmt = BigDecimal(it.savedAmount)
                    val targetAmt = BigDecimal(it.targetAmount)
                    if(savedAmt >= targetAmt) {
                        goalViewModel.updateGoalStatus()
                        binding.addSavedAmtButton.visibility = View.GONE
                        binding.setGoalAsReachedBtn.visibility = View.GONE
                        val editItem = goalViewModel.menu?.findItem(R.id.edit)
                        editItem?.isVisible = false
                    } else {
                        binding.addSavedAmtButton.visibility = View.VISIBLE
                        binding.setGoalAsReachedBtn.visibility = View.VISIBLE
                    }
//                    binding.addSavedAmtButton.visibility = View.VISIBLE
                    binding.setGoalAsReachedBtn.visibility = View.VISIBLE
                } else if(it.goalStatus == GoalStatus.CLOSED.value) {
                    binding.addSavedAmtButton.visibility = View.GONE
                    binding.setGoalAsReachedBtn.visibility = View.GONE
                }
            }
        })

        binding.addSavedAmtButton.setOnClickListener {
            showAddSavedAmountDialog()
        }

        binding.setGoalAsReachedBtn.setOnClickListener {
            goalViewModel.updateGoalStatus()
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
        val addSavedAmtTil = dialogView.findViewById<TextInputLayout>(R.id.addSavedAmtTil)
        val addSavedAmtEt = dialogView.findViewById<TextInputEditText>(R.id.savedAmtTileTInput)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton("Add", null) // set positive button to null initially
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        addSavedAmtEt.addTextChangedListener {
            if(it != null && it.isNotEmpty()) {
                /*if(addSavedAmtEt.text?.isEmpty() == true) {
                    addSavedAmtTil.error = "Target amount should not be empty"
                } else if(Helper.checkAmountIsZeroOrNot(addSavedAmtEt.text.toString())) {
                    addSavedAmtTil.error = "Target amount can not be zero"
                } else if(!Helper.validateGoalAmount(addSavedAmtEt.text.toString())) {
                    addSavedAmtTil.error = "Should have 1 to 10 digits before decimal, 0 to 2 digits after decimal & decimal not mandatory"//"Target amount should have 1 to 5 digits before decimal and 0 to 2 digits after decimal"
                } else {
                    addSavedAmtTil.error = null
                }*/
                addSavedAmtTil.error = null
            } else {
                addSavedAmtTil.error = null
            }
        }
        // Get a reference to the positive button
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        // Set a click listener on the positive button
        positiveButton.setOnClickListener {
            goalViewModel.goal.value?.let {
                if (!addSavedAmtEt.text.isNullOrEmpty()) {
                    if(Helper.checkAmountIsZeroOrNot(addSavedAmtEt.text.toString())) {
                        addSavedAmtTil.error = "Amount can not be zero"
                    } else if (!Helper.validateGoalAmount(addSavedAmtEt.text.toString())) {
                        addSavedAmtTil.error =
                            "Should have 1 to 10 digits before decimal, 0 to 2 digits after decimal & decimal not mandatory"
                    } else {
                        addSavedAmtTil.error = null
                        val amt = addSavedAmtEt.text.toString().trim().toBigDecimal()
                        val savedAmt = it.savedAmount.toBigDecimal() + amt
                        goalViewModel.updateGoal(
                            userId = it.userId,
                            goalName = it.goalName,
                            targetAmount = it.targetAmount,
                            savedAmount = savedAmt.toString(),
                            goalColor = it.goalColor,
                            goalIcon = it.goalIcon,
                            desiredDate = it.desiredDate
                        )
                        dialog.dismiss() // dismiss dialog on success
                    }
                } else  {
                    addSavedAmtTil.error = "Amount should not be empty"
                }
            }
        }
    }


/*
    @SuppressLint("MissingInflatedId")
    private fun showAddSavedAmountDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.add_saved_amount_dialog, null)
        val addSavedAmtEt = dialogView.findViewById<TextInputEditText>(R.id.savedAmtTileTInput)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                goalViewModel.goal.value?.let {
                    if(!addSavedAmtEt.text.isNullOrEmpty()) {
                        if(!Helper.validateAmount(addSavedAmtEt.text.toString())) {
                            addSavedAmtEt.error = "Target amount should have min 1 digit before decimal, can have max 5 digits before decimal and max 2 digits after decimal"
                            setCancelable(false)
                            return@setPositiveButton
                        } else {
                            addSavedAmtEt.error = null
                            val amt = addSavedAmtEt.text.toString().toFloat()
                            val savedAmt = it.savedAmount + amt
                            goalViewModel.updateGoal(userId = it.userId, goalName = it.goalName, targetAmount = it.targetAmount, savedAmount = savedAmt, goalColor = it.goalColor, goalIcon = it.goalIcon, desiredDate = it.desiredDate)
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
*/

    @SuppressLint("MissingInflatedId")
    private fun showAlertDialog(alertMessage: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.alert_text_view, null)
        val alertTextView = dialogView.findViewById<TextView>(R.id.alertTextView)
        alertTextView.text = alertMessage
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton("Delete") { _, _ ->
                deleteGoal()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun editGoal() {
        val action = ViewGoalFragmentDirections.actionViewGoalFragmentToAddGoalFragment(isEditGoal = true)
        findNavController().navigate(action)
    }

}