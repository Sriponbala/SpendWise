package com.example.spendwise.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.spendwise.database.SpendWiseDatabase
import com.example.spendwise.domain.User
import com.example.spendwise.domain.UserPassword
import com.example.spendwise.repository.UserAccountRepository
import kotlinx.coroutines.*

class UserViewModel(application: Application): AndroidViewModel(application) {

    private val repository: UserAccountRepository = UserAccountRepository(SpendWiseDatabase.getInstance(application).userAccountDao)
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user

    private var _uniqueUser = MutableLiveData<Boolean>()
    val uniqueUser: LiveData<Boolean>
       get() = _uniqueUser


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun createUserAccount(email: String) {
        uiScope.launch {
            val user = User(email.trim())
            repository.insertUser(user)
            _user.value = repository.getUser(email)
           // loadUser(email)
          //  val userPassword = UserPassword(user.userId, password)
           // repository.insertUserPassword(userPassword)
        }
    }

    private fun loadUser(email: String) {
        uiScope.launch {
            Log.e("User", repository.getUser(email).toString() + "load user")
            /*user.value = repository.getUser(email)
            Log.e("User", _user.value.toString())*/
            _user.value = repository.getUser(email)
            Log.e("User", user.toString())
        }
    }

    fun checkUniqueUser(email: String) {
        uiScope.launch {
            val uniqueUser = repository.checkUniqueUser(email.trim())
            Log.e("Unique", "model " + uniqueUser.toString())
            _uniqueUser.value = uniqueUser
        }
       // Log.e("Unique - call", uniqueUser.toString())
    }

    fun insertPassword(password: String, email: String) {
        uiScope.launch {
            val user = repository.getUser(email)
            val userPassword = UserPassword(user!!.userId,password)
            repository.insertUserPassword(userPassword)
        }
    }


}