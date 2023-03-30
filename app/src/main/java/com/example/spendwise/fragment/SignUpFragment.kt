package com.example.spendwise.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.spendwise.Helper
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.databinding.FragmentSignUpBinding
import com.example.spendwise.enums.LogInStatus
import com.example.spendwise.viewmodel.UserViewModel
import com.example.spendwise.viewmodelfactory.UserViewModelFactory
import kotlin.text.Typography.registered

class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userViewModelFactory = UserViewModelFactory((activity as MainActivity).application)
        userViewModel = ViewModelProvider(this, userViewModelFactory)[UserViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        ((activity) as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
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
        confirmPasswordFocusListener()

        binding.signUpButton.setOnClickListener {
            registerUser()
        }
        binding.LoginTextView.setOnClickListener {
            it.findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }
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

    private fun confirmPasswordFocusListener() {
        binding.confirmPasswordTextInputEditText.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                binding.confirmPasswordTextInputLayout.helperText = validConfirmPassword()
            }
        }
    }

    private fun validConfirmPassword(): String? {
        val password = binding.passwordTextInputEditText.text.toString()
        val confirmPassword = binding.confirmPasswordTextInputEditText.text.toString()
        return Helper.validateConfirmPassword(password, confirmPassword)
    }

    private fun registerUser() {
        val isEmailValid = validEmail() == null
        val isPasswordValid = validPasswordText() == null
        val isConfirmPasswordValid = validConfirmPassword() == null
        if(isEmailValid && isPasswordValid && isConfirmPasswordValid) {
            val email = binding.emailTextInputEditText.text.toString()
            userViewModel.checkIfUserExists(email)
            userViewModel.isEmailExists.observe(viewLifecycleOwner, Observer {
                if(it != null) {
                    if(it) {
                        binding.emailTextInputLayout.helperText = "User already exists"
                    } else {
                        registerNewUser()
                    }
                    userViewModel.isEmailExists.value = null
                }
            })
        } else {
            binding.emailTextInputLayout.helperText = validEmail()
            binding.passwordTextInputLayout.helperText = validPasswordText()
            binding.confirmPasswordTextInputLayout.helperText = validConfirmPassword()
        }
      /*  val isEmailValid = validEmail() == null
        val isPasswordValid = validPasswordText() == null
        val isConfirmPasswordValid = validConfirmPassword() == null
        if(isEmailValid && isPasswordValid && isConfirmPasswordValid) {
            val email = binding.emailTextInputEditText.text.toString()
            val password = binding.passwordTextInputEditText.text.toString()
            userViewModel.checkIfUserExists(email).observe(viewLifecycleOwner, Observer {
                if(!it) {
                    userViewModel.createUserAccount(email)
                    this.password = password
                    this.email = email
                    moveToNextFragment()
                } else {
                    binding.emailTextInputLayout.helperText = "User already exists"
                }
            })
        } else {
            binding.emailTextInputLayout.helperText = validEmail()
            binding.passwordTextInputLayout.helperText = validPasswordText()
            binding.confirmPasswordTextInputLayout.helperText = validConfirmPassword()
        }*/
    }

    private fun registerNewUser() {
        userViewModel.createUserAccount(binding.emailTextInputEditText.text.toString())
        userViewModel.isNewUserInserted.observe(viewLifecycleOwner, Observer {
            if( it != null) {
                userViewModel.insertPassword(binding.passwordTextInputEditText.text.toString(), binding.emailTextInputEditText.text.toString())
                editor.apply {
                    putInt("userId", userViewModel.user.value!!.userId)
                    putString("status", LogInStatus.LOGGED_IN.name)
                    apply()
                }
                moveToNextFragment()
                userViewModel.isNewUserInserted.value = null
            }
        })
    }

    private fun moveToNextFragment() {
        this.findNavController().navigate(R.id.action_signUpFragment_to_homePageFragment)
    }

}