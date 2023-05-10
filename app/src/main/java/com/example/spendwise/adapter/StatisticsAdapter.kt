package com.example.spendwise.adapter

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.spendwise.Helper
import com.example.spendwise.R
import com.example.spendwise.domain.Category
import com.example.spendwise.enums.Month
import com.google.android.material.divider.MaterialDivider
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class StatisticsAdapter(private val records: List<Pair<Category, BigDecimal>>): RecyclerView.Adapter<StatisticsAdapter.ViewHolder>() {

    private var total = BigDecimal(0)
    var onItemClick: ((Pair<Category, BigDecimal>) -> Unit)? = null

    init {
        calculateTotal()
    }

    private fun calculateTotal() {
        records.forEach {
            total += it.second
        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val categoryBgColor: LinearLayout = itemView.findViewById(R.id.percentLinear)
        val percentText: TextView = itemView.findViewById(R.id.percentText)
        val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        val amount: TextView = itemView.findViewById(R.id.amountStats)
        val divider: MaterialDivider = itemView.findViewById(R.id.statsDivider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.stats_by_category_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = records.size.also{
        println("Stats record size $it")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resources = holder.itemView.context.resources
        val record = records[position]
        if(position == records.lastIndex || resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            holder.divider.visibility = View.GONE
        } else {
            holder.divider.visibility = View.VISIBLE
        }
        Log.e("Display", record.toString())
        val category = record.first
        holder.categoryBgColor.backgroundTintList = ColorStateList.valueOf(resources.getColor(category.bgColor))
        holder.categoryName.text = category.title
        holder.percentText.text = "${((record.second / total) * BigDecimal(100)).toInt()}"
        Log.e("Coroutine", "${((record.second / total) * BigDecimal(100)).toInt()}" +
                " ${record.second}, $total, ${(record.second / total)}, ${((record.second / total) * BigDecimal(100))}")
        holder.amount.text = Helper.formatNumberToIndianStyle(record.second)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(record)
        }
    }

}