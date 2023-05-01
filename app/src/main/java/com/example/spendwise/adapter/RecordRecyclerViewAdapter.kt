package com.example.spendwise.adapter

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.spendwise.Helper
import com.example.spendwise.R
import com.example.spendwise.domain.Category
import com.example.spendwise.domain.Record
import com.example.spendwise.enums.Month
import com.example.spendwise.enums.RecordType
import com.example.spendwise.fragment.DashboardFragment
import com.google.android.material.divider.MaterialDivider
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

class RecordRecyclerViewAdapter(private val records: List<Record>, private val categories: List<Category>): RecyclerView.Adapter<RecordRecyclerViewAdapter.ViewHolder>() {

    private lateinit var colorStateList: ColorStateList
    var onItemClick: ((Record) -> Unit)? = null
    private lateinit var fragment: Fragment

    init {
        Log.e("UserID", "record adapter " + records.toString())
    }
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val categoryLogo: ImageView = itemView.findViewById(R.id.categoryLogo)
        val title: TextView = itemView.findViewById(R.id.noteTextView)
        val category: TextView = itemView.findViewById(R.id.categoryTextView)
        val amount: TextView = itemView.findViewById(R.id.amountTextView)
        val date: TextView = itemView.findViewById(R.id.dateTextView)
        val divider: MaterialDivider = itemView.findViewById(R.id.dividerRecordItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.record_list_item, parent, false)
        if (fragment is DashboardFragment) {
            view.isClickable = false
            view.isFocusable = false
            view.foreground = null
        }
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = records.size.also{
        println("Record size $it")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resources = holder.itemView.context.resources
        if(position == records.lastIndex) {
            holder.divider.visibility = View.GONE
        } else {
            holder.divider.visibility = View.VISIBLE
        }
        val record = records[position]
        Log.e("Display", record.toString())
        var category: Category? = null
        for(it in categories) {
            if(it.title == record.category) {
                category = it
                break
            }
        }
        Log.e("Color cat", "$category")
        val rupeeSymbol = resources.getString(R.string.rupee_symbol)
        if(record.type == RecordType.EXPENSE.value) {
            holder.amount.setTextColor(resources.getColor(R.color.expenseColour))
        } else if (record.type == RecordType.INCOME.value) {
            holder.amount.setTextColor(resources.getColor(R.color.incomeColour))
        }
        holder.amount.text = "$rupeeSymbol ${Helper.formatNumberToIndianStyle(record.amount)}"
        holder.category.text = record.category
        holder.title.text = record.note
        category?.logo?.let { holder.categoryLogo.setImageResource(it) }
        category?.bgColor?.let {
            Log.e("Color", "$it")
            colorStateList = ColorStateList.valueOf(resources.getColor(it))//ContextCompat.getColorStateList(holder.itemView.context, R.color.background_tint)!!
            Log.e("Color", colorStateList.toString())
            holder.categoryLogo.backgroundTintList = colorStateList
        }
        val date = parseDate(record.date)
        val monthName = Month.values()[date?.monthValue?.minus(1)!!].value
        val monthYear = "$monthName ${date.dayOfMonth}"
        holder.date.text = monthYear
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(records[position])
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun parseDate(dateString: String): LocalDate? {
        val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
        return try {
            LocalDate.parse(dateString, formatter)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    fun setTheFragment(fragment: Fragment) {
        this.fragment = fragment
    }

}