package com.example.spendwise.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.spendwise.Helper
import com.example.spendwise.R
import com.example.spendwise.activity.MainActivity
import com.example.spendwise.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("LoginFragment", "oncreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        ((activity) as MainActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            Log.e("LoginFragment", "oncreateView")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emailFocusListener()
        passwordFocusListener()
        binding.loginButton.setOnClickListener{
            it.findNavController().navigate(R.id.action_loginFragment_to_homePageFragment)
        }

        binding.SignUpTextView.setOnClickListener {
            it.findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }
    }

    private fun emailFocusListener() {
        binding.emailTextInputEditText.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                binding.emailTextInputLayout.helperText = getEmail()
            }
        }
    }

    private fun getEmail(): String? {
        val email = binding.emailTextInputEditText.text.toString()
        return Helper.validateEmail(email)
    }

    private fun passwordFocusListener() {
        binding.passwordTextInputEditText.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                binding.passwordTextInputLayout.helperText = validPassword()
            }
        }
    }

    private fun validPassword(): String? {
        val password = binding.passwordTextInputEditText.text.toString()
        return Helper.validatePasswordText(password)
    }

}