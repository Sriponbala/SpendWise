package com.example.spendwise.activity

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.spendwise.R
import com.example.spendwise.database.SpendWiseDatabase
import com.example.spendwise.databinding.ActivityMainBinding
import com.example.spendwise.domain.User
import com.example.spendwise.enums.LogInStatus
import com.example.spendwise.viewmodel.UserViewModel
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var navGraph: NavGraph
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarMain)
        val sharedPref = getSharedPreferences("LoginStatus", MODE_PRIVATE)
        editor = sharedPref.edit()

        drawer = binding.drawerLayout
        navHostFragment = supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        navGraph = navController.navInflater.inflate(R.navigation.navigation).apply {
            if(sharedPref.getString("status", "") == LogInStatus.LOGGED_IN.name) {
                setStartDestination(R.id.homePageFragment)
            } else {
                setStartDestination(R.id.loginFragment)
            }
        }
        navController.graph = navGraph
        NavigationUI.setupActionBarWithNavController(this, navController)
        NavigationUI.setupWithNavController(binding.navView, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Check if the current destination is the desired one to lock the drawer
            val lockDrawerDestinations = setOf(R.id.homePageFragment, R.id.dashBoardFragment, R.id.budgetFragment, R.id.goalsFragment)
            val shouldNotLockDrawer = lockDrawerDestinations.contains(destination.id)

            // Lock or unlock the drawer based on the destination
            if (shouldNotLockDrawer) {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                toggle = ActionBarDrawerToggle(this, drawer, binding.toolbarMain, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
                binding.root.addDrawerListener(toggle)
                toggle.syncState()
            } else {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }
        binding.navView.setNavigationItemSelectedListener(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        val currentFragment = navController.currentDestination?.label
        Log.e("Main", currentFragment.toString())
        Log.e("Main", "${navController.currentDestination?.label?.equals(navGraph.findNode(R.id.loginFragment)?.label)} - ${navGraph.findNode(R.id.loginFragment)?.label}")
        Log.e("Main", "${navController.currentDestination?.label?.equals(navGraph.findNode(R.id.homePageFragment)?.label)} - ${navGraph.findNode(R.id.dashBoardFragment)?.label}")
        if(navController.currentDestination?.label?.equals(navGraph.findNode(R.id.loginFragment)?.label) == true) {
            Log.e("Main", "current - login")
            navController.popBackStack(R.id.loginFragment, true)
            finish()
        } else if(navController.currentDestination?.label?.equals(navGraph.findNode(R.id.dashBoardFragment)?.label) == true) {
            Log.e("Main", "current - home")
            navController.popBackStack(0, true)
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("MainActivity", "on destroy")
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.profile -> {
                navController.navigate(R.id.action_global_userProfileFragment)
            }
            R.id.expenses -> {
                navController.navigate(R.id.action_global_expensesFragment)
            }
            R.id.settings -> {
                navController.navigate(R.id.action_global_settingsFragment)
            }
            R.id.recommend -> {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra("About Spend wise app", "Download spend wise")
                    type= "text/plain"
                }
                startActivity(intent)
            }
            R.id.logout -> {
                editor.apply {
                    putString("status", LogInStatus.LOGGED_OUT.name)
                    commit()
                }
                navController.navigate(R.id.action_global_loginFragment)
            }
        }

        return true
    }

}