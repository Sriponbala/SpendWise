package com.example.spendwise.adapter

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.spendwise.Helper
import com.example.spendwise.R
import com.example.spendwise.domain.Category
import com.example.spendwise.domain.Record
import com.example.spendwise.enums.Month
import com.example.spendwise.enums.RecordType
import com.example.spendwise.fragment.ViewBudgetFragment
import com.google.android.material.divider.MaterialDivider
import java.util.*

class RecordRecyclerViewAdapter(private val records: List<Record>, private val categories: List<Category>): RecyclerView.Adapter<RecordRecyclerViewAdapter.ViewHolder>() {

    private lateinit var colorStateList: ColorStateList
    var onItemClick: ((Record) -> Unit)? = null
//    var onItemClickRecordsFragment: ((Record, Int) -> Unit)? = null
    private lateinit var fragment: Fragment
    private val calendar = Calendar.getInstance()

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
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = records.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resources = holder.itemView.context.resources

        if(records.lastIndex == position || resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            holder.divider.visibility = View.GONE
        } else {
            holder.divider.visibility = View.VISIBLE
        }
        val record = records[position]
        var category: Category? = null
        for(it in categories) {
            if(it.title == record.category) {
                category = it
                break
            }
        }
        if(record.type == RecordType.EXPENSE.value) {
            holder.amount.setTextColor(resources.getColor(R.color.expenseColour))
        } else if (record.type == RecordType.INCOME.value) {
            holder.amount.setTextColor(resources.getColor(R.color.incomeColour))
        }
        holder.amount.text = resources.getString(R.string.amount_format, Helper.formatNumberToIndianStyle((record.amount).toBigDecimal())) //"$rupeeSymbol ${Helper.formatNumberToIndianStyle((record.amount).toBigDecimal())}"
        holder.category.text = record.category
        holder.title.text = record.note
        category?.logo?.let { holder.categoryLogo.setImageResource(it)
        holder.categoryLogo.setColorFilter(resources.getColor(R.color.logoColor), PorterDuff.Mode.SRC_ATOP)
        }
        category?.bgColor?.let {
            colorStateList = ColorStateList.valueOf(resources.getColor(it))
            holder.categoryLogo.backgroundTintList = colorStateList
        }
        calendar.time = record.date
        val monthName = Month.values()[calendar.get(Calendar.MONTH)].value
        val monthYear = "$monthName ${calendar.get(Calendar.DAY_OF_MONTH)}"
        holder.date.text = monthYear
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(records[position])
//            onItemClickRecordsFragment?.invoke(records[position], position)
        }
    }

    fun setTheFragment(fragment: Fragment) {
        this.fragment = fragment
    }

}