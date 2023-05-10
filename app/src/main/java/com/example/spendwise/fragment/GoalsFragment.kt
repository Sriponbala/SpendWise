package com.example.spendwise.fragment

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.adapter.GoalAdapter
import com.example.spendwise.databinding.FragmentGoalsBinding
import com.example.spendwise.domain.Goal
import com.example.spendwise.listeners.NavigationListener
import com.example.spendwise.viewmodel.GoalViewModel
import com.example.spendwise.viewmodel.RestoreScrollPositionViewModel
import com.example.spendwise.viewmodelfactory.GoalViewModelFactory
import com.example.spendwise.viewmodelfactory.RestoreScrollPositionViewModelFactory

class GoalsFragment : Fragment() {

    private lateinit var binding: FragmentGoalsBinding
    private lateinit var goalViewModel: GoalViewModel
    private lateinit var navigationListener: NavigationListener
    private lateinit var restoreScrollPositionViewModel: RestoreScrollPositionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
/*        navigationListener = parentFragment?.parentFragment as HomePageFragment
        navigationListener.changeVisibilityOfFab(true)*/
        val factory = GoalViewModelFactory(requireActivity().application)
        goalViewModel = ViewModelProvider(requireActivity(), factory)[GoalViewModel::class.java]
        val restoreFactory = RestoreScrollPositionViewModelFactory(requireActivity().application)
        restoreScrollPositionViewModel = ViewModelProvider(this, restoreFactory)[RestoreScrollPositionViewModel::class.java]
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        /*(activity as MainActivity).supportActionBar?.apply {
            title = "Goals"
            setDisplayHomeAsUpEnabled(false)
        }*/
        binding = FragmentGoalsBinding.inflate(inflater, container, false)
        navigationListener = parentFragment?.parentFragment as HomePageFragment
        val userId = (activity as MainActivity).getSharedPreferences("LoginStatus", Context.MODE_PRIVATE).getInt("userId", 0)
        Log.e("Goal", userId.toString())
        goalViewModel.fetchAllGoals(userId)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.goalsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    restoreScrollPositionViewModel.scrollPositionGoals = (binding.goalsRecyclerView.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
                }
            }
        })

        goalViewModel.goals.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                if(it.isNotEmpty()) {
                    setAdapter(it)
                    binding.scrollViewEmptyDataGoals.visibility = View.GONE
                    binding.goalsRecyclerView.visibility = View.VISIBLE
                } else {
                    binding.scrollViewEmptyDataGoals.visibility = View.VISIBLE
                    binding.noData.emptyGoals.emptyDataImage.setImageResource(R.drawable.goal)
                    binding.noData.emptyGoals.emptyDataText.text = "No Goals Found\nClick (+) To Add Goal"
                    binding.goalsRecyclerView.visibility = View.GONE
                }
            }
        })
    }

    private fun setAdapter(goals: List<Goal>) {
        val adapter = GoalAdapter(goals)
        adapter.onItemClick = {
            Log.e("Goal", it.toString())
            goalViewModel.setSelectedGoalItem(it)
            navigationListener.onActionReceived(destination = ViewGoalFragment())
        }
        binding.goalsRecyclerView.adapter = adapter
        val spanCount = if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
            resources.configuration.screenWidthDp >= 600) {
            binding.goalsRecyclerView.setBackgroundColor(resources.getColor(R.color.behindScreen))
            2
        } else {
            binding.goalsRecyclerView.setBackgroundColor(resources.getColor(R.color.recordPage))
            1
        }
        binding.goalsRecyclerView.layoutManager = GridLayoutManager(this.context, spanCount)//LinearLayoutManager(requireContext())
        (binding.goalsRecyclerView.layoutManager as GridLayoutManager).scrollToPosition(restoreScrollPositionViewModel.scrollPositionGoals)
    }

    override fun onResume() {
        super.onResume()
        val actionBar = (activity as? MainActivity)?.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(false)
        actionBar?.title = "Goals"
    }

/*
    override fun onPause() {
        restoreScrollPositionViewModel.dashboardScrollPosition.observe(viewLifecycleOwner, Observer {
            Log.e("Scroll", it.toString() + "observe")
            if (it != null) {
                binding.scrollViewGoals.scrollTo(0, it)
            }
        })
        super.onPause()
        Log.e("Scroll", "onPause")
        restoreScrollPositionViewModel.updateDashboardScrollPosition(binding.scrollViewGoals.scrollY)
    }
*/


}