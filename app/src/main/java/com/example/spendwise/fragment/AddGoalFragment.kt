package com.example.spendwise.fragment

import android.app.DatePickerDialog
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.spendwise.ColorsForSpinner
import com.example.spendwise.Helper
import com.example.spendwise.IconsForSpinner
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.adapter.ColorSpinnerAdapter
import com.example.spendwise.adapter.GoalIconSpinnerAdapter
import com.example.spendwise.databinding.FragmentAddGoalBinding
import com.example.spendwise.domain.ColorItem
import com.example.spendwise.domain.IconItem
import com.example.spendwise.viewmodel.GoalViewModel
import com.example.spendwise.viewmodelfactory.GoalViewModelFactory
import java.util.*

class AddGoalFragment : Fragment() {

    private lateinit var binding: FragmentAddGoalBinding
    private lateinit var goalViewModel: GoalViewModel
    private lateinit var args: AddGoalFragmentArgs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = GoalViewModelFactory(requireActivity().application, resources)
        goalViewModel = ViewModelProvider(requireActivity(), factory)[GoalViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddGoalBinding.inflate(inflater, container, false)
        args = AddGoalFragmentArgs.fromBundle(requireArguments())
        binding.toolbarAddGoal.apply {
            title = if(args.isEditGoal) {
                resources.getString(R.string.two_strings_concate, resources.getString(R.string.edit), resources.getString(R.string.goal_label))
            } else {
                resources.getString(R.string.two_strings_concate, resources.getString(R.string.add), resources.getString(R.string.goal_label))
            }
            setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }
        }
        return binding.root
    }

    private fun createAndSetAdapter() {
        ColorSpinnerAdapter(requireContext(), ColorsForSpinner.colorsList).also {
            binding.tilGoalColor.adapter = it
        }
        GoalIconSpinnerAdapter(requireContext(), IconsForSpinner.goalIcons).also {
            binding.tilGoalIcon.adapter = it
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createAndSetAdapter()

        if(args.isEditGoal && savedInstanceState == null) {
            goalViewModel.goal.observe(viewLifecycleOwner) {
                if (it != null) {
                    binding.etGoalName.setText(it.goalName)
                    binding.etGoalAmount.setText(Helper.formatDecimal(it.targetAmount.toBigDecimal()))
                    binding.etSavedGoalAmount.setText(Helper.formatDecimal(it.savedAmount.toBigDecimal()))
                    val selectedIcon =
                        IconsForSpinner.goalIcons.find { icon -> icon.goalIcon == it.goalIcon }
                    val iconIndex = IconsForSpinner.goalIcons.indexOf(selectedIcon)
                    binding.tilGoalColor.setSelection(iconIndex)

                    val selectedColor =
                        ColorsForSpinner.colorsList.find { color -> color.color == it.goalColor }
                    val indexColor = ColorsForSpinner.colorsList.indexOf(selectedColor)
                    binding.tilGoalColor.setSelection(indexColor)
                    binding.etDesiredDate.setText(it.desiredDate)
                }
            }
        }

        binding.saveGoalButton.setOnClickListener {
            if(args.isEditGoal) {
                updateGoal()
            } else addGoal()
        }

        binding.tilGoalColor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedValue = parent?.getItemAtPosition(position) as ColorItem
                goalViewModel.goalColor = selectedValue.color
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.tilGoalIcon.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedValue = parent?.getItemAtPosition(position) as IconItem
                goalViewModel.goalIcon = selectedValue.goalIcon
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.etDesiredDate.setOnClickListener {
            hideInputMethod(it as EditText)
            getInputDate()
        }

        binding.etGoalName.addTextChangedListener {
            if(it != null && it.toString().isNotEmpty()) {
                if(goalViewModel.goalNameError.isNotEmpty()) {
                    if(Helper.validateName(binding.etGoalName.text.toString())){
                        binding.tilGoalName.error = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.goal_name))
                        goalViewModel.goalNameError = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.goal_name))
                    } else {
                        binding.tilGoalName.error = null
                        goalViewModel.goalNameError = ""
                    }
                } else {
                    binding.tilGoalName.error = null
                }
//                binding.tilGoalName.error = null
            }
        }
        binding.etGoalAmount.addTextChangedListener {
            if(it != null && it.toString().isNotEmpty()) {
                if(goalViewModel.targetAmountError.isNotEmpty()) {
                    if(binding.etGoalAmount.text?.isEmpty() == true) {
                        binding.tilGoalAmount.error = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.amount_label))
                        goalViewModel.targetAmountError = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.amount_label))
                    } else if(Helper.checkAmountIsZeroOrNot(binding.etGoalAmount.text.toString())) {
                        binding.tilGoalAmount.error = resources.getString(R.string.amount_can_not_be_zero)
                        goalViewModel.targetAmountError = resources.getString(R.string.amount_can_not_be_zero)
                    } else if(!Helper.validateGoalAmount(binding.etGoalAmount.text.toString())) {
                        binding.tilGoalAmount.error = resources.getString(R.string.amount_format_message)
                        goalViewModel.targetAmountError = resources.getString(R.string.amount_format_message)
                    } else {
                        binding.tilGoalAmount.error = null
                        goalViewModel.targetAmountError = ""
                    }
                } else {
                    binding.tilGoalAmount.error = null
                }
            }
        }

        binding.etSavedGoalAmount.addTextChangedListener {
            if(it != null && it.toString().isNotEmpty()) {
                if(goalViewModel.savedAmountError.isNotEmpty()) {
                    if(binding.etSavedGoalAmount.text.toString() != "" && !Helper.validateGoalAmount(binding.etSavedGoalAmount.text.toString())) {
                        binding.tilSavedGoalAmount.error = resources.getString(R.string.saved_amount_format_message)
                        goalViewModel.savedAmountError = resources.getString(R.string.saved_amount_format_message)
                    } else {
                        binding.tilSavedGoalAmount.error = null
                        goalViewModel.savedAmountError = ""
                    }
                } else {
                    binding.tilSavedGoalAmount.error = null
                }
            }
        }
    }

    private fun addGoal() {
        if(validateAllFields()) {
            val userId = (activity as MainActivity).getSharedPreferences(resources.getString(R.string.loginStatus), Context.MODE_PRIVATE).getInt(resources.getString(R.string.userId), 0)
            goalViewModel.insertGoal(
                userId = userId,
                goalName = binding.etGoalName.text.toString(),
                targetAmount = binding.etGoalAmount.text.toString(),
                savedAmount = if(binding.etSavedGoalAmount.text.toString() == "") {
                    resources.getString(R.string.zero)
                } else { binding.etSavedGoalAmount.text.toString() },
                goalColor = goalViewModel.goalColor,
                goalIcon = goalViewModel.goalIcon,
                desiredDate = binding.etDesiredDate.text.toString()
            )
            moveToPreviousPage()
        }
    }

    private fun updateGoal() {
        if(validateAllFields()) {
            val userId = (activity as MainActivity).getSharedPreferences(resources.getString(R.string.loginStatus), Context.MODE_PRIVATE).getInt(resources.getString(R.string.userId), 0)
            goalViewModel.updateGoal(
                userId = userId,
                goalName = binding.etGoalName.text.toString(),
                targetAmount = binding.etGoalAmount.text.toString(),
                savedAmount = if(binding.etSavedGoalAmount.text.toString() == "") {
                    resources.getString(R.string.zero)
                } else { binding.etSavedGoalAmount.text.toString() },
                goalColor = goalViewModel.goalColor,
                goalIcon = goalViewModel.goalIcon,
                desiredDate = binding.etDesiredDate.text.toString()
            )
            moveToPreviousPage()
        }
    }

    private fun validateAllFields(): Boolean {
        if(binding.etGoalName.text?.isEmpty() == true) {
            binding.tilGoalName.error = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.goal_name))
        }  else if(Helper.validateName(binding.etGoalName.text.toString())){
            binding.tilGoalName.error = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.goal_name))
            goalViewModel.goalNameError = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.goal_name))
        } else {
            binding.tilGoalName.error = null
        }
        if(binding.etGoalAmount.text?.isEmpty() == true) {
            binding.tilGoalAmount.error = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.amount_label))
            goalViewModel.targetAmountError = resources.getString(R.string.should_not_be_empty_message, resources.getString(R.string.amount_label))
        } else if(Helper.checkAmountIsZeroOrNot(binding.etGoalAmount.text.toString())) {
            binding.tilGoalAmount.error = resources.getString(R.string.amount_can_not_be_zero)
            goalViewModel.targetAmountError = resources.getString(R.string.amount_can_not_be_zero)
        } else if(!Helper.validateGoalAmount(binding.etGoalAmount.text.toString())) {
            binding.tilGoalAmount.error = resources.getString(R.string.amount_format_message)
            goalViewModel.targetAmountError = resources.getString(R.string.amount_format_message)
        } else {
            binding.tilGoalAmount.error = null
            goalViewModel.targetAmountError = ""
        }
        if(binding.etSavedGoalAmount.text.toString() != "" && !Helper.validateGoalAmount(binding.etSavedGoalAmount.text.toString())) {
            binding.tilSavedGoalAmount.error = resources.getString(R.string.saved_amount_format_message)
            goalViewModel.savedAmountError = resources.getString(R.string.saved_amount_format_message)
        } else {
            binding.tilSavedGoalAmount.error = null
            goalViewModel.savedAmountError = ""
        }

        return if(binding.etGoalName.text?.isEmpty() == true || Helper.validateName(binding.etGoalName.text.toString())) {
            false
        }
        else if(binding.etGoalAmount.text?.isEmpty() == true || Helper.checkAmountIsZeroOrNot(binding.etGoalAmount.text.toString())) {
            false
        } else if(!Helper.validateGoalAmount(binding.etGoalAmount.text.toString())) {
            false
        } else !(binding.etSavedGoalAmount.text.toString() != "" && !Helper.validateGoalAmount(binding.etSavedGoalAmount.text.toString()))
    }

    private fun getInputDate() {

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this.requireContext(), {
                _, selectedYear, monthOfYear, dayOfMonth ->
            val date = Helper.formatDate(Helper.getDate("$dayOfMonth-${monthOfYear+1}-$selectedYear"))
            binding.etDesiredDate.setText(date)
        }, year, month, day)
        datePickerDialog.show()
    }

    private fun hideInputMethod(view: EditText) {
        val imm = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view.windowToken,0)
    }

    private fun moveToPreviousPage() {
        this.findNavController().popBackStack()
    }

    override fun onPause() {
        super.onPause()
        val actionBar = (activity as? MainActivity)?.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(false)
    }

}