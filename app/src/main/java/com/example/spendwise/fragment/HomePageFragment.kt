package com.example.spendwise.fragment

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences.Editor
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.databinding.FragmentHomePageBinding
import com.example.spendwise.enums.LogInStatus
import com.example.spendwise.enums.Period
import com.example.spendwise.enums.RecordType
import com.example.spendwise.listeners.NavigationListener
import com.example.spendwise.viewmodel.*
import com.example.spendwise.viewmodelfactory.*
import com.google.android.material.bottomsheet.BottomSheetDialog


class HomePageFragment : Fragment(), NavigationListener {

    private lateinit var binding: FragmentHomePageBinding
    private lateinit var navController: NavController
    private lateinit var parentNavController: NavController
    private lateinit var userViewModel: UserViewModel
    private lateinit var editor: Editor
    private val navigationViewModel: NavigationViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ((activity) as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userViewModelFactory = UserViewModelFactory((activity as MainActivity).application)
        userViewModel = ViewModelProvider(requireActivity(), userViewModelFactory)[UserViewModel::class.java]
        val sharedPreferences = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", 0)
        editor = sharedPreferences.edit()
        Log.e("UserID", "home oncreate 1 "+ userId.toString())
        userViewModel.fetchUser(userId)
        Log.e("UserId", userViewModel.toString())
        Log.e("UserID", "home oncreate 2 "+ userViewModel.user?.userId.toString())
        val nestedNavHostFragment2 = (activity as MainActivity).supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        parentNavController = nestedNavHostFragment2.navController
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        setHasOptionsMenu(true)
        binding = FragmentHomePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val nestedNavHostFragment = childFragmentManager.findFragmentById(R.id.homePageFragmentContainer) as NavHostFragment
        navController = nestedNavHostFragment.navController
//        navController.navigate(R.id.action_homePageFragment_to_dashBoardFragment)
        val navGraph = navController.graph
        binding.bottomNavigationView.setupWithNavController(navController)

      //  navController.navigate(R.id.action_homePageFragment_to_dashBoardFragment)
        binding.bottomNavigationView.setOnItemSelectedListener {
            bottomNavigationHandler(it)
        }

        if(navController.currentDestination?.id == R.id.settingsFragment) {
            binding.fabHomePage.visibility = View.GONE
        }

        binding.fabHomePage.setOnClickListener {
            when(navController.currentDestination?.label) {
                navGraph.findNode(R.id.dashBoardFragment)?.label -> {
                    val action = HomePageFragmentDirections.actionHomePageFragmentToAddRecordFragment(isEditRecord = false)
                    parentNavController.navigate(action)
//                    parentNavController.navigate(R.id.action_homePageFragment_to_addRecordFragment)
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

    /*override fun onStop() {
        super.onStop()
        (activity as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
           // setHomeAsUpIndicator(R.drawable.baseline_close_24)
        }
    }*/

    private fun bottomNavigationHandler(menuItem: MenuItem): Boolean {
        var flag = false
        when(menuItem.itemId) {
            R.id.dashBoardFragment -> {
                if(getCurrentDestinationId() != R.id.dashBoardFragment) {
                    binding.fabHomePage.visibility = View.VISIBLE
                    navController.navigate(R.id.action_homePageFragment_to_dashBoardFragment)//, null, navOptions)
                }
                flag =  true
            }
            R.id.budgetFragment -> {
                if(getCurrentDestinationId() != R.id.budgetFragment) {
                    binding.fabHomePage.visibility = View.VISIBLE
                    navController.navigate(R.id.action_homePageFragment_to_budgetFragment)//, null, navOptions)
                }
                flag =  true
            }
            R.id.goalsFragment -> {
                if(getCurrentDestinationId() != R.id.goalsFragment) {
                    binding.fabHomePage.visibility = View.VISIBLE
                    navController.navigate(R.id.action_homePageFragment_to_goalsFragment)
                }
                flag =  true
            }
            R.id.settingsFragment -> {
                if(getCurrentDestinationId() != R.id.settingsFragment) {
                    binding.fabHomePage.visibility = View.GONE
                    navController.navigate(R.id.action_homePageFragment_to_settingsFragment)
                }
                flag = true
            }
        }
        return flag
    }

    private fun getCurrentDestinationId(): Int {
        val navGraph = navController.graph
        return when(navController.currentDestination?.label) {
            navGraph.findNode(R.id.dashBoardFragment)?.label -> {
                R.id.dashBoardFragment
            }
            navGraph.findNode(R.id.budgetFragment)?.label -> {
                R.id.budgetFragment
            }
            navGraph.findNode(R.id.goalsFragment)?.label -> {
                R.id.goalsFragment
            }
            navGraph.findNode(R.id.settingsFragment)?.label -> {
                R.id.settingsFragment
            }
            else -> R.id.dashBoardFragment
        }
    }

/*
    private fun showBottomSheet() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(com.example.spendwise.R.layout.bottom_sheet_layout)
        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.window!!.attributes.windowAnimations = com.example.shoppingapplication.R.style.Theme_ShoppingApplication
        dialog.window!!.setGravity(Gravity.BOTTOM)
//        navigateFromBottomSheet(bottomSheetView, bottomSheetDialog)
        binding.bottomNavigationView.selectedItemId = getCurrentDestinationId().also {
            println("more 1")
            println(it)
            println("more 2")
        }
    }
*/
/*
    private fun showBottomSheet1() {

        binding.bottomNavigationView.selectedItemId = getCurrentDestinationId().also {
            println("more 1")
            println(it)
            println("more 2")
        }

        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)//view?.findViewById(R.id.bottomSheetContainer), false)
       // bottomSheetView.parent?.toString().also(::println)
//        bottomSheetView.setBackgroundResource(R.drawable.bottom_sheet_bg)

        val bottomSheetDialog = BottomSheetDialog(this.requireActivity())
        bottomSheetDialog.setContentView(bottomSheetView)
//        bottomSheetDialog.window?.setWindowAnimations(R.style.DialogAnimation)
        bottomSheetDialog.show()
        binding.bottomNavigationView.selectedItemId = navigationViewModel.previouslySelectedMenuItemId.also(::println)
        navigateFromBottomSheet(bottomSheetView, bottomSheetDialog)
    }
*/

/*
    private fun navigateFromBottomSheet(view: View, bottomSheetDialog: BottomSheetDialog) {
        // Set click listeners for the clickable layouts in the bottom sheet
        view.findViewById<View>(R.id.layoutIncome).setOnClickListener {
            // Handle click for layout1
           // parentNavController.navigate()
            Log.e("Income", "clicked")
            val action = HomePageFragmentDirections.actionHomePageFragmentToCategoryFragment(RecordType.INCOME.value, R.id.homePageFragment)
            parentNavController.navigate(action)
            bottomSheetDialog.dismiss() // Dismiss the bottom sheet with slide-out animation
            binding.bottomNavigationView.selectedItemId = getCurrentDestinationId()
            Toast.makeText(requireContext(), "Income", Toast.LENGTH_LONG)

        }

        view.findViewById<View>(R.id.layoutExpense).setOnClickListener {
            // Handle click for layout1
            // parentNavController.navigate()
            Log.e("Expense", "clicked")

            val action = HomePageFragmentDirections.actionHomePageFragmentToCategoryFragment(RecordType.EXPENSE.value, R.id.homePageFragment)
            parentNavController.navigate(action)

            Toast.makeText(requireContext(), "Expense", Toast.LENGTH_LONG)
            bottomSheetDialog.dismiss() // Dismiss the bottom sheet with slide-out animation
            binding.bottomNavigationView.selectedItemId = getCurrentDestinationId().also {
                println(R.id.homePageFragment)
                println(it)
            }
            Log.e("Expense", "clicked")
        }

        view.findViewById<View>(R.id.layoutSettings).setOnClickListener {
            // Handle click for layout1
            // parentNavController.navigate()
            Log.e("Settings", "clicked")
            bottomSheetDialog.dismiss() // Dismiss the bottom sheet with slide-out animation
            binding.bottomNavigationView.selectedItemId = getCurrentDestinationId()
            Toast.makeText(requireContext(), "Settings", Toast.LENGTH_LONG)
        }

    }
*/

    override fun onActionReceived(destination: Fragment, title: RecordType, period: Period) {
        when(destination) {
            is RecordsFragment -> {
                val action = HomePageFragmentDirections.actionHomePageFragmentToRecordsFragment(selectedCategory = null, hideFilterView = false, hideAmountView = false, hideDescriptionText = true)
//                parentNavController.navigate(R.id.action_homePageFragment_to_recordsFragment)
                parentNavController.navigate(action)
            }
            is StatisticsFragment -> {
                val action = HomePageFragmentDirections.actionHomePageFragmentToStatisticsFragment(title.value)
                parentNavController.navigate(action)
               // parentNavController.navigate(R.id.action_homePageFragment_to_statisticsFragment)
            }
            is LoginFragment -> {
                Log.e("UserID", "Logout")
                Log.e("Settings", "home when login")
                userViewModel.user = null
                userViewModel.isUserFetched.value = null
                ViewModelProvider(requireActivity(), RecordViewModelFactory(requireActivity().application))[RecordViewModel::class.java].clear()
                ViewModelProvider(requireActivity(), GoalViewModelFactory(requireActivity().application))[GoalViewModel::class.java].clear()
                ViewModelProvider(requireActivity(), BudgetViewModelFactory(requireActivity().application))[BudgetViewModel::class.java].clear()
                ViewModelProvider(requireActivity(), RestoreScrollPositionViewModelFactory(requireActivity().application))[RestoreScrollPositionViewModel::class.java].clear()

                editor.apply {
//                    putInt("userId", 0)
                    putString("status", LogInStatus.LOGGED_OUT.name)
                    commit()
                }
//                Log.e("UserID Main", sharedPref.getInt("userId", 0).toString())
//                findNavController().popBackStack()

                parentNavController.navigate(R.id.action_global_loginFragment)
            }
            is CategoryFragment -> {
                val action = HomePageFragmentDirections.actionHomePageFragmentToCategoryFragment(title.value, R.id.homePageFragment)
                parentNavController.navigate(action)
            }
            is MonthlyBudgetsFragment ->  {
                val action = HomePageFragmentDirections.actionHomePageFragmentToMonthlyBudgetsFragment(period.value)
                parentNavController.navigate(action)
            }
            is ViewGoalFragment -> {
                Log.e("Goal", "Homepage")
                parentNavController.navigate(R.id.action_homePageFragment_to_viewGoalFragment)
            }
        }
    }




}