package com.example.spendwise.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.spendwise.Helper
import com.example.spendwise.R
import com.example.spendwise.domain.Budget
import com.google.android.material.progressindicator.LinearProgressIndicator

class BudgetRecyclerViewAdapter(private val budgets: List<Pair<Budget, Float>>): Adapter<BudgetRecyclerViewAdapter.ViewHolder>() {

    var onItemClick: ((Pair<Budget,Float>) -> Unit)? = null

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val budgetName: TextView = itemView.findViewById(R.id.budgetNameText)
        val budgetAmount: TextView = itemView.findViewById(R.id.budgetAmountText)
        val spentAmount: TextView = itemView.findViewById(R.id.spentAmountText)
        val remainsText: TextView = itemView.findViewById(R.id.remainsText)
        val remainingAmount: TextView = itemView.findViewById(R.id.remainingAmountText)
        val remainingAmountCurrency: TextView = itemView.findViewById(R.id.remainingCurrencySymbol)
        val progressBar: LinearProgressIndicator = itemView.findViewById(R.id.budgetProgressBarRecycler)
        val budgetPercentText: TextView = itemView.findViewById(R.id.budgetPercentTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = (LayoutInflater.from(parent.context)).inflate(R.layout.budget_recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = budgets.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.e("Budget", budgets.toString())
        val res = holder.itemView.resources
        val item = budgets[position]
        holder.budgetName.text = item.first.category
        holder.budgetAmount.text = Helper.formatNumberToIndianStyle(item.first.maxAmount)//.toString()
        val percentage = (((item.second)/item.first.maxAmount) * 100).toInt()
        holder.progressBar.progress = percentage
//        holder.progressBar.trackCornerRadius
        if(percentage in 70..99) {
            holder.progressBar.apply {
                setIndicatorColor(res.getColor(R.color.progressIndicatorDanger))
                trackColor = resources.getColor(R.color.progressBarNearingToBudget)
            }
        } else if (percentage < 70) {
            holder.progressBar.apply {
                setIndicatorColor(res.getColor(R.color.progressIndicatorLeisure))
                trackColor = resources.getColor(R.color.progressBarLeisure)
            }
//            holder.progressBar.setIndicatorColor(res.getColor(R.color.colorPrimary))
        } else if(percentage >= 100) {
            holder.progressBar.apply {
                setIndicatorColor(res.getColor(R.color.progressIndicatorExceeded))
            }
//            holder.progressBar.setIndicatorColor(res.getColor(R.color.health_color))
        }

        if(percentage >= 100) {
            holder.budgetPercentText.text = "$percentage %"
            holder.budgetPercentText.setTextColor(res.getColor(R.color.white))
            holder.remainingAmount.setTextColor(res.getColor(R.color.progressIndicatorExceeded))
            holder.remainingAmount.text = Helper.formatNumberToIndianStyle(item.second.minus(item.first.maxAmount))
            holder.remainsText.setTextColor(res.getColor(R.color.progressIndicatorExceeded))
            holder.remainsText.text = "Overspent"
            holder.remainingAmountCurrency.setTextColor(res.getColor(R.color.progressIndicatorExceeded))
        } else {
            holder.budgetPercentText.text = "$percentage % "
            holder.budgetPercentText.setTextColor(res.getColor(R.color.black))
//            holder.remainingAmount.setTextColor(res.getColor(R.color.black))
            holder.remainingAmount.text = Helper.formatNumberToIndianStyle(item.first.maxAmount.minus(item.second))
//            holder.remainsText.setTextColor(res.getColor(R.color.black))
            holder.remainsText.text = "Remains"
//            holder.remainingAmountCurrency.setTextColor(res.getColor(R.color.black))
        }
        holder.spentAmount.text = Helper.formatNumberToIndianStyle(item.second)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

}