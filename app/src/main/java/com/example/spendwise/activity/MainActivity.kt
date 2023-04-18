package com.example.spendwise.activity

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.databinding.DataBindingUtil.setContentView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.room.RoomDatabase
import com.example.spendwise.R
import com.example.spendwise.database.SpendWiseDatabase
import com.example.spendwise.databinding.ActivityMainBinding
import com.example.spendwise.domain.User
import com.example.spendwise.enums.LogInStatus
import com.example.spendwise.viewmodel.RecordViewModel
import com.example.spendwise.viewmodel.UserViewModel
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory
import com.example.spendwise.viewmodelfactory.UserViewModelFactory
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() { //, NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
//    private lateinit var drawer: DrawerLayout
//    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var navGraph: NavGraph
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var currentDestination : NavDestination
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarMain)
        val sharedPref = getSharedPreferences("LoginStatus", MODE_PRIVATE)
        editor = sharedPref.edit()

//        drawer = binding.drawerLayout
        navHostFragment = supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        navGraph = navController.navInflater.inflate(R.navigation.navigation).apply {
            if(sharedPref.getString("status", "") == LogInStatus.LOGGED_IN.name) {
                Log.e("userId Main create", sharedPref.getInt("userId", 0).toString())
                setStartDestination(R.id.homePageFragment)
            } else {
                setStartDestination(R.id.loginFragment)
            }
        }
        navController.graph = navGraph
        // Define the top level destinations for the AppBarConfiguration
        appBarConfiguration = AppBarConfiguration.Builder(R.id.homePageFragment, R.id.loginFragment, R.id.signUpFragment)
            .build()
        NavigationUI.setupActionBarWithNavController(this, navController)//, appBarConfiguration)
//        NavigationUI.setupWithNavController(binding.navView, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
//            currentDestination = destination
            Log.e("Destination", destination.displayName)
            // Check if the current destination is the desired one to lock the drawer
//            val lockDrawerDestinations = setOf(R.id.homePageFragment)//setOf("Dashboard", "Budget", "Goals")
            Log.e("Destination", destination.id.toString())
            Log.e("Destination", R.id.homePageFragment.toString())
//            val shouldNotLockDrawer = lockDrawerDestinations.contains(destination.id)//supportActionBar?.title)

            Log.e("Destination", supportActionBar?.title.toString())
//            Log.e("Destination", shouldNotLockDrawer.toString())
            // Lock or unlock the drawer based on the destination
           /* if (shouldNotLockDrawer) {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                toggle = ActionBarDrawerToggle(this, drawer, binding.toolbarMain, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
                binding.root.addDrawerListener(toggle)
                toggle.syncState()
            } else {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            }*/
        }
//        binding.navView.setNavigationItemSelectedListener(this)

        /*// Set up the ActionBar with the NavController and AppBarConfiguration
        setupActionBarWithNavController(navController, appBarConfiguration)*/
    }

    override fun onSupportNavigateUp(): Boolean {
       /* // Update the visibility and icon of the Up button based on the current fragment's state
        val currentDestination = navController.currentDestination
        val canNavigateUp = currentDestination?.id !in appBarConfiguration.topLevelDestinations
        supportActionBar?.setDisplayHomeAsUpEnabled(canNavigateUp)
        // Set the desired icon drawable based on the current fragment's state
        val iconDrawable = if (canNavigateUp) R.drawable.back_arrow else null
        if (iconDrawable != null) {
            supportActionBar?.setHomeAsUpIndicator(iconDrawable)
        }

        // Handle Up button click event based on the current fragment's state
        return if (canNavigateUp) {
            navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        } else {
            // Handle other actions for Up button click event
            // For example, show a menu or perform some other action
            // ...
            true
        }*/
        Log.e("NavigateUp", "")
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        val currentFragment = navController.currentDestination?.label
        Log.e("Main", currentFragment.toString())
        Log.e("Main", "${navController.currentDestination?.label?.equals(navGraph.findNode(R.id.loginFragment)?.label)} - ${navGraph.findNode(R.id.loginFragment)?.label}")
        Log.e("Main", "${navController.currentDestination?.label?.equals(navGraph.findNode(R.id.homePageFragment)?.label)} - ${navGraph.findNode(R.id.dashBoardFragment)?.label}")
//        Log.e("Main", "${navController.currentDestination?.label?.equals(navGraph.findNode(R.id.userProfileFragment)?.label)} - ${navGraph.findNode(R.id.userProfileFragment)?.label}")
//        Log.e("Main", currentDestination.displayName)
        if(navController.currentDestination?.label?.equals(navGraph.findNode(R.id.loginFragment)?.label) == true) {
            Log.e("Main", "current - login")
            navController.popBackStack(R.id.loginFragment, true)
            finish()
        } else if(navController.currentDestination?.id == R.id.homePageFragment) {
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

    /*override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.profile -> {
//                navController.navigate(R.id.action_global_userProfileFragment)
            }
            R.id.expenses -> {
//                navController.navigate(R.id.action_global_categoryFragment, )
//                navController.navigate(R.id.action_global_expensesFragment)
            }
            R.id.settings -> {
               // navController.navigate(R.id.action_global_settingsFragment)
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
                *//*val userViewModelFactory = UserViewModelFactory(application)
                val userViewModel = ViewModelProvider(this, userViewModelFactory)[UserViewModel::class.java]
                userViewModel.user.value = null
                val sharedPref = getSharedPreferences("LoginStatus", MODE_PRIVATE)
                editor = sharedPref.edit()*//*
                *//*val userViewModelFactory = UserViewModelFactory(application)
                val userViewModel = ViewModelProvider(this, userViewModelFactory)[UserViewModel::class.java]
                userViewModel.user = null*//*
                editor.apply {
//                    putInt("userId", 0)
                    putString("status", LogInStatus.LOGGED_OUT.name)
                    commit()
                }
//                Log.e("UserID Main", sharedPref.getInt("userId", 0).toString())
                navController.navigate(R.id.action_global_loginFragment)
            }
        }

        return true
    }*/

    /*override fun onStop() {
        super.onStop()
        *//*val s = SpendWiseDatabase.getInstance(application)
        s.userAccountDao.deleteAllUserPasswordsRecords()
        s.userAccountDao.deleteAllRecords()
        s.recordDao.deleteAllRecords()*//*
        val u = ViewModelProvider(this, UserViewModelFactory(application))[UserViewModel::class.java]
        val r = ViewModelProvider(this, RecordViewModelFactory(application))[RecordViewModel::class.java]
        *//*u.deleteAllRecords()
        r.deleteAllRecords()*//*

    }*/

}