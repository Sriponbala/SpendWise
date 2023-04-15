package com.example.spendwise.adapter

import android.util.Log
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
        return ViewHolder(view).also {
            Log.e("Adapter", "ViewModel "+ it.toString())
        }
    }

    override fun getItemCount(): Int = SettingsOption.values().size.also {
        Log.e("Adapter", "size "+ it.toString())
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(SettingsOption.values()[position]) {
            SettingsOption.INCOME -> {
                Log.e("Adapter", "ViewModel income $position")
                holder.iconImageView.apply {
                    setImageResource(R.drawable.baseline_money_24)
                    setColorFilter(resources.getColor(R.color.black))
                }
                holder.iconTitle.text = SettingsOption.INCOME.text
            }
            SettingsOption.EXPENSE -> {
                Log.e("Adapter", "ViewModel expense $position")
                holder.iconImageView.apply {
                    setImageResource(R.drawable.expenses)
                    setColorFilter(resources.getColor(R.color.black))
                }
                holder.iconTitle.text = SettingsOption.EXPENSE.text
            }
            SettingsOption.FEEDBACK -> {
                Log.e("Adapter", "ViewModel feedback $position")
                holder.iconImageView.apply {
                    setImageResource(R.drawable.baseline_feedback_24)
                    setColorFilter(resources.getColor(R.color.black))
                }
                holder.iconTitle.text = SettingsOption.FEEDBACK.text
            }
            SettingsOption.CALL_SUPPORT -> {
                Log.e("Adapter", "ViewModel call support $position")
                holder.iconImageView.apply {
                    setImageResource(R.drawable.phone)
                    setColorFilter(resources.getColor(R.color.black))
                }
                holder.iconTitle.text = SettingsOption.CALL_SUPPORT.text
            }
        }
        holder.itemView.setOnClickListener{
            onItemClick?.invoke(SettingsOption.values()[position])
        }
    }
}