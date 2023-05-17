package com.example.spendwise.activity

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import com.example.spendwise.Helper
import com.example.spendwise.R
import com.example.spendwise.databinding.ActivityMainBinding
import com.example.spendwise.enums.LogInStatus
import com.example.spendwise.viewmodel.CategoryViewModel
import com.example.spendwise.viewmodel.RecordViewModel
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var navGraph: NavGraph
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Helper.setResources(resources)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences(resources.getString(R.string.loginStatus), MODE_PRIVATE)
        editor = sharedPref.edit()

        navHostFragment = supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        navGraph = navController.navInflater.inflate(R.navigation.navigation).apply {
            if(sharedPref.getString(resources.getString(R.string.status), "") == LogInStatus.LOGGED_IN.name) {
                setStartDestination(R.id.homePageFragment)
            } else {
                setStartDestination(R.id.loginFragment)
            }
        }
        navController.graph = navGraph
    }

    override fun onSupportNavigateUp(): Boolean {
        when {
            (navController.currentDestination?.id == R.id.addRecordFragment || navController.currentDestination?.id == R.id.addBudgetFragment) -> {
                val recordViewModelFactory = RecordViewModelFactory(application)
                val recordViewModel = ViewModelProvider(this, recordViewModelFactory)[RecordViewModel::class.java]
                recordViewModel.isTempDataSet = false
            }
        }
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if(navController.currentDestination?.id == R.id.addRecordFragment || navController.currentDestination?.id == R.id.addBudgetFragment) {
            val recordViewModelFactory = RecordViewModelFactory(application)
            val recordViewModel = ViewModelProvider(this, recordViewModelFactory)[RecordViewModel::class.java]
            recordViewModel.isTempDataSet = false
        }
        if(navController.currentDestination?.label?.equals(navGraph.findNode(R.id.recordsFragment)?.label) == true) {
            val categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
            categoryViewModel.category.value = null
        }
        if(navController.currentDestination?.label?.equals(navGraph.findNode(R.id.loginFragment)?.label) == true) {
            navController.popBackStack(R.id.loginFragment, true)
            finish()
        } else {
            super.onBackPressed()
        }

    }

}