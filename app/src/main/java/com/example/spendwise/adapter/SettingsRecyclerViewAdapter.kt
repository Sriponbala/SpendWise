package com.example.spendwise.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spendwise.R
import com.example.spendwise.enums.SettingsOption

class SettingsRecyclerViewAdapter: RecyclerView.Adapter<SettingsRecyclerViewAdapter.ViewHolder>() {

    var onItemClick: ((SettingsOption) -> Unit)? = null

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val iconImageView: ImageView = itemView.findViewById(R.id.settingsOptionIcon)
        val iconTitle: TextView = itemView.findViewById(R.id.iconTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.settings_recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = SettingsOption.values().size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(SettingsOption.values()[position]) {
            SettingsOption.INCOME -> {
                holder.iconImageView.apply {
                    setImageResource(R.drawable.baseline_money_24)
                }
                holder.iconTitle.text = SettingsOption.INCOME.text
            }
            SettingsOption.EXPENSE -> {
                holder.iconImageView.apply {
                    setImageResource(R.drawable.expenses)
                }
                holder.iconTitle.text = SettingsOption.EXPENSE.text
            }
            SettingsOption.FEEDBACK -> {
                holder.iconImageView.apply {
                    setImageResource(R.drawable.baseline_feedback_24)
                }
                holder.iconTitle.text = SettingsOption.FEEDBACK.text
            }
            SettingsOption.CALL_SUPPORT -> {
                holder.iconImageView.apply {
                    setImageResource(R.drawable.phone)
                }
                holder.iconTitle.text = SettingsOption.CALL_SUPPORT.text
            }
        }
        holder.itemView.setOnClickListener{
            onItemClick?.invoke(SettingsOption.values()[position])
        }
    }
}