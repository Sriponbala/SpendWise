package com.example.spendwise.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.spendwise.dao.RecordDao
import com.example.spendwise.dao.UserAccountDao
import com.example.spendwise.domain.Record
import com.example.spendwise.domain.User
import com.example.spendwise.domain.UserPassword

@Database(entities = [User::class, UserPassword::class, Record::class], version = 1, exportSchema = false)
abstract class SpendWiseDatabase: RoomDatabase() {

    abstract val userAccountDao: UserAccountDao
    abstract val recordDao: RecordDao

    companion object {
        @Volatile
        private var INSTANCE: SpendWiseDatabase? = null
        fun getInstance(context: Context): SpendWiseDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if(instance == null) {
                    instance = Room.databaseBuilder(context,
                           SpendWiseDatabase::class.java,
                           "spend_wise_database").fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}