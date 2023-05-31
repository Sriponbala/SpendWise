package com.example.spendwise.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
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

class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
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
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
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

        emailFocusListener()
        passwordFocusListener()
        confirmPasswordFocusListener()

        binding.signUpButton.setOnClickListener {
            registerUser()
        }
        binding.LoginTextView.setOnClickListener {
            it.findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }

/*
        binding.confirmPasswordTextInputEditText.addTextChangedListener {
            if(it != null) {
                binding.confirmPasswordTextInputLayout.helperText = Helper.validateConfirmPassword(binding.passwordTextInputEditText.text.toString(),binding.confirmPasswordTextInputEditText.text.toString())
            }
        }
*/

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
            userViewModel.isEmailExists.observe(viewLifecycleOwner) {
                if (it != null) {
                    if (it) {
                        binding.emailTextInputLayout.helperText = resources.getString(
                            R.string.two_strings_concate,
                            resources.getString(R.string.user_label),
                            resources.getString(R.string.already_exists)
                        )
                    } else {
                        registerNewUser()
                    }
                    userViewModel.isEmailExists.value = null
                }
            }
        } else {
            binding.emailTextInputLayout.helperText = validEmail()
            binding.passwordTextInputLayout.helperText = validPasswordText()
            binding.confirmPasswordTextInputLayout.helperText = validConfirmPassword()
        }
    }

    private fun registerNewUser() {
        userViewModel.createUserAccount(binding.emailTextInputEditText.text.toString())
        userViewModel.isNewUserInserted.observe(viewLifecycleOwner) {
            if (it != null) {
                userViewModel.insertPassword(binding.passwordTextInputEditText.text.toString())
                editor.apply {
                    putInt(resources.getString(R.string.userId), userViewModel.user!!.userId)
                    putString(resources.getString(R.string.status), LogInStatus.LOGGED_IN.name)
                    apply()
                }
                moveToNextFragment()
                userViewModel.isNewUserInserted.value = null
            }
        }
    }

    private fun moveToNextFragment() {
        this.findNavController().navigate(R.id.action_signUpFragment_to_homePageFragment)
    }

}











//1	1	A@asbvw  1	abi@nkdk.com	1
// Bala@123 2	uma@gmail.com	2

/*2	1	Shopping	80.0	Expense	5/3/2023	2
3	1	Rental	500.0	Expense	29/3/2023	3
4	1	Transport	800.0	Expense	5/3/2023	4
5	1	Shopping	50.0	Expense	6/4/2023	5
6	1	Transport	60.0	Expense	6/4/2023	6
7	1	Coupons	50.0	Income	7/4/2023	7
8	2	Gifts	80.0	Income	8/4/2023	8
9	2	Shopping	500000.0	Expense	27/4/2023	9
10	2	Vehicle	588888.0	Expense	9/4/2023	10	kehuyewcuvwecbiejcioh8cgeycgieniejiocjihyg	khdy7eg7cuhewihciewhciuewh ubuiew 9we ineiniwh9eh8ey87eg8ehc9h9cecjec9ejoej0e0
11	2	Entertainment	9000.0	Expense	10/4/2023	11		*/

// #76a8ab