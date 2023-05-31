package com.example.spendwise.fragment

import android.content.Context
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.databinding.FragmentHomePageBinding
import com.example.spendwise.domain.Budget
import com.example.spendwise.domain.Record
import com.example.spendwise.enums.LogInStatus
import com.example.spendwise.enums.Period
import com.example.spendwise.enums.RecordType
import com.example.spendwise.listeners.NavigationListener
import com.example.spendwise.viewmodel.*
import com.example.spendwise.viewmodelfactory.*
import java.math.BigDecimal


class HomePageFragment : Fragment(), NavigationListener {

    private lateinit var binding: FragmentHomePageBinding
    private lateinit var navController: NavController
    private lateinit var parentNavController: NavController
    private lateinit var userViewModel: UserViewModel
    private val homePageViewModel: HomePageViewModel by activityViewModels()
    private lateinit var editor: Editor

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
        val sharedPreferences = (activity as MainActivity).getSharedPreferences(resources.getString(R.string.loginStatus), Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt(resources.getString(R.string.userId), 0)
        editor = sharedPreferences.edit()
        userViewModel.fetchUser(userId)
        val nestedNavHostFragment2 = (activity as MainActivity).supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        parentNavController = nestedNavHostFragment2.navController
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val nestedNavHostFragment = childFragmentManager.findFragmentById(R.id.homePageFragmentContainer) as NavHostFragment
        navController = nestedNavHostFragment.navController
        val navGraph = navController.graph
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setOnItemSelectedListener {
            bottomNavigationHandler(it)
        }

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true){
                override fun handleOnBackPressed() {
                    if(homePageViewModel.isAllFabVisible) {
                        binding.backScreenHome.visibility = View.GONE
                        binding.backScreenHomeBottomNav?.visibility = View.GONE
                        binding.addIncomeFabHome.visibility = View.GONE
                        binding.addExpenseFabHome.visibility = View.GONE
                        homePageViewModel.isAllFabVisible = false
                        binding.fabHomePage.animate().rotationBy(-45F)
                        homePageViewModel.isFabRotated = false
                    } else {
                        requireActivity().finish()
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)


        binding.fabHomePage.setOnClickListener {
            when(navController.currentDestination?.label) {
                navGraph.findNode(R.id.dashBoardFragment)?.label -> {
                    if(homePageViewModel.isAllFabVisible) {
                        binding.fabHomePage.animate().rotationBy(-45F)
                        binding.backScreenHome.visibility = View.GONE
                        binding.backScreenHomeBottomNav?.visibility = View.GONE
                        binding.addIncomeFabHome.visibility = View.GONE
                        binding.addExpenseFabHome.visibility = View.GONE
                        homePageViewModel.isAllFabVisible = false
                        homePageViewModel.isFabRotated = false
                    } else {
                        binding.fabHomePage.animate().rotationBy(45F)
                        binding.backScreenHome.visibility = View.VISIBLE
                        binding.backScreenHomeBottomNav?.visibility = View.VISIBLE
                        binding.addIncomeFabHome.visibility = View.VISIBLE
                        binding.addExpenseFabHome.visibility = View.VISIBLE
                        homePageViewModel.isAllFabVisible = true
                        homePageViewModel.isFabRotated = true
                    }
                }
                navGraph.findNode(R.id.budgetFragment)?.label -> {
                    parentNavController.navigate(R.id.action_homePageFragment_to_addBudgetFragment)
                    homePageViewModel.isAllFabVisible = false
                }
                navGraph.findNode(R.id.goalsFragment)?.label -> {
                    parentNavController.navigate(R.id.action_homePageFragment_to_addGoalFragment)
                    homePageViewModel.isAllFabVisible = false
                }
            }
        }

        binding.addIncomeFabHome.setOnClickListener {
            homePageViewModel.isAllFabVisible = false
            homePageViewModel.isFabRotated = true
            val action = HomePageFragmentDirections.actionHomePageFragmentToAddRecordFragment(isEditRecord = false, recordType = RecordType.INCOME.value)
            parentNavController.navigate(action)
        }

        binding.addExpenseFabHome.setOnClickListener {
            homePageViewModel.isAllFabVisible = false
            homePageViewModel.isFabRotated = true
            val action = HomePageFragmentDirections.actionHomePageFragmentToAddRecordFragment(isEditRecord = false, recordType = RecordType.EXPENSE.value)
            parentNavController.navigate(action)
        }

        if(homePageViewModel.isAllFabVisible) {
            binding.backScreenHome.visibility = View.VISIBLE
            binding.addIncomeFabHome.visibility = View.VISIBLE
            binding.addExpenseFabHome.visibility = View.VISIBLE
            binding.backScreenHomeBottomNav?.visibility = View.VISIBLE
            if(homePageViewModel.isFabRotated) {
                binding.fabHomePage.animate().rotationBy(45F)
            }
        } else {
            binding.backScreenHome.visibility = View.GONE
            binding.addIncomeFabHome.visibility = View.GONE
            binding.addExpenseFabHome.visibility = View.GONE
            binding.backScreenHomeBottomNav?.visibility = View.GONE
            if(homePageViewModel.isFabRotated && resources.configuration.uiMode == Configuration.UI_MODE_NIGHT_MASK) {
                binding.fabHomePage.animate().rotationBy(-45F)
                homePageViewModel.isFabRotated = false
            }
        }

    }

    private fun bottomNavigationHandler(menuItem: MenuItem): Boolean {
        var flag = false
        when(menuItem.itemId) {
            R.id.dashBoardFragment -> {
                if(getCurrentDestinationId() != R.id.dashBoardFragment) {
                    navController.navigate(R.id.action_homePageFragment_to_dashBoardFragment)
                    binding.fabHomePage.visibility = View.VISIBLE
                }
                flag =  true
            }
            R.id.budgetFragment -> {
                if(getCurrentDestinationId() != R.id.budgetFragment) {
                    binding.fabHomePage.visibility = View.GONE
                    binding.fabHomePage.visibility = View.VISIBLE
                    binding.backScreenHome.visibility = View.GONE
                    binding.backScreenHomeBottomNav?.visibility = View.GONE
                    binding.addIncomeFabHome.visibility = View.GONE
                    binding.addExpenseFabHome.visibility = View.GONE
                    homePageViewModel.isAllFabVisible = false
                    navController.navigate(R.id.action_homePageFragment_to_budgetFragment)
                }
                flag =  true
            }
            R.id.goalsFragment -> {
                if(getCurrentDestinationId() != R.id.goalsFragment) {
                    binding.fabHomePage.visibility = View.VISIBLE
                    binding.backScreenHome.visibility = View.GONE
                    binding.backScreenHomeBottomNav?.visibility = View.GONE
                    binding.addIncomeFabHome.visibility = View.GONE
                    binding.addExpenseFabHome.visibility = View.GONE
                    homePageViewModel.isAllFabVisible = false
                    navController.navigate(R.id.action_homePageFragment_to_goalsFragment)
                }
                flag =  true
            }
            R.id.settingsFragment -> {
                if(getCurrentDestinationId() != R.id.settingsFragment) {
                    binding.fabHomePage.visibility = View.GONE
                    binding.backScreenHome.visibility = View.GONE
                    binding.backScreenHomeBottomNav?.visibility = View.GONE
                    binding.addIncomeFabHome.visibility = View.GONE
                    binding.addExpenseFabHome.visibility = View.GONE
                    homePageViewModel.isAllFabVisible = false
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

    override fun onActionReceived(destination: Fragment, title: RecordType, period: Period, record: Record?, budget: Pair<Budget, BigDecimal>?) {
        when(destination) {
            is RecordsFragment -> {
                val action = HomePageFragmentDirections.actionHomePageFragmentToRecordsFragment(selectedCategory = null, hideFilterView = false, hideAmountView = false, hideDescriptionText = true)
                parentNavController.navigate(action)
            }
            is StatisticsFragment -> {
                val action = HomePageFragmentDirections.actionHomePageFragmentToStatisticsFragment(title.value)
                parentNavController.navigate(action)
            }
            is LoginFragment -> {
                userViewModel.user = null
                userViewModel.isUserFetched.value = null
                ViewModelProvider(requireActivity(), RecordViewModelFactory(requireActivity().application))[RecordViewModel::class.java].clear()
                ViewModelProvider(requireActivity(), GoalViewModelFactory(requireActivity().application, resources))[GoalViewModel::class.java].clear()
                ViewModelProvider(requireActivity(), BudgetViewModelFactory(requireActivity().application))[BudgetViewModel::class.java].clear()
                ViewModelProvider(requireActivity(), RestoreScrollPositionViewModelFactory(requireActivity().application))[RestoreScrollPositionViewModel::class.java].clear()

                editor.apply {
                    putString(resources.getString(R.string.status), LogInStatus.LOGGED_OUT.name)
                    commit()
                }
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
                parentNavController.navigate(R.id.action_homePageFragment_to_viewGoalFragment)
            }
            is ViewRecordFragment -> {
                record?.let {
                    ViewModelProvider(requireActivity(), RecordViewModelFactory(requireActivity().application))[RecordViewModel::class.java].setSelectedRecord(it)
                    parentNavController.navigate(R.id.action_homePageFragment_to_viewRecordFragment)
                }

            }
            is ViewBudgetFragment -> {
                budget?.let {
                    ViewModelProvider(requireActivity(), BudgetViewModelFactory(requireActivity().application))[BudgetViewModel::class.java].setSelectedBudgetItem(it)
                    parentNavController.navigate(R.id.action_homePageFragment_to_viewBudgetFragment)
                }
            }
        }
    }

    override fun changeVisibilityOfFab(showFab: Boolean) {
        if(showFab) {
            binding.fabHomePage.visibility = View.VISIBLE
        } else {
            binding.fabHomePage.visibility = View.GONE
        }
    }

}