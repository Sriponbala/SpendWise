package com.example.spendwise.dao

import androidx.room.*
import com.example.spendwise.domain.User
import com.example.spendwise.domain.UserPassword

@Dao
interface UserAccountDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertUser(user: User)

    @Insert
    fun insertUserPassword(userPassword: UserPassword)

    @Query("delete from User where userId = :userId")
    fun deleteUser(userId: Int)

    @Query("delete from UserPassword where userId = :userId")
    fun deleteUserPassword(userId: Int)

    @Query("update User set firstName=:firstName, lastName=:lastName, mobile=:mobile, gender=:gender where userId = :userId")
    fun updateUserAccount(userId: Int, firstName: String, lastName: String, mobile: String, gender: String)

    @Query("update UserPassword set password=:password where userId=:userId")
    fun updateUserPassword(userId: Int, password: String)

    @Query("select * from User where email = :email")
    fun getUser(email: String): User?

    @Query("select * from User where userId = :userId")
    fun getUser(userId: Int): User?

    @Query("select exists(select email from User where email = :email)")
    fun checkIfUserExists(email: String): Boolean

    @Query("select exists(select password from UserPassword where userId = :userId and password = :password)")
    fun verifyPassword(userId: Int, password: String): Boolean

}