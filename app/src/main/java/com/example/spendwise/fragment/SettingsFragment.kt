package com.example.spendwise.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendwise.DataBinderMapperImpl
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.adapter.SettingsRecyclerViewAdapter
import com.example.spendwise.databinding.FragmentDashboardBinding
import com.example.spendwise.databinding.FragmentSettingsBinding
import com.example.spendwise.enums.Period
import com.example.spendwise.enums.RecordType
import com.example.spendwise.enums.SettingsOption
import com.example.spendwise.listeners.NavigationListener
import com.example.spendwise.viewmodel.RecordViewModel
import com.example.spendwise.viewmodelfactory.RecordViewModelFactory

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var adapter: SettingsRecyclerViewAdapter
    private lateinit var navigationListener: NavigationListener
    private lateinit var recordViewModel: RecordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      /*  navigationListener = parentFragment?.parentFragment as HomePageFragment
        navigationListener.changeVisibilityOfFab(false)*/
        val factory = RecordViewModelFactory(requireActivity().application)
        recordViewModel = ViewModelProvider(requireActivity(), factory)[RecordViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        /*(activity as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            title = "Settings"
        }*/
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding.toolbarSettings.apply {
            setOnMenuItemClickListener {
                onOptionsItemSelected(it)
            }
        }
        navigationListener = parentFragment?.parentFragment as HomePageFragment
        adapter = SettingsRecyclerViewAdapter()
        binding.settingsRecyclerView.adapter = adapter
        adapter.onItemClick = { doSelectedOption(it) }
        binding.settingsRecyclerView.layoutManager = GridLayoutManager(this.context, 3)//LinearLayoutManager(this.context)//

        return binding.root
    }

   /* override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_overflow_menu, menu)
    }*/

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.e("Settings", "menu " + item.title + " id "+ item.itemId.toString() + " R.id.logout - "+ R.id.logout.toString())
        when(item.itemId) {
            R.id.logOut -> {
                val alertMessage = resources.getString(R.string.logOutAlert)
                showAlertDialog(alertMessage)
//                navigationListener.onActionReceived(LoginFragment())
                Log.e("Settings", "menu")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingInflatedId")
    private fun showAlertDialog(alertMessage: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.alert_text_view, null)
        val alertTextView = dialogView.findViewById<TextView>(R.id.alertTextView)
        alertTextView.text = alertMessage
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setPositiveButton("Log out") { _, _ ->
                navigationListener.onActionReceived(LoginFragment())
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun doSelectedOption(option: SettingsOption) {
        when(option) {
            SettingsOption.INCOME -> {
                recordViewModel.period = Period.ALL
                recordViewModel.recordType.value = RecordType.INCOME.value
                navigationListener.onActionReceived(CategoryFragment(), RecordType.INCOME)
            }
            SettingsOption.EXPENSE -> {
                recordViewModel.period = Period.ALL
                recordViewModel.recordType.value = RecordType.EXPENSE.value
                navigationListener.onActionReceived(CategoryFragment(), RecordType.EXPENSE)
            }
            SettingsOption.FEEDBACK -> {
                try{
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:sriponbala.kb@zohocorp.com")
                        putExtra(Intent.EXTRA_SUBJECT, "App Usage Feedback")
                    }
                    startActivity(intent)
                }catch (error: Exception){
                    Toast.makeText(
                        requireContext(),
                        "Issues with providing feedback. We'll check and update soon.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            SettingsOption.CALL_SUPPORT -> {
                val callIntent = Intent(Intent.ACTION_DIAL)
                callIntent.data = Uri.parse("tel:9080440195")
                startActivity(callIntent)
            }
        }
    }

}