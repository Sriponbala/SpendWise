package com.example.spendwise.fragment

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
import com.example.spendwise.viewmodel.UserViewModel
import com.example.spendwise.viewmodelfactory.UserViewModelFactory

class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private var isEmailValid = false
    private var isPasswordValid = false
    private var isConfirmPasswordValid = false
    private lateinit var userViewModel: UserViewModel
    private lateinit var password: String
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("SignUpFragment", "oncreate")
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
            Log.e("SignUpFragment", "oncreate view")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            emailFocusListener()
            passwordFocusListener()
            confirmPasswordFocusListener()
        }catch (e: Exception) {
            Log.e("Exception", e.message.toString())
        }

        binding.signUpButton.setOnClickListener {
            Log.e("Signup", "onViewCreated: 1", )
            registerUser()
           /* if() {
                Log.e("Signup", "onViewCreated:2 " )

            } else {
                Log.e("Signup", "onViewCreated: 3")
            }*/
        }
        binding.LoginTextView.setOnClickListener {
            it.findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }

        userViewModel.user.observe(viewLifecycleOwner, Observer {
            userViewModel.insertPassword(password, email)
        })
    }

    private fun emailFocusListener() {
        binding.emailTextInputEditText.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                Log.e("SignupFragment", "isemail- $isEmailValid")
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
                Log.e("SignupFragment", "isPass - $isPasswordValid")
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
                Log.e("SignupFragment", "isconfirm - $isConfirmPasswordValid")
                binding.confirmPasswordTextInputLayout.helperText = validConfirmPassword()
            }
        }
    }

    private fun validConfirmPassword(): String? {
        val password = binding.passwordTextInputEditText.text.toString()
        val confirmPassword = binding.confirmPasswordTextInputEditText.text.toString()
        return Helper.validateConfirmPassword(password, confirmPassword)
    }

    private fun registerUser(): Boolean {
        var registered = false
        isEmailValid = validEmail() == null
        isPasswordValid = validPasswordText() == null
        isConfirmPasswordValid = validConfirmPassword() == null
        Log.e("SignupFragment", "r")
        Log.e("SignupFragment", "${isEmailValid.toString()} ${isPasswordValid.toString()} ${isConfirmPasswordValid.toString()}")
        if(isEmailValid && isPasswordValid && isConfirmPasswordValid) {
            Log.e("SignupFragment", "r1")
            val email = binding.emailTextInputEditText.text.toString()
            val password = binding.passwordTextInputEditText.text.toString()
            userViewModel.checkUniqueUser(email)
            userViewModel.uniqueUser.observe(viewLifecycleOwner, Observer {
                Log.e("SignupFragment", "$it"+" observer")
                if(!it) {
                    Log.e("SignupFragment", "r2")
                    userViewModel.createUserAccount(email)
                    this.password = password
                    this.email = email
                    registered = true
                    moveToNextFragment()
                    Log.e("SignupFragment", "r3")
                    Log.e("Signup", "registered")
                } else {
                    Log.e("SignupFragment", "r4 else")
                    Log.e("Signup", "not registered")
                    binding.emailTextInputLayout.helperText = "User already exists"
                    registered = false
                }
            })
        } else {
            binding.emailTextInputLayout.helperText = validEmail()
            binding.passwordTextInputLayout.helperText = validPasswordText()
            binding.confirmPasswordTextInputLayout.helperText = validConfirmPassword()
            registered = false
        }
        return registered
    }

    private fun moveToNextFragment() {
        Log.e("SignupFragment", "r5 nav")
        this.findNavController().navigate(R.id.action_signUpFragment_to_homePageFragment)
    }

}