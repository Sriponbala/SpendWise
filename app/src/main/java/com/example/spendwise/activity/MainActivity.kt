package com.example.spendwise.activity

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import com.example.spendwise.Categories
import com.example.spendwise.Helper
import com.example.spendwise.R
import com.example.spendwise.databinding.ActivityMainBinding
import com.example.spendwise.domain.User
import com.example.spendwise.enums.CategoryEnum
import com.example.spendwise.enums.LogInStatus
import com.example.spendwise.enums.Period
import com.example.spendwise.enums.RecordType
import com.example.spendwise.viewmodel.*
import com.example.spendwise.viewmodelfactory.BudgetViewModelFactory
import com.example.spendwise.viewmodelfactory.GoalViewModelFactory
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory
import com.example.spendwise.viewmodelfactory.UserViewModelFactory

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

       // preLoadData(sharedPref)

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

    private fun preLoadData(sharedPref: SharedPreferences) {
        if(sharedPref.getString("preLoaded", "") != "PRELOADED") {
            val recordViewModelFactory = RecordViewModelFactory(application)
            val recordViewModel = ViewModelProvider(this, recordViewModelFactory)[RecordViewModel::class.java]
            val categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
            val userViewModelFactory = UserViewModelFactory(application)
            val userViewModel = ViewModelProvider(this, userViewModelFactory)[UserViewModel::class.java]
            val budgetViewModelFactory = BudgetViewModelFactory(application)
            val budgetViewModel = ViewModelProvider(this, budgetViewModelFactory)[BudgetViewModel::class.java]
            val goalViewModelFactory = GoalViewModelFactory(application, resources)
            val goalViewModel = ViewModelProvider(this, goalViewModelFactory)[GoalViewModel::class.java]

            userViewModel.createUserAccount("test@gmail.com")
            userViewModel.isNewUserInserted.observe(this) {
                if (it != null) {
                    userViewModel.insertPassword("Test@123")
                    recordViewModel.insertRecord(userViewModel.user!!.userId, CategoryEnum.Food.categoryName, "9999999999.99", RecordType.EXPENSE.value, "17-05-2023", "Biriyani", "")
                    budgetViewModel.insertBudget(userViewModel.user!!.userId, "Function", "999999", CategoryEnum.Entertainment.categoryName, Period.MONTH.value)
                    goalViewModel.insertGoal(userViewModel.user!!.userId, "House", "50000.99", "20000.88", R.color.entertainment_color, R.drawable.baseline_emoji_people_24)
                    userViewModel.isNewUserInserted.value = null
                }
            }
            editor.putString("preLoaded", "PRELOADED")
        }
    }

}