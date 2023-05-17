package com.example.spendwise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.spendwise.database.SpendWiseDatabase
import com.example.spendwise.domain.User
import com.example.spendwise.domain.UserPassword
import com.example.spendwise.repository.UserAccountRepository
import kotlinx.coroutines.*

class UserViewModel(application: Application): AndroidViewModel(application) {

    private val repository: UserAccountRepository = UserAccountRepository(SpendWiseDatabase.getInstance(application).userAccountDao)

    var user: User? = null

    val isEmailExists = MutableLiveData<Boolean?>()
    val isLoggedIn = MutableLiveData<Boolean>()
    val isNewUserInserted = MutableLiveData<Boolean?>()
    val isUserFetchedFinally = MutableLiveData<Boolean>()

    fun createUserAccount(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var userData: User? = null
            val fetchJob = launch {
                val user = User(email)
                repository.insertUser(user)
                userData = repository.getUser(email)
            }
            fetchJob.join()
            withContext(Dispatchers.Main){
                user = userData
                isNewUserInserted.value = true
            }
        }
    }

    val isUserFetched = MutableLiveData<Boolean>()

    fun fetchUser(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var userData: User? = null
            val fetchJob = launch {
                userData = repository.getUser(email)
            }
            fetchJob.join()
            withContext(Dispatchers.Main){
                user = userData
                isUserFetched.value = true
            }
        }
    }

    fun fetchUser(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            var userData: User? = null
            val fetchJob = launch {
                userData = repository.getUser(userId)
            }
            fetchJob.join()
            withContext(Dispatchers.Main){
                user = userData
                isUserFetched.value = true
            }
        }
    }

    fun checkIfUserExists(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var result: Boolean? = null
            val job = launch {
                result = repository.checkIfUserExists(email)
            }
            job.join()
            withContext(Dispatchers.Main) {
                isEmailExists.value = result
            }
        }
    }

    val isCorrectPassword = MutableLiveData<Boolean>()
    fun verifyPassword(password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var result = false
            val job = launch {
                user?.userId?.let {
                    result = repository.verifyPassword(it, password)
                }
            }
            job.join()
            withContext(Dispatchers.Main){
                isCorrectPassword.value = result
            }
        }
    }

    fun insertPassword(password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                repository.insertUserPassword(UserPassword(user!!.userId, password))
            }
        }
    }

}