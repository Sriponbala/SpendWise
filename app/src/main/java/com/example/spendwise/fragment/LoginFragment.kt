package com.example.spendwise.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.spendwise.Helper
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
//        Log.e("UserViewModel  Login", userViewModel.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        ((activity) as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).apply {
            val sharedPref = this.getSharedPreferences("LoginStatus", Context.MODE_PRIVATE)
            editor = sharedPref.edit()
        }

        emailFocusListener()
        passwordFocusListener()

        binding.loginButton.setOnClickListener{
            login()
        }

        binding.SignUpTextView.setOnClickListener {
            it.findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        userViewModel.isUserFetchedFinally.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                if(it) {
                    editor.apply{
                        Log.e("UserId", "login before entering in editor " + userViewModel.user?.userId.toString())
                        putInt("userId", userViewModel.user?.userId!!)
                        putString("status", LogInStatus.LOGGED_IN.name)
                        apply()
                    }
                    moveToNextFragment()
                }
                userViewModel.isUserFetchedFinally.value = null
            }
        })
    }

    private fun emailFocusListener() {
        binding.emailTextInputEditText.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                binding.emailTextInputLayout.helperText = validEmail()
            }
        }
    }

    private fun validEmail(): String? {
        val email = binding.emailTextInputEditText.text.toString()
        return Helper.validateEmail(email)
    }

    private fun passwordFocusListener() {
        binding.passwordTextInputEditText.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                binding.passwordTextInputLayout.helperText = validPasswordText()
            }
        }
    }

    private fun validPasswordText(): String? {
        val password = binding.passwordTextInputEditText.text.toString()
        return Helper.validatePasswordText(password)
    }

    private fun login() {
        val isEmailValid = validEmail() == null
        val isPasswordValid = validPasswordText() == null
        if (isEmailValid && isPasswordValid) {
            val email = binding.emailTextInputEditText.text.toString()
            userViewModel.checkIfUserExists(email)
            userViewModel.isEmailExists.observe(viewLifecycleOwner, Observer {
                if(it != null) {
                    if(it) {
                        loginUser()
                    } else {
                        binding.emailTextInputLayout.helperText = "Invalid email id"
                    }
                    userViewModel.isEmailExists.value = null
                }
            })
        } else {
            binding.emailTextInputLayout.helperText = validEmail()
            binding.passwordTextInputLayout.helperText = validPasswordText()
        }
    }

    private fun loginUser() {
        userViewModel.fetchUser(binding.emailTextInputEditText.text.toString())
        userViewModel.isUserFetched.observe(viewLifecycleOwner, Observer {
            Log.e("UserID","isFetched - " + it?.toString())
            if(it != null) {
                if(it) {
                    Log.e("UserID", "before verify password ${userViewModel.user.toString()}")
                    userViewModel.verifyPassword(binding.passwordTextInputEditText.text.toString())
                    userViewModel.isCorrectPassword.observe(viewLifecycleOwner, Observer { password ->
                        Log.e("Password", password.toString())
                        Log.e("isCorrect0", it.toString())
                        if(password != null) {
                            Log.e("Password", password.toString())
                            Log.e("isCorrect1", password.toString())
                            if(password) {
                                Log.e("Password", password.toString())
                                Log.e("isCorrect2", password.toString())
                                userViewModel.isUserFetchedFinally.value = true
                                /*editor.apply {
                                    Log.e("isCorrect3", it.toString())
                                    Log.e("Login", userViewModel.user.value!!.userId.toString())
                                    Log.e("isCorrect4", it.toString())
                                    Log.e("Login userId", userViewModel.user.value!!.userId.toString())
                                    Log.e("isCorrect5", it.toString())
                                    Log.e("UserId moveNext", userViewModel.user.value!!.userId.toString())
                                   // userViewModel.fetchUser(binding.emailTextInputEditText.text.toString())
                                   // putInt("userId", userViewModel.user.value!!.userId)
                                    putString("status", LogInStatus.LOGGED_IN.name)
                                    apply()
                                }*/
                                Log.e("UserID", "login() "+ userViewModel.user?.userId.toString())
                                //moveToNextFragment()
                            } else {
                                /* editor.apply {
                                     putInt("userId", 0)
                                 }*/
                                binding.passwordTextInputLayout.helperText = "Incorrect password"
                            }
                            userViewModel.isCorrectPassword.value = null
                        }
                    })
                }
                userViewModel.isUserFetched.value = null
                userViewModel.isLoggedIn.value = null
            }
        })
    }

    private fun moveToNextFragment() {
        findNavController().navigate(R.id.action_loginFragment_to_homePageFragment)
    }
}