package com.example.spendwise.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.example.spendwise.domain.User
import com.example.spendwise.domain.UserPassword

@Dao
interface UserAccountDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertUser(user: User)

    @Insert
    fun insertUserPassword(userPassword: UserPassword)

    @Query("delete from User where userId like :userId")
    fun deleteUser(userId: Int)

    @Query("delete from UserPassword where userId like :userId")
    fun deleteUserPassword(userId: Int)

    @Query("update User set firstName=:firstName, lastName=:lastName, mobile=:mobile, gender=:gender where userId like :userId")
    fun updateUserAccount(userId: Int, firstName: String, lastName: String, mobile: String, gender: String)

    @Query("update UserPassword set password=:password where userId=:userId")
    fun updateUserPassword(userId: Int, password: String)

    @Query("select * from User where email like :email")
    fun getUser(email: String): User?

    @Query("select exists(select email from User where email like :email)")
    fun checkIfUserExists(email: String): Boolean //LiveData<Boolean>

    @Query("select exists(select password from UserPassword where userId like :userId and password like :password)")
    fun verifyPassword(userId: Int, password: String): Boolean // LiveData<Boolean>

}