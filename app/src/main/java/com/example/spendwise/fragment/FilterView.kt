package com.example.spendwise.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import com.example.spendwise.R
import com.example.spendwise.databinding.FilterLayoutBinding
import com.example.spendwise.enums.Month
import com.example.spendwise.interfaces.FilterViewDelegate
import com.example.spendwise.viewmodel.RecordViewModel
import kotlinx.coroutines.NonDisposableHandle.parent
import java.util.*

class FilterView(
    private val recordViewModel: RecordViewModel,
    private var binding: FilterLayoutBinding?,
    private var delegateImpl: FilterViewDelegate?
): AdapterView.OnItemSelectedListener {

    fun setMonthYearValue() {
        val month = recordViewModel.month.value.also {
            Log.e("Landscape", "filterview - month - $it")
        }
        val year = recordViewModel.year.value.also {
            Log.e("Landscape", "filterview - year - $it")
        }
        if(month != null && year != null) {
            val monthName = Month.values()[month-1].value
            binding?.monthAndYearTv?.text = "$monthName $year"
        }
    }

    fun showMonthYearPicker(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.month_and_year_picker, null)
        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.month_picker)
        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.value = Calendar.getInstance().get(Calendar.MONTH) + 1

        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.year_picker)
        yearPicker.minValue = 1900
        yearPicker.maxValue = 2100
        yearPicker.value = Calendar.getInstance().get(Calendar.YEAR)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val month = monthPicker.value
                val year = yearPicker.value
                /*val monthName = Month.values()[month-1].value
                binding?.monthAndYearTv?.text = "$monthName $year"*/
                delegateImpl?.intimateSelectedDate(month, year)
                setMonthYearValue()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    fun createAndSetAdapter(context: Context, arrayRes: Int) {
        ArrayAdapter.createFromResource(context, arrayRes, R.layout.record_type_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding?.spinner?.adapter = adapter
        }

        binding?.spinner?.onItemSelectedListener = this //filterView//this
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selectedOption = parent?.getItemAtPosition(position) as String
        if (view != null) {
            (parent.getChildAt(0) as TextView).apply{
                setTextColor(view.resources.getColor(R.color.black))
                TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(view.resources.getColor(R.color.black)))
            }
        }
        delegateImpl?.intimateSelectedRecordType(selectedOption)
    }

    override fun onNothingSelected(view: AdapterView<*>?) {
        if (view != null) {
            (view.getChildAt(0) as TextView).apply{
                setTextColor(view.resources.getColor(R.color.black))
                TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(view.resources.getColor(R.color.black)))
            }
        }
    }

    fun clear() {
        delegateImpl = null
        binding = null
    }

}