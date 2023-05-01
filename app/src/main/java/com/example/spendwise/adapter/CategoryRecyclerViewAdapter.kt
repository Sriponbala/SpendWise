package com.example.spendwise.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.spendwise.R
import com.example.spendwise.domain.Category
import com.example.spendwise.fragment.AddRecordFragmentDirections
import com.example.spendwise.fragment.CategoryFragment
import com.example.spendwise.fragment.CategoryFragmentArgs
import com.example.spendwise.fragment.CategoryFragmentDirections
import com.google.android.material.divider.MaterialDivider

class CategoryRecyclerViewAdapter(private var categories: List<Category>): RecyclerView.Adapter<CategoryRecyclerViewAdapter.ViewHolder>() {

    var onItemClick: ((Category) -> Unit)? = null
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val categoryImage: ImageView = itemView.findViewById(R.id.categoryImage)
        val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        val divider: MaterialDivider = itemView.findViewById(R.id.categoryItemDivider)
    }

    fun filterList(filteredList: List<Category>) {
        categories = filteredList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.category_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = categories.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.categoryImage.setImageResource(categories[position].logo)
        if(position == categories.lastIndex) {
            holder.divider.visibility = View.GONE
        } else {
            holder.divider.visibility = View.VISIBLE
        }
        val colorStateList = ColorStateList.valueOf(holder.itemView.resources.getColor(categories[position].bgColor))
        holder.categoryImage.backgroundTintList = colorStateList
        holder.categoryTextView.text = categories[position].title
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(categories[position])
        }
    }
}