package com.example.spendwise.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity

class GoalsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (activity as MainActivity).supportActionBar?.apply {
            title = "Goals"
        }
        return inflater.inflate(R.layout.fragment_goals, container, false)
    }

}