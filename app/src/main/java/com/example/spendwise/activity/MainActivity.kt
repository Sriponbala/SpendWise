package com.example.spendwise.activity

import android.content.Intent
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
import com.example.spendwise.viewmodel.UserViewModel
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var navGraph: NavGraph

    companion object {
        var isLoggedIn = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*val userViewModel = UserViewModel(application)
        userViewModel.createUserAccount(User("sri@"))
        userViewModel.createUserAccount(User("sri@"))
        userViewModel.createUserAccount(User("bala"))
        Log.e("User", "main oncreate 1")*/

//        Log.e("User", userViewModel.user.value.toString() + " main")
//        Log.e("User", userViewModel.user.toString() + " main")
    //    val user = userViewModel.user
      //  Log.e("User", user.value.toString())



        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarMain)

        drawer = binding.drawerLayout
        navHostFragment = supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        navGraph = navController.navInflater.inflate(R.navigation.navigation).apply {
            if(isLoggedIn) {
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

        Log.e("MainActivity", "on create")
        binding.navView.setNavigationItemSelectedListener(this)

//        Log.e("User", userViewModel.user.toString() + " main")
    }



    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {

        if(navController.currentDestination?.label?.equals(navGraph.findNode(R.id.loginFragment)?.label) == true) {
            navController.popBackStack(R.id.loginFragment, true)
            finish()
        } else if(navController.currentDestination?.label?.equals(navGraph.findNode(R.id.homePageFragment)?.label) == true) {
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
                navController.navigate(R.id.action_global_loginFragment)
            }
        }

        return true
    }

}