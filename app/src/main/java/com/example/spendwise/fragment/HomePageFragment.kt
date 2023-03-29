package com.example.spendwise.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.databinding.FragmentHomePageBinding
import com.example.spendwise.listeners.NavigationListener
import com.google.android.material.bottomnavigation.BottomNavigationView


class HomePageFragment : Fragment(), NavigationListener {

    private lateinit var binding: FragmentHomePageBinding
    private lateinit var navController: NavController
    private lateinit var parentNavController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val nestedNavHostFragment2 = (activity as MainActivity).supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        parentNavController = nestedNavHostFragment2.navController
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentHomePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val nestedNavHostFragment = childFragmentManager.findFragmentById(R.id.homePageFragmentContainer) as NavHostFragment
        navController = nestedNavHostFragment.navController
        val navGraph = navController.graph
        binding.bottomNavigationView.setupWithNavController(navController)

        binding.fabHomePage.setOnClickListener {
            when(navController.currentDestination?.label) {
                navGraph.findNode(R.id.dashBoardFragment)?.label -> {
                    parentNavController.navigate(R.id.action_homePageFragment_to_addRecordFragment)
                }
                navGraph.findNode(R.id.budgetFragment)?.label -> {
                    parentNavController.navigate(R.id.action_homePageFragment_to_addBudgetFragment)
                }
                navGraph.findNode(R.id.goalsFragment)?.label -> {
                    parentNavController.navigate(R.id.action_homePageFragment_to_addGoalFragment)
                }
            }
        }


    }

    override fun onActionReceived(destination: Fragment) {
        when(destination) {
            is RecordsFragment -> {
                parentNavController.navigate(R.id.action_homePageFragment_to_recordsFragment)
            }
            is StatisticsFragment -> {
                parentNavController.navigate(R.id.action_homePageFragment_to_statisticsFragment)
            }
            /*R.id.action_homePageFragment_to_recordsFragment -> {
                parentNavController.navigate(action)
            }
            R.id.action_homePageFragment_to_statisticsFragment -> {
                parentNavController.navigate(action)
            }*/
        }
    }




}