package com.example.spendwise.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.spendwise.R
import com.example.spendwise.adapter.SettingsRecyclerViewAdapter
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
        val factory = RecordViewModelFactory(requireActivity().application)
        recordViewModel = ViewModelProvider(requireActivity(), factory)[RecordViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        binding.settingsRecyclerView.layoutManager = GridLayoutManager(this.context, 3)

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.logOut -> {
                val alertMessage = resources.getString(R.string.logOutAlert)
                showAlertDialog(alertMessage)
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
            .setPositiveButton(resources.getString(R.string.log_out_button)) { _, _ ->
                navigationListener.onActionReceived(LoginFragment())
            }
            .setNegativeButton(resources.getString(R.string.cancel_button), null)
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
                        putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.app_usage_feedback))
                    }
                    startActivity(intent)
                }catch (error: Exception){
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.emailFeedbackErrorMessage),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            SettingsOption.CALL_SUPPORT -> {
                val callIntent = Intent(Intent.ACTION_DIAL)
                callIntent.data = Uri.parse("tel:9080440194")
                startActivity(callIntent)
            }
        }
    }

}