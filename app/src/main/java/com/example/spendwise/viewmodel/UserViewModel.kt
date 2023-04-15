package com.example.spendwise.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.spendwise.database.SpendWiseDatabase
import com.example.spendwise.domain.User
import com.example.spendwise.domain.UserPassword
import com.example.spendwise.repository.UserAccountRepository
import kotlinx.coroutines.*
import java.lang.ref.WeakReference

class UserViewModel(application: Application): AndroidViewModel(application) {

    private val repository: UserAccountRepository = UserAccountRepository(SpendWiseDatabase.getInstance(application).userAccountDao)

    var user: User? = null
    /*var user = MutableLiveData<User?>().also {
        Log.e("User", "getter " + it.value.toString())
    }*/
    /*val user: LiveData<User?>
        get() = _user.also {
            Log.e("User", "getter" + it.value.toString())
        }*/

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
             //   user.value = userData
                user = userData
//                _user.value = userData
//                Log.e("User", "create - ${_user.value.toString()}")
                isNewUserInserted.value = true
            }
        }
        /*uiScope.launch {
            val user = User(email.trim())
            repository.insertUser(user)
            _user.value = repository.getUser(email)
            isLoggedIn.value = true
        }*/
    }

/*    suspend fun fetchUser(email: String) {
            Log.e("Login", "User view model 1 - ${repository.getUser(email)}")
            _user.value = repository.getUser(email)
            Log.e("Login", "User view model 2 - ${_user.value}")
    }*/

    val isUserFetched = MutableLiveData<Boolean>()

    fun fetchUser(email: String) {
        Log.e("Email", email)
        viewModelScope.launch(Dispatchers.IO) {
            var userData: User? = null
            val fetchJob = launch {
                userData = repository.getUser(email)
            }
            fetchJob.join()
            withContext(Dispatchers.Main){
//                _user.value = userData
                Log.e("User fetch", user.toString())
                user = userData
                isUserFetched.value = true
            }
        }
    }

    fun fetchUser(userId: Int) {
        Log.e("userId from home", userId.toString())
        viewModelScope.launch(Dispatchers.IO) {
            var userData: User? = null
            val fetchJob = launch {
                userData = repository.getUser(userId)
            }
            fetchJob.join()
            withContext(Dispatchers.Main){
//                _user.value = userData
                Log.e("User fetch userID 1", user.toString())
//                user = userData
//                Log.e("User fetch userID 2", user.toString())
//                isUserFetched.value = true
//                Log.e("User fetch userID 3", user.toString())
            }
        }
    }

    fun checkIfUserExists(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var result: Boolean? = null
            val job = launch {
                Log.e("Email", email)
                result = repository.checkIfUserExists(email)
                Log.e("User", "result1 - $result")
            }
            job.join()
            withContext(Dispatchers.Main) {
                Log.e("User", "result2 - $result")
                isEmailExists.value = result
            }
        }
    }

    val isCorrectPassword = MutableLiveData<Boolean>()
    fun verifyPassword(password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var result = false
            val job = launch {
                result = repository.verifyPassword(user!!.userId, password)
            }
            job.join()
            withContext(Dispatchers.Main){
                isCorrectPassword.value = result
            }
        }
    }

 //   fun checkIfUserExists(email: String) = repository.checkIfUserExists(email.trim())

    fun verifyPassword(userId: Int, password: String) = repository.verifyPassword(userId, password)

/*    fun insertPassword(password: String, email: String) {
        uiScope.launch {
            val user = repository.getUser(email)
            val userPassword = UserPassword(user!!.userId,password)
            repository.insertUserPassword(userPassword)
        }
    }*/

    fun insertPassword(password: String, email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                repository.insertUserPassword(UserPassword(user!!.userId, password))
            }
        }
    }

    /*fun deleteAllRecords() {
        viewModelScope.launch {
            val job = launch {
                repository.deleteAllRecords()
            }
        }
    }*/


}