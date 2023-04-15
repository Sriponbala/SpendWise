package com.example.spendwise.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.spendwise.R
import com.example.spendwise.domain.ColorItem

class ColorSpinnerAdapter(context: Context, private val colorItems: List<ColorItem>) :
    ArrayAdapter<ColorItem>(context, R.layout.colour_item_for_goal, colorItems) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.colour_item_for_goal, parent, false)
        val colorItem = colorItems[position]
        val viewColorRectangle = view.findViewById<View>(R.id.viewColorRectangle)
        viewColorRectangle.setBackgroundColor(view.resources.getColor(colorItem.color))
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.colour_item_for_goal, parent, false)
        val colorItem = colorItems[position]
        val viewColorRectangle = view.findViewById<View>(R.id.viewColorRectangle)
        viewColorRectangle.setBackgroundColor(view.resources.getColor(colorItem.color))
        return view
    }
}
