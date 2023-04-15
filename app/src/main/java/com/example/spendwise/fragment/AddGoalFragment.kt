package com.example.spendwise.fragment

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.adapters.TextViewBindingAdapter.setText
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.spendwise.ColorsForSpinner
import com.example.spendwise.IconsForSpinner
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.adapter.ColorSpinnerAdapter
import com.example.spendwise.adapter.GoalIconSpinnerAdapter
import com.example.spendwise.databinding.FragmentAddBudgetBinding
import com.example.spendwise.databinding.FragmentAddGoalBinding
import com.example.spendwise.domain.ColorItem
import com.example.spendwise.domain.IconItem
import com.example.spendwise.enums.RecordType
import com.example.spendwise.viewmodel.BudgetViewModel
import com.example.spendwise.viewmodel.CategoryViewModel
import com.example.spendwise.viewmodel.GoalViewModel
import com.example.spendwise.viewmodel.RecordViewModel
import com.example.spendwise.viewmodelfactory.BudgetViewModelFactory
import com.example.spendwise.viewmodelfactory.GoalViewModelFactory
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory
import java.util.*

class AddGoalFragment : Fragment() {

    private lateinit var binding: FragmentAddGoalBinding
    private lateinit var goalViewModel: GoalViewModel
//    private lateinit var args: AddGoalFragmentArgs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = GoalViewModelFactory(requireActivity().application)
        goalViewModel = ViewModelProvider(requireActivity(), factory)[GoalViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        args = AddGoalFragmentArgs.fromBundle(requireArguments())
        (activity as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_close_24)
            /*title = if(args.isEditGoal) {
                "Edit Goal"
            } else "Add Goal"*/
        }
        // Inflate the layout for this fragment
        binding = FragmentAddGoalBinding.inflate(inflater, container, false)
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

        /*if(args.isEditGoal) {
            goalViewModel.goal.observe(viewLifecycleOwner, Observer {
                if(it != null) {
                    binding.etGoalName.setText(it.goalName)
                    binding.etGoalAmount.setText(it.targetAmount.toString())
                    val selectedIcon = IconsForSpinner.goalIcons.find { icon -> icon.goalIcon == it.goalIcon }
                    val iconIndex = IconsForSpinner.goalIcons.indexOf(selectedIcon)
                    binding.tilGoalColor.setSelection(iconIndex)

                    val selectedColor = ColorsForSpinner.colorsList.find { color -> color.color == it.goalColor}
                    val indexColor = ColorsForSpinner.colorsList.indexOf(selectedColor)
                    binding.tilGoalColor.setSelection(indexColor)

                    binding.etDesiredDate.setText(it.desiredDate)
                }
            })
        }*/

        binding.saveGoalButton.setOnClickListener {
            addGoal()
            /*if(args.isEditGoal) {
                updateGoal()
            } else addGoal()*/
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
            getInputDate()
        }

    }

    private fun addGoal() {
        if(validateAllFields()) {
            val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
            Log.e("userId add record", userId.toString())
            goalViewModel.insertGoal(
                userId = userId,
                goalName = binding.etGoalName.text.toString(),
                targetAmount = binding.etGoalAmount.text.toString().toFloat(),
                savedAmount = binding.etSavedGoalAmount.text.toString().toFloat(),
                goalColor = goalViewModel.goalColor,
                goalIcon = goalViewModel.goalIcon,
                desiredDate = binding.etDesiredDate.toString()
            )
            moveToPreviousPage()
        }
    }

    private fun updateGoal() {
        if(validateAllFields()) {
            val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
            Log.e("userId edit goal", userId.toString())
            goalViewModel.updateGoal(
                userId = userId,
                goalName = binding.etGoalName.text.toString(),
                targetAmount = binding.etGoalAmount.text.toString().toFloat(),
                savedAmount = binding.etSavedGoalAmount.text.toString().toFloat(),
                goalColor = goalViewModel.goalColor,
                goalIcon = goalViewModel.goalIcon,
                desiredDate = binding.etDesiredDate.toString()
            )
            moveToPreviousPage()
        }
    }

    private fun validateAllFields(): Boolean {
        return if(binding.etGoalName.text?.isEmpty() == true) {
            Toast.makeText(
                this.requireContext(),
                "Goal name should not be empty",
                Toast.LENGTH_SHORT
            ).show()
            false
        } else if(binding.etGoalAmount.text?.isEmpty() == true) {
            Toast.makeText(
                this.requireContext(),
                "Target amount should not be empty",
                Toast.LENGTH_SHORT
            ).show()
            false
        } else true
    }

    private fun getInputDate() {

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this.requireContext(), {
                _, selectedYear, monthOfYear, dayOfMonth ->
            val date = "$dayOfMonth/${monthOfYear+1}/$selectedYear"
            binding.etDesiredDate.setText(date)
        }, year, month, day)
        datePickerDialog.show()
    }

    private fun moveToPreviousPage() {
        this.findNavController().popBackStack()
    }
}