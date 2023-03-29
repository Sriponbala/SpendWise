package com.example.spendwise.repository

import android.util.Log
import com.example.spendwise.dao.UserAccountDao
import com.example.spendwise.domain.User
import com.example.spendwise.domain.UserPassword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserAccountRepository(private val userAccountDao: UserAccountDao) {


    suspend fun insertUser(user: User) {
        withContext(Dispatchers.IO) {
            Log.e("User", user.toString() + "insertUser")
            userAccountDao.insertUser(user)
        }
    }

    suspend fun insertUserPassword(userPassword: UserPassword) {
        withContext(Dispatchers.IO) {
            userAccountDao.insertUserPassword(userPassword)
        }
    }

    suspend fun deleteUser(userId: Int) {
        userAccountDao.deleteUser(userId)
    }

    suspend fun updateUserAccount(
        userId: Int,
        firstName: String,
        lastName: String,
        mobile: String,
        gender: String,
    ) {
        userAccountDao.updateUserAccount(userId, firstName, lastName, mobile, gender)
    }

    suspend fun updatePassword(userId: Int, password: String) {
        userAccountDao.updateUserPassword(userId, password)
    }

    suspend fun getUser(email: String): User? {
        return withContext(Dispatchers.IO) {
            val user = userAccountDao.getUser(email)
            Log.e("User", user.toString() + "getUser")
            Log.e("SignupFragment - dao", "${user.toString()}")
            user
        }
    }

    suspend fun checkUniqueUser(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            userAccountDao.checkIfUserExists(email)
        }
    }

   /* suspend fun checkIfUserExists(userId: Int, email: String, password: String): Boolean {
        return userAccountDao.checkIfUserExists(email) && userAccountDao.verifyPassword(userId, password)
    }*/

}