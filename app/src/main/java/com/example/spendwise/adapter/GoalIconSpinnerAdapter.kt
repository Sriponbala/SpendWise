package com.example.spendwise.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.spendwise.R
import com.example.spendwise.domain.ColorItem
import com.example.spendwise.domain.IconItem

class GoalIconSpinnerAdapter(context: Context, private val goalIcons: List<IconItem>):
    ArrayAdapter<IconItem>(context, R.layout.grid_item_for_goal, goalIcons) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Use the custom layout for the Spinner item in the selected view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_item_for_goal, parent, false)
        val iconData = getItem(position)
        val imageViewIcon = view.findViewById<ImageView>(R.id.ivGridItem)
        // Set the icon data
        iconData?.let {
            imageViewIcon.setImageResource(iconData.goalIcon)
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Use the custom layout for the Spinner item in the drop-down view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_item_for_goal, parent, false)
        val iconData = getItem(position)
        val imageViewIcon = view.findViewById<ImageView>(R.id.ivGridItem)
        // Set the icon data
        iconData?.let {
            imageViewIcon.setImageResource(iconData.goalIcon)
        }
        return view
    }
}