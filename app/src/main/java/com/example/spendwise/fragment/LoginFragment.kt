package com.example.spendwise.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.databinding.FragmentLoginBinding
import com.example.spendwise.enums.LogInStatus
import com.example.spendwise.viewmodel.UserViewModel
import com.example.spendwise.viewmodelfactory.UserViewModelFactory

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userViewModelFactory = UserViewModelFactory((activity as MainActivity).application)
        userViewModel = ViewModelProvider(requireActivity(), userViewModelFactory)[UserViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        ((activity) as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).apply {
            val sharedPref = this.getSharedPreferences(resources.getString(R.string.loginStatus), Context.MODE_PRIVATE)
            editor = sharedPref.edit()
        }

        binding.loginButton.setOnClickListener{
            login()
        }

        binding.SignUpTextView.setOnClickListener {
            it.findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        userViewModel.isUserFetchedFinally.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it) {
                    editor.apply {
                        Log.e(
                            "UserId",
                            "login before entering in editor " + userViewModel.user?.userId.toString()
                        )
                        putInt(resources.getString(R.string.userId), userViewModel.user?.userId!!)
                        putString(resources.getString(R.string.status), LogInStatus.LOGGED_IN.name)
                        apply()
                    }
                    moveToNextFragment()
                }
                userViewModel.isUserFetchedFinally.value = null
            }
        }
    }

    private fun login() {
        val email = binding.emailTextInputEditText.text.toString()
        userViewModel.checkIfUserExists(email)
        userViewModel.isEmailExists.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it) {
                    loginUser()
                } else {
                    binding.emailTextInputLayout.helperText = resources.getString(R.string.loginEmailMessage)
                }
                userViewModel.isEmailExists.value = null
            }
        }
    }

    private fun loginUser() {
        binding.emailTextInputLayout.helperText = null
        userViewModel.fetchUser(binding.emailTextInputEditText.text.toString())
        userViewModel.isUserFetched.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it) {
                    userViewModel.verifyPassword(binding.passwordTextInputEditText.text.toString())
                    userViewModel.isCorrectPassword.observe(
                        viewLifecycleOwner
                    ) { password ->
                        if (password != null) {
                            if (password) {
                                userViewModel.isUserFetchedFinally.value = true
                            } else {
                                binding.passwordTextInputLayout.helperText =
                                    resources.getString(R.string.incorrectPassword)
                            }
                            userViewModel.isCorrectPassword.value = null
                        }
                    }
                }
                userViewModel.isUserFetched.value = null
                userViewModel.isLoggedIn.value = null
            }
        }
    }

    private fun moveToNextFragment() {
        findNavController().navigate(R.id.action_loginFragment_to_homePageFragment)
    }

}