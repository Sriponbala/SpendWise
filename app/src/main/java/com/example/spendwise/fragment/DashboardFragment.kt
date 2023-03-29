package com.example.spendwise.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.databinding.FragmentDashboardBinding
import com.example.spendwise.listeners.NavigationListener

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var navController: NavController
    private lateinit var navigationListener: NavigationListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        (activity as MainActivity).supportActionBar?.apply {
            title = "Dashboard"
            setDisplayHomeAsUpEnabled(true)
        }
        navigationListener = parentFragment?.parentFragment as HomePageFragment
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.showRecords.setOnClickListener {
            navigationListener.onActionReceived(RecordsFragment())
        }
        binding.showStatistics.setOnClickListener {
            navigationListener.onActionReceived(StatisticsFragment())
        }
    }

}