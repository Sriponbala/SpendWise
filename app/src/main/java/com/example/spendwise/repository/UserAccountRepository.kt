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
            userAccountDao.insertUser(user)
        }
    }

    suspend fun insertUserPassword(userPassword: UserPassword) {
        withContext(Dispatchers.IO) {
            userAccountDao.insertUserPassword(userPassword)
        }
    }

    fun getUser(email: String): User? = userAccountDao.getUser(email)

    fun getUser(userId: Int): User? = userAccountDao.getUser(userId)

    fun checkIfUserExists(email: String) = userAccountDao.checkIfUserExists(email)

    fun verifyPassword(userId: Int, password: String) = userAccountDao.verifyPassword(userId, password)

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

    /*suspend fun deleteAllRecords() {
        withContext(Dispatchers.IO) {
            userAccountDao.deleteAllRecords()
            userAccountDao.deleteAllUserPasswordsRecords()
        }
    }*/

}