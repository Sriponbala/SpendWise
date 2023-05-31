package com.example.spendwise.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.spendwise.Helper
import com.example.spendwise.R
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
        val factory = GoalViewModelFactory(requireActivity().application, resources)
        goalViewModel = ViewModelProvider(requireActivity(), factory)[GoalViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
//        val userId = (activity as MainActivity).getSharedPreferences(resources.getString(R.string.loginStatus), Context.MODE_PRIVATE).getInt(resources.getString(R.string.userId), 0)
        goalViewModel.deleteGoal()
        findNavController().popBackStack()
    }

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
        goalViewModel.goal.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.imageViewGoal.apply {
                    setImageResource(it.goalIcon)
                    backgroundTintList = ColorStateList.valueOf(resources.getColor(it.goalColor))
                    setColorFilter(resources.getColor(R.color.white))
                }
                binding.titleTextViewGoal.text =
                    it.goalName.ifEmpty { resources.getString(R.string.empty_data_fill_in_value) }
                binding.subTextViewDate.text =
                    if (it.desiredDate == "") resources.getString(R.string.no_target_date_label) else resources.getString(
                        R.string.two_strings_concate,
                        resources.getString(R.string.target_date_label),
                        it.desiredDate
                    )
                val percent =
                    ((it.savedAmount.toBigDecimal()) / (it.targetAmount.toBigDecimal()) * BigDecimal(
                        100
                    )).toInt()
                val percentageSymbol = resources.getString(R.string.percentage_symbol)
                binding.percentageCircularProgress.text = resources.getString(
                    R.string.percent_format,
                    Helper.formatPercentage(percent),
                    percentageSymbol
                ) //"${Helper.formatPercentage(percent)}%"
                binding.circularProgressIndicator.progress = percent
                binding.goalStatusText.text = it.goalStatus
                binding.targetAmtTileT.text = resources.getString(
                    R.string.amount_format,
                    Helper.formatNumberToIndianStyle(it.targetAmount.toBigDecimal())
                ) // "₹ ${Helper.formatNumberToIndianStyle(it.targetAmount.toBigDecimal())}"
                binding.savedAmtTileT.text = resources.getString(
                    R.string.amount_format,
                    Helper.formatNumberToIndianStyle(it.savedAmount.toBigDecimal())
                ) //"₹ ${Helper.formatNumberToIndianStyle(it.savedAmount.toBigDecimal())}"
                if (it.goalStatus == GoalStatus.ACTIVE.value) {
                    val savedAmt = BigDecimal(it.savedAmount)
                    val targetAmt = BigDecimal(it.targetAmount)
                    if (savedAmt >= targetAmt) {
                        goalViewModel.updateGoalStatus()
                        binding.addSavedAmtButton.visibility = View.GONE
                        binding.setGoalAsReachedBtn.visibility = View.GONE
                        val editItem = goalViewModel.menu?.findItem(R.id.edit)
                        editItem?.isVisible = false
                    } else {
                        binding.addSavedAmtButton.visibility = View.VISIBLE
                        binding.setGoalAsReachedBtn.visibility = View.VISIBLE
                    }
                    binding.setGoalAsReachedBtn.visibility = View.VISIBLE
                } else if (it.goalStatus == GoalStatus.CLOSED.value) {
                    binding.addSavedAmtButton.visibility = View.GONE
                    binding.setGoalAsReachedBtn.visibility = View.GONE
                }
            }
        }

        binding.addSavedAmtButton.setOnClickListener {
            showAddSavedAmountDialog()
        }

        binding.setGoalAsReachedBtn.setOnClickListener {
            goalViewModel.updateGoalStatus()
            binding.addSavedAmtButton.visibility = View.GONE
            binding.setGoalAsReachedBtn.visibility = View.GONE
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
            .setPositiveButton(resources.getString(R.string.add), null)
            .setNegativeButton(resources.getString(R.string.cancel_button), null)
            .create()

        dialog.show()

        addSavedAmtEt.addTextChangedListener {
            if(it != null && it.isNotEmpty()) {
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
                        addSavedAmtTil.error = resources.getString(R.string.amount_can_not_be_zero)
                    } else if (!Helper.validateGoalAmount(addSavedAmtEt.text.toString())) {
                        addSavedAmtTil.error = resources.getString(R.string.amount_format_message)
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
                        dialog.dismiss()
                    }
                } else  {
                    addSavedAmtTil.error = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.amount_label))
                }
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showAlertDialog(alertMessage: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.alert_text_view, null)
        val alertTextView = dialogView.findViewById<TextView>(R.id.alertTextView)
        alertTextView.text = alertMessage
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton(resources.getString(R.string.delete)) { _, _ ->
                deleteGoal()
            }
            .setNegativeButton(resources.getString(R.string.cancel_button), null)
            .create()

        dialog.show()
    }

    private fun editGoal() {
        val action = ViewGoalFragmentDirections.actionViewGoalFragmentToAddGoalFragment(isEditGoal = true)
        findNavController().navigate(action)
    }


}