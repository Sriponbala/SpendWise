package com.example.spendwise.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.example.spendwise.R
import com.example.spendwise.domain.IconItem

class GoalIconSpinnerAdapter(context: Context, goalIcons: List<IconItem>):
    ArrayAdapter<IconItem>(context, R.layout.grid_item_for_goal, goalIcons) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_item_for_goal, parent, false)
        val iconData = getItem(position)
        val imageViewIcon = view.findViewById<ImageView>(R.id.ivGridItem)
        iconData?.let {
            imageViewIcon.setImageResource(iconData.goalIcon)
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_item_for_goal, parent, false)
        val iconData = getItem(position)
        val imageViewIcon = view.findViewById<ImageView>(R.id.ivGridItem)
        iconData?.let {
            imageViewIcon.setImageResource(iconData.goalIcon)
        }
        return view
    }
}