package com.example.spendwise.adapter

import android.content.res.Configuration
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.spendwise.Helper
import com.example.spendwise.R
import com.example.spendwise.domain.Budget
import com.example.spendwise.fragment.BudgetFragment
import com.example.spendwise.fragment.ConfigurationFragment
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.math.BigDecimal

class BudgetRecyclerViewAdapter(private val budgets: List<Pair<Budget, BigDecimal>>): Adapter<BudgetRecyclerViewAdapter.ViewHolder>() {

    var onItemClick: ((Pair<Budget, BigDecimal>) -> Unit)? = null
    private lateinit var fragment: Fragment

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val budgetName: TextView = itemView.findViewById(R.id.budgetNameText)
        val budgetAmount: TextView = itemView.findViewById(R.id.budgetAmountText)
        val spentAmount: TextView = itemView.findViewById(R.id.spentAmountText)
        val remainsText: TextView = itemView.findViewById(R.id.remainsText)
        val remainingAmount: TextView = itemView.findViewById(R.id.remainingAmountText)
        val remainingAmountCurrency: TextView = itemView.findViewById(R.id.remainingCurrencySymbol)
        val progressBar: LinearProgressIndicator = itemView.findViewById(R.id.budgetProgressBarRecycler)
        val budgetPercentText: TextView = itemView.findViewById(R.id.budgetPercentTextView)
        val divider: MaterialDivider = itemView.findViewById(R.id.budgetDivider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = (LayoutInflater.from(parent.context)).inflate(R.layout.budget_recycler_item, parent, false)
        /*if(fragment is BudgetFragment) {
            Log.e("BudgetAdapter", (fragment is BudgetFragment).toString())
            view.isClickable = false
            view.isFocusable = false
            view.background = null
        }*/
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = budgets.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.e("Budget", budgets.toString())
        val res = holder.itemView.resources
        if(position == budgets.lastIndex || res.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            holder.divider.visibility = View.GONE
        } else {
            holder.divider.visibility = View.VISIBLE
        }
        val item = budgets[position]
        holder.budgetName.text = item.first.category
        holder.budgetAmount.text = Helper.formatNumberToIndianStyle(item.first.maxAmount.toBigDecimal())//.toString()
        val percentage = (((item.second)/(item.first.maxAmount).toBigDecimal()) * BigDecimal(100)).toInt()
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
            holder.budgetPercentText.text = "${Helper.formatPercentage(percentage)}%"
            holder.budgetPercentText.setTextColor(res.getColor(R.color.white))
            holder.remainingAmount.setTextColor(res.getColor(R.color.progressIndicatorExceeded))
            holder.remainingAmount.text = Helper.formatNumberToIndianStyle(item.second.minus((item.first.maxAmount).toBigDecimal()))
            holder.remainsText.setTextColor(res.getColor(R.color.progressIndicatorExceeded))
            holder.remainsText.text = "Overspent"
            holder.remainingAmountCurrency.setTextColor(res.getColor(R.color.progressIndicatorExceeded))
        } else {
            holder.budgetPercentText.text = "$percentage% "
            holder.budgetPercentText.setTextColor(res.getColor(R.color.black))
//            holder.remainingAmount.setTextColor(res.getColor(R.color.black))
            holder.remainingAmount.text = Helper.formatNumberToIndianStyle(((item.first.maxAmount).toBigDecimal()).minus(item.second))
//            holder.remainsText.setTextColor(res.getColor(R.color.black))
            holder.remainsText.text = "Remains"
//            holder.remainingAmountCurrency.setTextColor(res.getColor(R.color.black))
        }
        holder.spentAmount.text = Helper.formatNumberToIndianStyle(item.second)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    fun setTheFragment(fragment: Fragment) {
        this.fragment = fragment
    }

}