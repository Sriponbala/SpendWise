package com.example.spendwise.adapter

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.RecyclerView
import com.example.spendwise.Helper
import com.example.spendwise.R
import com.example.spendwise.domain.Budget
import com.example.spendwise.domain.Goal
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.math.BigDecimal

class GoalAdapter(private val goals: List<Goal>): RecyclerView.Adapter<GoalAdapter.ViewHolder>() {

    var onItemClick: ((Goal) -> Unit)? = null

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val goalName: TextView = itemView.findViewById(R.id.goalNameTextView)
        val targetAmount: TextView = itemView.findViewById(R.id.targetAmountTextView)
        val desiredDate: TextView = itemView.findViewById(R.id.desiredDateTextView)
        val goalIcon: ImageView = itemView.findViewById(R.id.goalIcon)
        val savedAmount: TextView = itemView.findViewById(R.id.savedGoalAmtTv)
        val progressBar: LinearProgressIndicator = itemView.findViewById(R.id.goalProgressLinear)
        val divider: MaterialDivider = itemView.findViewById(R.id.dividerGoalItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = (LayoutInflater.from(parent.context)).inflate(R.layout.goal_item_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = goals.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.e("Goal", goals.toString())
        val res = holder.itemView.resources
        if(goals.lastIndex == position || res.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            /*val marginLayoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
            marginLayoutParams.bottomMargin = 64
            holder.itemView.layoutParams = marginLayoutParams*/
            holder.divider.visibility = View.GONE
        } else {
            holder.divider.visibility = View.VISIBLE
        }
        val item = goals[position]
        holder.goalName.text = item.goalName.ifEmpty { "-" }
        holder.desiredDate.text = if(item.desiredDate == "") "No Target Date" else item.desiredDate
        holder.targetAmount.text = "₹ ${Helper.formatNumberToIndianStyle(item.targetAmount.toBigDecimal())}"
        holder.savedAmount.text = "₹ ${Helper.formatNumberToIndianStyle(item.savedAmount.toBigDecimal())}"
        holder.goalIcon.apply{
            Log.e("Goal", "${item.goalName} - icon - ${item.goalIcon}")
            setImageResource(item.goalIcon)
            Log.e("Goal", "${item.goalName} - color - ${item.goalColor}")
            backgroundTintList = ColorStateList.valueOf(res.getColor(item.goalColor))
            /*setColorFilter(res.getColor(R.color.white))*/ }
        holder.progressBar.apply {
            try{
                progress = ((item.savedAmount.toBigDecimal()/item.targetAmount.toBigDecimal()) * BigDecimal(100)).toInt()
            } catch (e: Exception) {
                Log.e("Coroutine", e.message.toString() + " ${item.savedAmount}")
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }
}